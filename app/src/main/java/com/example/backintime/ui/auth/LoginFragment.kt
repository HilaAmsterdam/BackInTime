package com.example.backintime.ui.auth;


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.backintime.Model.FirebaseModel
import com.example.backintime.R
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {
    private val firebaseModel = FirebaseModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = view.findViewById<EditText>(R.id.emailInput)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton = view.findViewById<MaterialButton>(R.id.loginBtn)
        val goToRegisterFragment = view.findViewById<MaterialButton>(R.id.signUpClickable)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotBlank() && password.isNotBlank()) {
                firebaseModel.loginUser(email, password) { success, errorMessage ->
                    if (success) {
                        Toast.makeText(requireContext(), "התחברת בהצלחה!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                    } else {
                        Toast.makeText(requireContext(), "שגיאה: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show()
            }
        }

        goToRegisterFragment.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
