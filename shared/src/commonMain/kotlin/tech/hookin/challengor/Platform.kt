package tech.hookin.challengor

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform