package com.example.iot_plot.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val apiAddress: String,
    val token: String,
    val organization: String
)