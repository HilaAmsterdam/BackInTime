//package com.example.backintime.ui.post
//
//import android.os.Bundle
//import android.text.format.DateFormat
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.navArgs
//import com.example.backintime.R
//import com.example.backintime.databinding.FragmentSelectedMemoryBinding
//import com.squareup.picasso.Picasso
//import java.util.*
//
//class SelectedMemoryFragment : Fragment() {
//
//    private var _binding: FragmentSelectedMemoryBinding? = null
//    private val binding get() = _binding
//
//    // שימוש ב-safeArgs לקבלת הנתונים
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentSelectedMemoryBinding.inflate(inflater, container, false)
//        return binding?.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // קבלת הקפסולה שהועברה דרך safeArgs
//        val capsule = args.timeCapsule
//
//        binding?.apply {
//            memoryTitle.text = capsule.title
//            memoryDescription.text = capsule.content
//            memoryEmail.text = capsule.creatorName
//
//            // עיצוב התאריך לפורמט קריא
//            val formattedDate = DateFormat.format("dd/MM/yy", Date(capsule.openDate))
//            memoryDate.text = formattedDate.toString()
//
//            if (capsule.imageUrl.isNotEmpty()) {
//                Picasso.get()
//                    .load(capsule.imageUrl)
//                    .placeholder(R.drawable.baseline_account_circle_24)
//                    .error(R.drawable.baseline_account_circle_24)
//                    .into(memoryImage)
//            } else {
//                memoryImage.setImageResource(R.drawable.baseline_account_circle_24)
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
