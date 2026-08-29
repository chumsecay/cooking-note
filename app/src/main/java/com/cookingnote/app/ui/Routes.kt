package com.cookingnote.app.ui

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Library : Route("library")
    data object Pantry : Route("pantry")
    data object Ai : Route("ai")
    data object Settings : Route("settings")
    data object Favorites : Route("favorites")
    data object History : Route("history")
    data object Create : Route("create")
    data object Search : Route("search")

    data object Detail : Route("detail/{id}") {
        fun of(id: Long) = "detail/$id"
        const val ARG_ID = "id"
    }
    data object Edit : Route("edit/{id}") {
        fun of(id: Long) = "edit/$id"
        const val ARG_ID = "id"
    }
}
