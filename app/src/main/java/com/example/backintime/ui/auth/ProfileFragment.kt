package com.example.backintime.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.backintime.Model.FirebaseModel
import com.example.backintime.R
import com.example.backintime.activities.MainActivity
import com.example.backintime.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val firebaseModel = FirebaseModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // קבלת המשתמש הנוכחי מ-Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // הצגת אימייל המשתמש
            binding.profileEmailEdit.setText(currentUser.email)
            // הצגת שם המשתמש אם קיים, אחרת טקסט ברירת מחדל
            binding.accountUsername.text = currentUser.displayName ?: "משתמש ללא שם"
        }

        // מעבר למסך עריכת פרופיל (כפתור עריכה)
        binding.goToEditProfileFab.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // כפתור התנתקות: מתנתק מהחשבון ומעביר חזרה ל-MainActivity (שבו מופיע ה-home fragment)
        binding.logoutButton.setOnClickListener {
            firebaseModel.logoutUser()
            Toast.makeText(requireContext(), "התנתקת מהמערכת", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // כפתור מחיקת חשבון: מנסה למחוק את המשתמש מה-Firebase ומעביר חזרה ל-MainActivity
        binding.deleteAccountFab.setOnClickListener {
            currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "חשבונך נמחק", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "שגיאה במחיקת החשבון", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
