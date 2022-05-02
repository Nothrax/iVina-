package com.example.iot_plot.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.iot_plot.MainActivity
import com.example.iot_plot.databinding.ActivityLoginBinding

import com.example.iot_plot.R

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //todo store values
        val serverAddress = binding.apiAddressField
        val organization = binding.organizationField
        val token = binding.tokenField
        val saveLogin = binding.saveLoginCheck
        val login = binding.loginButton
        val loading = binding.loginProgress


        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            login.isEnabled = loginState.isDataValid

            if (loginState.addressError != null) {
                serverAddress.error = getString(loginState.addressError)
            }
            if (loginState.organizationError != null) {
                organization.error = getString(loginState.organizationError)
            }
            if (loginState.tokenError != null) {
                token.error = getString(loginState.tokenError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        })

        serverAddress.afterTextChanged {
            loginViewModel.loginDataChanged(
                serverAddress.text.toString(),
                organization.text.toString(),
                token.text.toString()
            )
        }

        organization.afterTextChanged {
            loginViewModel.loginDataChanged(
                serverAddress.text.toString(),
                organization.text.toString(),
                token.text.toString()
            )
        }

        token.afterTextChanged {
            loginViewModel.loginDataChanged(
                serverAddress.text.toString(),
                organization.text.toString(),
                token.text.toString()
            )
        }

        token.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    serverAddress.text.toString(),
                    organization.text.toString(),
                    token.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            serverAddress.text.toString(),
                            organization.text.toString(),
                            token.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(
                    serverAddress.text.toString(),
                    organization.text.toString(),
                    token.text.toString()
                )
            }
        }
        ///set values for the first time
        loginViewModel.loginDataChanged(
            serverAddress.text.toString(),
            organization.text.toString(),
            token.text.toString()
        )

    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val loginSucess = getString(R.string.login_sucess)
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            loginSucess,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}