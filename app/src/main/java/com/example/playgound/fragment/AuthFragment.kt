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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.playgound.Client
import com.example.playgound.MyViewModel
import com.example.playgound.R
import com.example.playgound.RC_SIGN_IN
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
        if(currentUser != null) {
            findNavController().navigate(R.id.action_authFragment_to_loggedFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        val button = view.findViewById<Button>(R.id.button)
        val buttonSend = view.findViewById<Button>(R.id.button_send)

        button.setOnClickListener {
            val client = Client(
                id = "id2",
                name = "client1",
                photoUrl = "photo1",
                email = "email1"
            )
            viewModel.addClient(client)
        }

        val googleSignInButton = view.findViewById<SignInButton>(R.id.button_google)

        //GOOGLE SIGN IN
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)


        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


        //PHONE SIGN IN

        buttonSend.setOnClickListener {
            phoneSignIn()
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                  Log.d("TAGTESTPHONE", "onVerificationCompleted:$credential")
//                signInWithPhoneAuthCredential(credential)
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

                val action = AuthFragmentDirections.actionAuthFragmentToVerificationFragment(storedVerificationId)
                findNavController().navigate(action)
            }
        }



        // FACEBOOK LOGIN
        val buttonFacebookLogin = view.findViewById<LoginButton>(R.id.button_facebook)

//        buttonFacebookLogin.setReadPermissions("email", "public_profile")
        facebookCallback = CallbackManager.Factory.create()

        buttonFacebookLogin.registerCallback(facebookCallback, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("TAGTESTFACEEBOOK", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("TAGTESTFACEEBOOK", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d("TAGTESTFACEEBOOK", "facebook:onError", error)
            }
        })
    }

    //FACEBOOK SIGN IN
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("TAGTESTFACEEBOOK", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAGTESTFACEBOOK", "signInWithCredential:success")
                    val user = auth.currentUser
                    findNavController().navigate(R.id.action_authFragment_to_loggedFragment)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("TAGTESTFACEBOOK", "signInWithCredential:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }
    }

    // PHONE SIGN IN
    private fun phoneSignIn(){
        val phoneEditText = requireView().findViewById<EditText>(R.id.et_mobile)
        var number = phoneEditText.text.toString().trim()

        if(number.isNotEmpty()){
            number = "+55$number"
            sendPhoneVerification(number)
        } else {
            Toast.makeText(context, "phone empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendPhoneVerification(number: String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // GOOGLE SIGN IN
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)!!
//                Log.d("TAGTEST", "signInWithCredential:success")
//                val user = account.displayName
//                Log.d("TAGTEST", account.idToken!!.toString())
//                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//                auth.signInWithCredential(credential)
//                findNavController().navigate(R.id.action_authFragment_to_loggedFragment)
//            } catch (e: ApiException){
//                Log.d("TAGTEST", "signInWithCredential:failure", task.exception)
//            }
//
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        facebookCallback.onActivityResult(requestCode, resultCode, data)
    }


}