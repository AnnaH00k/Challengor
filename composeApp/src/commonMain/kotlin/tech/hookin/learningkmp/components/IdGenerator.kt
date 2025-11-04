package tech.hookin.learningkmp.components

fun <T> AutoIncrementIdGenerator(items: List<T>, idSelector: (T) -> Int?): Int {
    val maxId = items.mapNotNull(idSelector).maxOrNull() ?: 0
    return maxId + 1
}
