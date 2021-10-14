package com.example.playgound.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.facebook.*
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.app.Activity

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.playgound.*
import com.example.playgound.R
import com.facebook.login.LoginManager


@AndroidEntryPoint
class AuthFragment : Fragment() {

    private val viewModel: MyViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var facebookCallback: CallbackManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_auth, container, false)

    }

    override fun onStart() {
        super.onStart()
        // CHECK CURRENT USER
        val currentUser = auth.currentUser
        if (currentUser != null) {
            findNavController().navigate(R.id.action_authFragment_to_loggedFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonSend = view.findViewById<Button>(R.id.button_send)

        val googleSignInButton = view.findViewById<SignInButton>(R.id.button_google)


        //GOOGLE SIGN IN

        googleSignInButton.setOnClickListener {
            viewModel
                .googleSignIn(
                    getString(R.string.default_web_client_id),
                    requireActivity(),
                    resultContract
                )
        }
        //PHONE SIGN IN

        buttonSend.setOnClickListener {
            viewModel.phoneSignIn(
                requireView(),
                requireContext(),
                auth,
                requireActivity(),
                callbacks
            )
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("TAGTESTPHONE", "onVerificationCompleted:$credential")
                findNavController().navigate(R.id.action_authFragment_to_loggedFragment)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                Log.w("TAGTESTPHONE", "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.d("TAGTESTPHONE", "invalid request")
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.d("TAGTESTPHONE", "The SMS quota for the project has been exceeded")
                }
                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                Log.d("TAGTESTPHONE", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                val action = AuthFragmentDirections.actionAuthFragmentToVerificationFragment(
                    storedVerificationId
                )
                findNavController().navigate(action)
            }
        }


        // FACEBOOK LOGIN
        val buttonFacebookLogin = view.findViewById<LoginButton>(R.id.button_facebook)

        facebookCallback = CallbackManager.Factory.create()

        buttonFacebookLogin.setOnClickListener { LoginManager.getInstance() }

        buttonFacebookLogin.registerCallback(facebookCallback, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                viewModel
                    .handleFacebookAccessToken(
                        loginResult.accessToken,
                        auth,
                        requireActivity(),
                        findNavController(),
                        context!!
                    )
            }

            override fun onCancel() {
                Log.d("TAG", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d("TAG", "facebook:onError", error)
            }
        })
    }

    //FACEBOOK
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        facebookCallback.onActivityResult(requestCode, resultCode, data)
    }

    // GOOGLE SIGN IN
    private val resultContract = registerForActivityResult(StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("TAG", "signInWithCredential:success")
                val user = account.displayName
                Log.d("TAG", account.idToken!!.toString())
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                findNavController().navigate(R.id.action_authFragment_to_loggedFragment)
            } catch (e: ApiException) {
                Log.d("TAG", "signInWithCredential:failure", task.exception)
            }
        }
    }
}