package com.example.playgound.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.playgound.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerificationFragment : Fragment() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verification, container, false)
    }

    private val storedVerificationId : VerificationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val buttonVerify = view.findViewById<Button>(R.id.button_verify)
        val inputCode = view.findViewById<EditText>(R.id.et_code)

        buttonVerify.setOnClickListener {
            val code = inputCode.text.toString().trim()

            if(code.isNotEmpty()){
                val credential  =
                    PhoneAuthCredential.zzc(storedVerificationId.verificationId, code)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(context, "Enter code", Toast.LENGTH_SHORT).show()
            }
        }


    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAGTESTPHONE", "signInWithCredential:success")
                    findNavController().navigate(R.id.action_verificationFragment_to_loggedFragment)
                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAGTESTPHONE", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context, "code invalid", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }


}