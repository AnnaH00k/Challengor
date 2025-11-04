package tech.hookin.learningkmp.objects

data class User(
    val id: Int,
    val name: String,
    val password: String,
    val email: String,
    var createdChallenges: List<Challenge> = emptyList(),
)
