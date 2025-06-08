package io.asterixorobelix.afrikaburn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform