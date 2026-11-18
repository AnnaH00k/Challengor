package tech.hookin.challengor.domain

const val MIN_PASSWORD_LENGTH = 6

private const val FORBIDDEN_CHARS = "<>{}()[];:'\"\\/"
private val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

/** Returns an error message, or `null` if [input] is a valid email address. */
fun validateEmail(input: String): String? = when {
    input.any { it.isWhitespace() } -> "Email cannot contain whitespace"
    input.any { it in FORBIDDEN_CHARS } -> "Email contains invalid characters"
    !EMAIL_REGEX.matches(input) -> "Invalid email format"
    else -> null
}

/** Returns an error message, or `null` if [input] is an acceptable password. */
fun validatePassword(input: String): String? = when {
    input.any { it.isWhitespace() } -> "Password cannot contain whitespace"
    input.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
    input.any { it in FORBIDDEN_CHARS } -> "Password contains invalid characters"
    else -> null
}

/** Topics are stored trimmed and lowercase so "Sport" and " sport" are the same topic. */
fun normalizeTopic(input: String): String = input.trim().lowercase()

/** Splits a comma-separated list into normalized, distinct, non-empty topics. */
fun parseTopics(input: String): List<String> =
    input.split(',')
        .map(::normalizeTopic)
        .filter { it.isNotEmpty() }
        .distinct()
