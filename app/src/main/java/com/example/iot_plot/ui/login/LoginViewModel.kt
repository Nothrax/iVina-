package com.example.iot_plot.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.iot_plot.data.LoginRepository
import com.example.iot_plot.data.Result

import com.example.iot_plot.R
import android.content.Intent

import androidx.core.content.ContextCompat
import com.example.iot_plot.MainActivity


class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(serverAddress: String, organization: String, token: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(serverAddress, organization, token)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
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