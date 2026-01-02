package com.romreviewertools.noteitup.data.encryption

/**
 * Platform-specific encryption service for secure data encryption.
 * Uses AES-256-GCM for authenticated encryption.
 */
expect class EncryptionService() {
    /**
     * Derives an encryption key from a password using PBKDF2 with SHA-256.
     * Uses 100,000 iterations for brute-force resistance.
     *
     * @param password The user's password
     * @param salt Random salt (16 bytes recommended)
     * @return 256-bit derived key
     */
    fun deriveKey(password: String, salt: ByteArray): ByteArray

    /**
     * Encrypts data using AES-256-GCM.
     * Returns: IV (12 bytes) + ciphertext + auth tag (16 bytes)
     *
     * @param data The plaintext data to encrypt
     * @param key 256-bit encryption key
     * @return Encrypted data with IV prepended
     */
    fun encrypt(data: ByteArray, key: ByteArray): ByteArray

    /**
     * Decrypts data encrypted with AES-256-GCM.
     * Input format: IV (12 bytes) + ciphertext + auth tag (16 bytes)
     *
     * @param encryptedData The encrypted data with IV prepended
     * @param key 256-bit encryption key
     * @return Decrypted plaintext data
     * @throws Exception if decryption fails (wrong key or corrupted data)
     */
    fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray

    /**
     * Generates a random salt for key derivation.
     *
     * @return 16-byte random salt
     */
    fun generateSalt(): ByteArray

    /**
     * Generates a random initialization vector for encryption.
     *
     * @return 12-byte random IV (as recommended for GCM)
     */
    fun generateIv(): ByteArray
}
