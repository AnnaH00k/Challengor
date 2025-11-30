package tech.hookin.learningkmp.objects

import kotlinx.serialization.Serializable


@Serializable
data class Challenge(
    val id: Int,
    val name: String,
    var description: String,
    var tasks: List<Task> = emptyList(),

    )
