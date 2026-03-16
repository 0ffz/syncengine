package me.dvyy.syncengine.actions

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

interface Action {
    /**
     * Combines this action with another action that occurred right before this one.
     * The result of running reducers on both actions should be the same as the combined action returned here.
     *
     * Returns null if the two cannot be combined.
     */
    fun reduce(previous: Action): Action? = null

    companion object {
        val IDENTITY = IdentityAction

        private val reset = "\u001b[0m"
        private val gray = "\u001b[90m"
        private val green = "\u001b[32m"
        private val yellow = "\u001b[33m"
        private val keyRegex = "\"(\\w+)\"\\s*:".toRegex()
        private fun colorizeJson(json: String): String = json
            .replace(keyRegex, "${green}\"$1\"${reset}:")
            .replace("{", "${gray}{${reset}")
            .replace("}", "${gray}}${reset}")

    }

    fun prettyString(actionSerializer: KSerializer<Action>): String {
        val json = Json.encodeToString(actionSerializer, this)
        return "$yellow(${this::class.simpleName})$reset: " + colorizeJson(json)
    }
}
