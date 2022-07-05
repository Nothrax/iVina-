package com.example.iot_plot.influx_connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InfluxViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfluxViewModel::class.java)) {
            return InfluxViewModel(
                influxRepository = InfluxRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}