package com.example.playgound

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class Authentication {

    fun googleSignIn(
        token: String,
        activity: Activity,
        resultContract: ActivityResultLauncher<Intent>
    ) {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)

        val signInIntent = googleSignInClient.signInIntent
        resultContract.launch(signInIntent)
    }


    fun handleFacebookAccessToken(
        token: AccessToken,
        auth: FirebaseAuth,
        activity: Activity,
        navController: NavController,
        context: Context
    ) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    navController.navigate(R.id.action_authFragment_to_loggedFragment)
                } else {
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    fun phoneSignIn(
        view: View,
        context: Context,
        auth: FirebaseAuth,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val phoneEditText = view.findViewById<EditText>(R.id.et_mobile)
        var number = phoneEditText.text.toString().trim()

        if (number.isNotEmpty()) {
            number = "+55$number"
            sendPhoneVerification(number, auth, activity, callbacks)
        } else {
            Toast.makeText(context, "phone empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendPhoneVerification(
        number: String,
        auth: FirebaseAuth,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}