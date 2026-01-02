package com.romreviewertools.noteitup

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform