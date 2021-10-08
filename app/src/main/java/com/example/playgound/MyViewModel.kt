package com.example.playgound

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: AddClient,
    private val auth: FirebaseAuth
) : ViewModel() {

    val analytics = Firebase.analytics

    fun addClient(client: Client) {
        viewModelScope.launch {
            repository.execute(client)

            analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "10")
                param(FirebaseAnalytics.Param.ITEM_NAME, "client")

            }
        }
    }

    fun firebaseAuthWithGoogle(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("TAGTEST", "signInWithCredential:success")
            val user = account.displayName
            Log.d("TAGTEST", account.idToken!!.toString())
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
        } catch (e: ApiException){
            Log.d("TAGTEST", "signInWithCredential:failure", task.exception)
        }
    }
}