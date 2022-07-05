package com.example.iot_plot.influx_connection

data class InfluxDataView(
    val apiAddress: String? = null,
    val token: String? = null,
    val organization: String? = null,
    val bucketList: ArrayList<String>? = null,
    val locationList: ArrayList<String>? = null,
    val deviceList: ArrayList<String>? = null,
    val measurementList: ArrayList<String>? = null,
    val dataList: ArrayList<Value>? = null
)