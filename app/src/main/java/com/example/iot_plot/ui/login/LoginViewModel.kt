package com.example.iot_plot.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.iot_plot.data.LoginRepository
import com.example.iot_plot.data.Result

import com.example.iot_plot.R
import android.content.Intent
import android.os.SystemClock

import androidx.core.content.ContextCompat
import com.example.iot_plot.MainActivity
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun login(serverAddress: String, organization: String, token: String) {
        executor.execute{
            //only visual
            SystemClock.sleep(3000)
            val result = loginRepository.login(serverAddress, organization, token)
            if (result is Result.Success) {
                _loginResult.postValue(LoginResult(success = LoggedInUserView(apiAddress = serverAddress, token = token, organization = organization)))
            } else {
                _loginResult.postValue(LoginResult(error = R.string.login_failed))
            }
        }
    }

    fun loginDataChanged(serverAddress: String, organization: String, token: String) {
        if (!isAddressValid(serverAddress)) {
            _loginForm.value = LoginFormState(addressError = R.string.invalid_address)
        } else if (!isOrganizationValid(organization)) {
            _loginForm.value = LoginFormState(organizationError = R.string.invalid_organization)
        } else if(!isTokenValid(token)){
            _loginForm.value = LoginFormState(tokenError = R.string.invalid_token)
        }else{
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isAddressValid(address: String): Boolean {
        //todo better check?
        return address.isNotBlank()
    }

    private fun isOrganizationValid(organization: String): Boolean {
        //todo better check?
        return organization.isNotBlank()
    }

    private fun isTokenValid(token: String): Boolean {
        //todo better check?
        return token.length > 5
    }
}