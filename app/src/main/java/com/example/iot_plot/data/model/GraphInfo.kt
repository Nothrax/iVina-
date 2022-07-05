package com.example.iot_plot.data.model

import com.example.iot_plot.influx_connection.InfluxDataView
import java.util.*
import kotlin.collections.ArrayList

data class GraphInfo(
    val bucket: String,
    val place: String,
    val device: String,
    val measurements: ArrayList<String>,
    val timeFrom: Calendar,
    val timeTo: Calendar,
    val graphName: String,

    )


data class InfluxGraphs(
    val graphs: ArrayList<GraphInfo>
)