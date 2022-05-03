package com.example.iot_plot.data

import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.example.iot_plot.data.model.LoggedInUser
import java.io.IOException
import android.os.AsyncTask




/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(serverAddress: String, organization: String, token: String): Result<LoggedInUser> {
        try {
            val influxDBClient = InfluxDBClientKotlinFactory.create(serverAddress, token.toCharArray())
            val response = influxDBClient.ping()
            influxDBClient.close()
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            if(!response){
                return Result.Error(IOException("Error logging in"))
            }
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}