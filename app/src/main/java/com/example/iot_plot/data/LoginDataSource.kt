package com.example.iot_plot.data

import com.example.iot_plot.data.model.LoggedInUser
import java.io.IOException
import com.example.iot_plot.influx_connection.InfluxRepository


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(serverAddress: String, organization: String, token: String): Result<LoggedInUser> {
        val influx = InfluxRepository()
        if (influx.makePingRequest(serverAddress, token, organization)) {
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            return Result.Success(fakeUser)
        } else {
            return Result.Error(IOException("Error logging in"))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}