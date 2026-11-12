package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Library : Screen("library")
    data object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    data object Editor : Screen("editor/{docType}/{docId}") {
        fun createRoute(docType: String, docId: Long) = "editor/$docType/$docId"
    }
    data object Notes : Screen("notes")
    data object Statistics : Screen("statistics")
    data object Trash : Screen("trash")
    data object Settings : Screen("settings")
    data object PinLock : Screen("pin_lock")
}
