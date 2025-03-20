package com.example.backintime.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.backintime.Model.FirebaseModel
import com.example.backintime.Model.SyncManager
import com.example.backintime.R
import com.example.backintime.activities.SecondActivity
import com.example.backintime.viewModel.ProgressViewModel
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {
    private val firebaseModel = FirebaseModel()
    private val progressViewModel: ProgressViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = view.findViewById<EditText>(R.id.emailInput)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton = view.findViewById<MaterialButton>(R.id.loginBtn)
        val goToRegisterFragment = view.findViewById<MaterialButton>(R.id.signUpClickable)

        loginButton.setOnClickListener {
            loginButton.isEnabled = false
            progressViewModel.setLoading(true)
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()


            if (email.isNotBlank() && password.isNotBlank()) {
                firebaseModel.loginUser(email, password) { success, errorMessage ->
                    progressViewModel.setLoading(false)
                    if (!isAdded) return@loginUser
                    val safeContext = context ?: return@loginUser

                    if (success) {
                        Toast.makeText(safeContext, "You have successfully connected!", Toast.LENGTH_SHORT).show()

                        SyncManager.listenFirebaseDataToRoom(safeContext)

                        val intent = Intent(safeContext, SecondActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    } else {
                        loginButton.isEnabled = true
                        Toast.makeText(safeContext, "error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                loginButton.isEnabled = true
                Toast.makeText(requireContext(), " Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        goToRegisterFragment.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
