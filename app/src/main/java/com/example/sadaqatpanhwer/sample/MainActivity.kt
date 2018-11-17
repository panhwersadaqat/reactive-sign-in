package com.example.sadaqatpanhwer.sample

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText

import com.jakewharton.rxbinding.widget.RxTextView

import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class MainActivity : AppCompatActivity() {
    private var signInButton: Button? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var checkEmail: TextInputLayout? = null
    private var checkPass: TextInputLayout? = null


    protected var compositeSubscription = CompositeSubscription()


    // region Member Variables
    private val pattern = android.util.Patterns.EMAIL_ADDRESS
    private var matcher: Matcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signInButton = findViewById<View>(R.id.signInButton) as Button
        emailEditText = findViewById<View>(R.id.email_et) as EditText
        passwordEditText = findViewById<View>(R.id.password_et) as EditText
        checkEmail = findViewById<View>(R.id.email_til) as TextInputLayout
        checkPass = findViewById<View>(R.id.password_til) as TextInputLayout


        val emailChangeObservable = RxTextView.textChanges(emailEditText!!)
        val passwordChangeObservable = RxTextView.textChanges(passwordEditText!!)

        // Checks for validity of the email input field

        val emailSubscription = emailChangeObservable
                .doOnNext { hideEmailError() }
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter { charSequence -> !TextUtils.isEmpty(charSequence) }
                .observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(object : Subscriber<CharSequence>() {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                    override fun onNext(charSequence: CharSequence) {
                        val isEmailValid = validateEmail(charSequence.toString())
                        if (!isEmailValid) {
                            showEmailError()
                        } else {
                            hideEmailError()
                        }
                    }
                })

        compositeSubscription.add(emailSubscription)


        // Checks for validity of the password input field

        val passwordSubscription = passwordChangeObservable
                .doOnNext { hidePasswordError() }
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter { charSequence -> !TextUtils.isEmpty(charSequence) }
                .observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(object : Subscriber<CharSequence>() {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                    override fun onNext(charSequence: CharSequence) {
                        val isPasswordValid = validatePassword(charSequence.toString())
                        if (!isPasswordValid) {
                            showPasswordError()
                        } else {
                            hidePasswordError()
                        }
                    }
                })

        compositeSubscription.add(passwordSubscription)


        // Checks for validity of both input fields for button chacnging

        val signInFieldsSubscription = Observable.combineLatest(emailChangeObservable, passwordChangeObservable) { email, password ->
            val isEmailValid = validateEmail(email.toString())
            val isPasswordValid = validatePassword(password.toString())

            isEmailValid && isPasswordValid
        }.observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(object : Observer<Boolean> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                    override fun onNext(validFields: Boolean?) {
                        if (validFields!!) {
                            enableSignIn()
                        } else {
                            disableSignIn()
                        }
                    }
                })

        compositeSubscription.add(signInFieldsSubscription)


    }

    // region Helper Methods
    private fun enableError(textInputLayout: TextInputLayout) {
        if (textInputLayout.childCount == 2)
            textInputLayout.getChildAt(1).visibility = View.VISIBLE
    }

    private fun disableError(textInputLayout: TextInputLayout) {
        if (textInputLayout.childCount == 2)
            textInputLayout.getChildAt(1).visibility = View.GONE
    }

    private fun validateEmail(email: String): Boolean {
        if (TextUtils.isEmpty(email))
            return false

        matcher = pattern.matcher(email)

        return matcher!!.matches()
    }

    private fun validatePassword(password: String): Boolean {
        return password.length > 5
    }

    private fun showEmailError() {
        enableError(checkEmail!!)
        checkEmail!!.isErrorEnabled = true
        checkEmail!!.error = getString(R.string.invalid_email)
    }

    private fun hideEmailError() {
        disableError(checkEmail!!)
        checkEmail!!.isErrorEnabled = false
        checkEmail!!.error = null
    }

    private fun showPasswordError() {
        enableError(checkPass!!)
        checkPass!!.isErrorEnabled = true
        checkPass!!.error = getString(R.string.invalid_password)
    }

    private fun hidePasswordError() {
        disableError(checkPass!!)
        checkPass!!.isErrorEnabled = false
        checkPass!!.error = null
    }


    private fun enableSignIn() {
        signInButton!!.isEnabled = true
        signInButton!!.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
    }

    private fun disableSignIn() {
        signInButton!!.isEnabled = false
        signInButton!!.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_500))
    }
    // endregion

}
