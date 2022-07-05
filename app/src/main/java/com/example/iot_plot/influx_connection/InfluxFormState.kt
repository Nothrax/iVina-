package com.example.iot_plot.influx_connection

/**
 * Data validation state of the login form.
 */
data class InfluxFormState (
    val addressError: Int? = null,
    val organizationError: Int? = null,
    val tokenError: Int? = null,
    val isDataValid: Boolean = false
)