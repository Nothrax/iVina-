package com.example.iot_plot.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val addressError: Int? = null,
    val organizationError: Int? = null,
    val tokenError: Int? = null,
    val isDataValid: Boolean = false
)