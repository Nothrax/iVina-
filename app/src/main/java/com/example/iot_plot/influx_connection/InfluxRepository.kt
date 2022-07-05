package com.example.iot_plot.influx_connection


import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

data class Value(val measurement: String, val value: Double, val time: Instant)

class InfluxRepository {



    fun makeBucketListRequest(
        address: String, token: String, organization: String,
    ): ArrayList<String> {
        val influxDBClient =
            InfluxDBClientKotlinFactory.create(address, token.toCharArray(), organization)
        val query = "buckets()" +
                " |> drop(columns: [\"_stop\", \"time\", \"id\", \"organizationID\", \"retentionPolicy\", \"retentionPeriod\"])"
        val results = influxDBClient.getQueryKotlinApi().query(query)
        val list: ArrayList<String> = ArrayList()

        runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                results.consumeEach { list.add(it.values["name"] as String) }
            }
        }
        return list
    }

    fun makeLocationListRequest(
        address: String, token: String, organization: String,
        bucket: String,
    ): ArrayList<String> {
        val influxDBClient =
            InfluxDBClientKotlinFactory.create(address, token.toCharArray(), organization)
        val query = "from(bucket: \"" + bucket + "\")\n" +
                "  |> range(start: 0, stop: 999999999999999999)\n" +
                "  |> drop(columns: [\"_start\", \"_stop\", \"_field\",\"_measurement\", \"_value\"])\n" +
                "  |> distinct(column: \"location\")"
        val list: ArrayList<String> = ArrayList()
        try {
            val results = influxDBClient.getQueryKotlinApi().query(query)
            runBlocking { // this: CoroutineScope
                launch { // launch a new coroutine and continue
                    results.consumeEach { list.add(it.values["_value"] as String) }
                }
            }
        } catch (e: java.lang.Exception) {
            // handler
        }

        return list
    }

    fun makeDeviceListRequest(
        address: String, token: String, organization: String,
        bucket: String,
        location: String
    ): ArrayList<String> {
        val influxDBClient =
            InfluxDBClientKotlinFactory.create(address, token.toCharArray(), organization)
        val query = "from(bucket: \"" + bucket + "\")\n" +
                "  |> range(start: 0, stop: 999999999999999999)\n" +
                "  |> drop(columns: [\"_field\", \"_start\", \"_stop\"])\n" +
                "  |> filter(fn: (r) => r[\"location\"] == \"" + location + "\")\n" +
                "  |> distinct(column: \"_measurement\")"
        val list: ArrayList<String> = ArrayList()
        try {
            val results = influxDBClient.getQueryKotlinApi().query(query)
            runBlocking { // this: CoroutineScope
                launch { // launch a new coroutine and continue
                    results.consumeEach { list.add(it.values["_value"] as String) }
                }
            }
        } catch (e: java.lang.Exception) {
            // handler
        }

        return list
    }

    fun makeMeasurementListRequest(
        address: String, token: String, organization: String,
        bucket: String,
        location: String,
        device: String,
    ):ArrayList<String> {
        val influxDBClient =
            InfluxDBClientKotlinFactory.create(address, token.toCharArray(), organization)
        val query = "from(bucket: \"" + bucket + "\")\n" +
                "  |> range(start: 0, stop: 999999999999999999)\n" +
                "  |> filter(fn: (r) => r[\"location\"] == \"" + location + "\")\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"" + device + "\")\n" +
                "  |> drop(columns: [\"_value\", \"_measurement\",\"_start\", \"_stop\", \"location\"])\n" +
                "  |> distinct(column: \"_field\")"
        val list: ArrayList<String> = ArrayList()
        try {
            val results = influxDBClient.getQueryKotlinApi().query(query)
            runBlocking { // this: CoroutineScope
                launch { // launch a new coroutine and continue
                    results.consumeEach { list.add(it.values["_value"] as String) }
                }
            }
        } catch (e: java.lang.Exception) {
            // handler
        }


        return list
    }

    fun makeGraphRequest(
        address: String, token: String, organization: String,
        bucket: String,
        location: String,
        device: String,
        measurements: List<String>,
        timeFrom: Calendar,
        timeTo: Calendar
    ): ArrayList<Value> {

        val list: ArrayList<Value> = ArrayList()

        if (measurements.isEmpty()) {
            return list
        } else {
            val startTimestamp: Long = timeFrom.getTime().time / 1000
            val endTimestamp: Long = timeTo.getTime().time / 1000
            val influxDBClient =
                InfluxDBClientKotlinFactory.create(address, token.toCharArray(), organization)
            var query = "from(bucket: \"" + bucket + "\")\n" +
                    "  |> range(start: " + startTimestamp + ", stop: " + endTimestamp + ")\n" +
                    "  |> filter(fn: (r) => r[\"_measurement\"] == \"" + device + "\")\n"

            query += "  |> filter(fn: (r) =>"
            query += " r[\"_field\"] == \"" + measurements[0] + "\" "
            val leftMeasurements = measurements.subList(1, measurements.lastIndex + 1)
            for (measurement in leftMeasurements) {
                query += "or  r[\"_field\"] == \"$measurement\" "
            }
            query += ")\n"
            query += "  |> filter(fn: (r) => r[\"location\"] == \"$location\")\n"

            try {
                val results = influxDBClient.getQueryKotlinApi().query(query)
                runBlocking {
                    launch {
                        results.consumeEach { list.add(Value(
                            it.values["_field"] as String,
                            it.values["_value"] as Double,
                            it.values["_time"] as Instant
                        )) }
                        val resultList = results.toList()
                        for (result in resultList) {
                            list.add(
                                Value(
                                    result.values["_field"] as String,
                                    result.values["_value"] as Double,
                                    result.values["_time"] as Instant
                                )
                            )
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                print("test")
            }
            return list
        }
    }


    fun makePingRequest(address: String, token: String, organization: String): Boolean {
        val influxDBClient = InfluxDBClientKotlinFactory.create(address, token.toCharArray())
        try {
            val response = influxDBClient.ping()
            influxDBClient.close()
            return response
        } catch (e: Exception) {
            return false
        }
    }

}