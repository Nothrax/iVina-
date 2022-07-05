package com.example.iot_plot.influx_connection


data class InfluxResult (
    val success: InfluxDataView? = null,
    val error: Int? = null
)