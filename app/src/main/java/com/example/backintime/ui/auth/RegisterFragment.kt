package com.example.backintime.ui.auth

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.backintime.Model.FirebaseModel
import com.example.backintime.Model.SyncManager
import com.example.backintime.R
import com.example.backintime.activities.SecondActivity
import com.example.backintime.databinding.FragmentRegisterBinding
import com.example.backintime.utils.CloudinaryHelper
import com.example.backintime.viewModel.ProgressViewModel
import java.io.File

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding

    private val firebaseModel = FirebaseModel()
    private var capturedImageUri: Uri? = null
    private val progressViewModel: ProgressViewModel by activityViewModels()


    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        binding?.let { safeBinding ->
            if (success) {
                capturedImageUri?.let { uri ->
                    safeBinding.profileImage.setImageURI(uri)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding?.let { safeBinding ->
            if (uri != null) {
                capturedImageUri = uri
                safeBinding.profileImage.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeBinding = binding ?: return

        val emailEditText = safeBinding.emailInput
        val passwordEditText = safeBinding.passwordInput
        val registerButton = safeBinding.registerButton
        val goToLoginFragment = safeBinding.FromRegisterToLogInBtn
        val uploadImageButton = safeBinding.uploadImageButton

        uploadImageButton.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")
            AlertDialog.Builder(requireContext())
                .setTitle("Choose Image Source")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val imageFile = createImageFile()
                            capturedImageUri = FileProvider.getUriForFile(
                                requireContext(),
                                "com.example.backintime.fileprovider",
                                imageFile
                            )
                            takePictureLauncher.launch(capturedImageUri)
                        }
                        1 -> {
                            pickImageLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }

        registerButton.setOnClickListener {
            registerButton.isEnabled = false
            progressViewModel.setLoading(true)
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (capturedImageUri != null) {
                    capturedImageUri?.let { uri ->
                        CloudinaryHelper(requireContext()).uploadImage(uri, onSuccess = { imageUrl ->
                            firebaseModel.registerUser(email, password, imageUrl) { success, errorMessage ->
                                if (success) {
                                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                                    SyncManager.listenFirebaseDataToRoom(requireContext())
                                    val intent = Intent(requireContext(), SecondActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                } else {
                                    registerButton.isEnabled = true
                                    Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }, onFailure = { error ->
                            registerButton.isEnabled = true
                            Toast.makeText(requireContext(), "Image upload error: $error", Toast.LENGTH_SHORT).show()
                        })
                    }
                } else {
                    firebaseModel.registerUser(email, password, null) { success, errorMessage ->
                        if (success) {
                            Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                            SyncManager.listenFirebaseDataToRoom(requireContext())
                            val intent = Intent(requireContext(), SecondActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        } else {
                            Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                            registerButton.isEnabled = true
                            progressViewModel.setLoading(false)
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                registerButton.isEnabled = true
                progressViewModel.setLoading(false)
            }
        }

        goToLoginFragment.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
