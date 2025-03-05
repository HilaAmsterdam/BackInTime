package com.example.backintime.ui.post

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.backintime.R
import com.example.backintime.databinding.FragmentSelectedMemoryBinding

class SelectedMemoryFragment : Fragment() {
    private var _binding: FragmentSelectedMemoryBinding? = null
    private val binding get() = _binding
    private val viewModel: SelectedMemoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedMemoryBinding.inflate(inflater, container, false)
        return _binding?.root ?: throw IllegalStateException("FragmentSelectedMemoryBinding is null")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.goToEditMemoryFab?.setOnClickListener {
            viewModel.onEditMemoryClicked()
        }

        val observe =
            viewModel.navigateToEditMemory.observe(viewLifecycleOwner, Observer { shouldNavigate ->
                if (shouldNavigate) {
                    findNavController().navigate(R.id.action_selectedMemoryFragment_to_editMemoryFragment)
                    viewModel.onEditMemoryNavigated()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}