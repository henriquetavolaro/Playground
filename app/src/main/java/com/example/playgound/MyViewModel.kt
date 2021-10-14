package com.example.playgound

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val authentication: Authentication
) : ViewModel() {

    fun googleSignIn(
        token: String,
        activity: Activity,
        resultContract: ActivityResultLauncher<Intent>
    ) {
        viewModelScope.launch {
            authentication.googleSignIn(
                token, activity, resultContract
            )
        }
    }

    fun handleFacebookAccessToken(
        token: AccessToken,
        auth: FirebaseAuth,
        activity: Activity,
        navController: NavController,
        context: Context
    ) {
        viewModelScope.launch {
            authentication.handleFacebookAccessToken(
                token,
                auth,
                activity,
                navController,
                context
            )
        }
    }

    fun phoneSignIn(
        view: View,
        context: Context,
        auth: FirebaseAuth,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ){
        viewModelScope.launch {
            authentication.phoneSignIn(
                view,
                context,
                auth,
                activity,
                callbacks
            )
        }
    }


//    val analytics = Firebase.analytics
//
//    fun addClient(client: Client) {
//        viewModelScope.launch {
//            repository.execute(client)
//
//            analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
//                param(FirebaseAnalytics.Param.ITEM_ID, "10")
//                param(FirebaseAnalytics.Param.ITEM_NAME, "client")
//
//            }
//        }
//    }

}