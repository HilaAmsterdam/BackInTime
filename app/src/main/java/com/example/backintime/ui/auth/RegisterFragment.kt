package com.example.backintime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.backintime.model.firebase.FirebaseModel
import com.google.android.material.button.MaterialButton

class RegisterFragment : Fragment() {
    private val firebaseModel = FirebaseModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = view.findViewById<EditText>(R.id.emailInput)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordInput)
        val registerButton = view.findViewById<MaterialButton>(R.id.registerButton)
        val goToLoginFragment = view.findViewById<MaterialButton>(R.id.FromRegisterToLogInBtn)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseModel.registerUser(email, password) { success, errorMessage ->
                    if (success) {
                        Toast.makeText(requireContext(), "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    } else {
                        Toast.makeText(requireContext(), "שגיאה: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show()
            }
        }

        goToLoginFragment.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}
