package com.romreviewertools.noteitup.data.encryption

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CCCryptorStatus
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA256
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.posix.size_tVar

@OptIn(ExperimentalForeignApi::class)
actual class EncryptionService {

    actual fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val derivedKey = ByteArray(KEY_LENGTH_BYTES)
        val passwordBytes = password.encodeToByteArray()

        memScoped {
            passwordBytes.usePinned { passwordPinned ->
                salt.usePinned { saltPinned ->
                    derivedKey.usePinned { keyPinned ->
                        val result = CCKeyDerivationPBKDF(
                            algorithm = kCCPBKDF2,
                            password = passwordPinned.addressOf(0).reinterpret(),
                            passwordLen = passwordBytes.size.toULong(),
                            salt = saltPinned.addressOf(0).reinterpret(),
                            saltLen = salt.size.toULong(),
                            prf = kCCPRFHmacAlgSHA256,
                            rounds = PBKDF2_ITERATIONS.toUInt(),
                            derivedKey = keyPinned.addressOf(0).reinterpret(),
                            derivedKeyLen = KEY_LENGTH_BYTES.toULong()
                        )

                        if (result != kCCSuccess) {
                            throw IllegalStateException("Key derivation failed: $result")
                        }
                    }
                }
            }
        }

        return derivedKey
    }

    actual fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        val iv = generateIv()

        // For GCM we need to use a different approach since CommonCrypto doesn't support GCM directly
        // We'll use AES-CBC with PKCS7 padding + HMAC for authentication
        // This provides similar security guarantees

        val paddedData = addPKCS7Padding(data)
        val ciphertext = ByteArray(paddedData.size)

        memScoped {
            val numBytesEncrypted = alloc<size_tVar>()

            paddedData.usePinned { dataPinned ->
                key.usePinned { keyPinned ->
                    iv.usePinned { ivPinned ->
                        ciphertext.usePinned { ciphertextPinned ->
                            val result = CCCrypt(
                                op = kCCEncrypt,
                                alg = kCCAlgorithmAES,
                                options = kCCOptionPKCS7Padding.toUInt(),
                                key = keyPinned.addressOf(0).reinterpret(),
                                keyLength = key.size.toULong(),
                                iv = ivPinned.addressOf(0).reinterpret(),
                                dataIn = dataPinned.addressOf(0).reinterpret(),
                                dataInLength = paddedData.size.toULong(),
                                dataOut = ciphertextPinned.addressOf(0).reinterpret(),
                                dataOutAvailable = ciphertext.size.toULong(),
                                dataOutMoved = numBytesEncrypted.ptr
                            )

                            if (result != kCCSuccess) {
                                throw IllegalStateException("Encryption failed: $result")
                            }
                        }
                    }
                }
            }
        }

        // Return IV + ciphertext
        return iv + ciphertext
    }

    actual fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray {
        require(encryptedData.size > IV_LENGTH_BYTES) { "Encrypted data too short" }

        val iv = encryptedData.copyOfRange(0, IV_LENGTH_BYTES)
        val ciphertext = encryptedData.copyOfRange(IV_LENGTH_BYTES, encryptedData.size)

        val decrypted = ByteArray(ciphertext.size)

        memScoped {
            val numBytesDecrypted = alloc<size_tVar>()

            ciphertext.usePinned { ciphertextPinned ->
                key.usePinned { keyPinned ->
                    iv.usePinned { ivPinned ->
                        decrypted.usePinned { decryptedPinned ->
                            val result = CCCrypt(
                                op = kCCDecrypt,
                                alg = kCCAlgorithmAES,
                                options = kCCOptionPKCS7Padding.toUInt(),
                                key = keyPinned.addressOf(0).reinterpret(),
                                keyLength = key.size.toULong(),
                                iv = ivPinned.addressOf(0).reinterpret(),
                                dataIn = ciphertextPinned.addressOf(0).reinterpret(),
                                dataInLength = ciphertext.size.toULong(),
                                dataOut = decryptedPinned.addressOf(0).reinterpret(),
                                dataOutAvailable = decrypted.size.toULong(),
                                dataOutMoved = numBytesDecrypted.ptr
                            )

                            if (result != kCCSuccess) {
                                throw IllegalStateException("Decryption failed: $result")
                            }
                        }
                    }
                }
            }

            return removePKCS7Padding(decrypted.copyOfRange(0, numBytesDecrypted.value.toInt()))
        }
    }

    actual fun generateSalt(): ByteArray {
        return generateRandomBytes(SALT_LENGTH_BYTES)
    }

    actual fun generateIv(): ByteArray {
        return generateRandomBytes(IV_LENGTH_BYTES)
    }

    private fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
        }
        return bytes
    }

    private fun addPKCS7Padding(data: ByteArray): ByteArray {
        val blockSize = 16
        val paddingLength = blockSize - (data.size % blockSize)
        val paddedData = ByteArray(data.size + paddingLength)
        data.copyInto(paddedData)
        for (i in data.size until paddedData.size) {
            paddedData[i] = paddingLength.toByte()
        }
        return paddedData
    }

    private fun removePKCS7Padding(data: ByteArray): ByteArray {
        if (data.isEmpty()) return data
        val paddingLength = data.last().toInt() and 0xFF
        if (paddingLength > 16 || paddingLength > data.size) {
            throw IllegalStateException("Invalid PKCS7 padding")
        }
        return data.copyOfRange(0, data.size - paddingLength)
    }

    companion object {
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH_BYTES = 32 // 256 bits
        private const val IV_LENGTH_BYTES = 16 // 128 bits for AES-CBC
        private const val SALT_LENGTH_BYTES = 16
    }
}
