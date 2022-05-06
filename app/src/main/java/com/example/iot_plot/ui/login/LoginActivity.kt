package com.example.iot_plot.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.iot_plot.MainActivity
import com.example.iot_plot.databinding.ActivityLoginBinding

import com.example.iot_plot.R
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.SystemClock
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class LoginActivity : AppCompatActivity() {
    private val sharedPrefFile = "kotlinsharedpreference"

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateFieldsFromPreferences()

        val serverAddress = binding.apiAddressField
        val organization = binding.organizationField
        val token = binding.tokenField
        val login = binding.loginButton


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

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
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
                updatePreferences()
                showLoadingScreen(true)

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
        val loginSuccess = getString(R.string.login_sucess)
        Toast.makeText(
            applicationContext,
            loginSuccess,
            Toast.LENGTH_LONG
        ).show()
        showLoadingScreen(false)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
        showLoadingScreen(false)
    }

    private fun showLoadingScreen(show: Boolean){
        val loadingScreen = binding.loginProgress
        val loginScreen = binding.loginWindow
        if(show){
            loginScreen.visibility = View.GONE
            loadingScreen.visibility = View.VISIBLE
        }else{
            loginScreen.visibility = View.VISIBLE
            loadingScreen.visibility = View.GONE
        }
    }

    private fun updateFieldsFromPreferences(){
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        binding.apiAddressField.setText(sharedPreferences.getString("PREF_API_ADDRESS",""))
        binding.tokenField.setText(sharedPreferences.getString("PREF_TOKEN",""))
        binding.organizationField.setText(sharedPreferences.getString("PREF_ORGANIZATION",""))
        binding.saveLoginCheck.isChecked = sharedPreferences.getBoolean("PREF_CHECK",false)
    }

    private fun updatePreferences(){
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)

        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        if(binding.saveLoginCheck.isChecked){
            editor.putString("PREF_API_ADDRESS", binding.apiAddressField.getText().toString())
            editor.putString("PREF_TOKEN", binding.tokenField.getText().toString())
            editor.putString("PREF_ORGANIZATION", binding.organizationField.getText().toString())
            editor.putBoolean("PREF_CHECK", binding.saveLoginCheck.isChecked)
            editor.apply()
        }else{
            editor.putString("PREF_API_ADDRESS", "")
            editor.putString("PREF_TOKEN", "")
            editor.putString("PREF_ORGANIZATION", "")
            editor.putBoolean("PREF_CHECK", false)
            editor.apply()
        }
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