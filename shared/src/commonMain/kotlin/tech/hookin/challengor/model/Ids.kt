package tech.hookin.challengor.model

import kotlin.uuid.Uuid

/**
 * Creates a new random id. Ids are generated on the device (not by the server),
 * so objects created offline never clash and need no remapping when they sync.
 */
fun newId(): String = Uuid.random().toString()
