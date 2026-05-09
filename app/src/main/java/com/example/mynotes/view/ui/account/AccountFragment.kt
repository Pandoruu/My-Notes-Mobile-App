package com.example.mynotes.view.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mynotes.databinding.FragmentAccountBinding
import com.example.mynotes.di.AppContainer
import com.example.mynotes.presentation.account.AccountViewModel

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            AccountViewModel.Factory(
                observeCurrentUserIdUseCase = AppContainer.observeCurrentUserIdUseCase,
                observeUserByIdUseCase = AppContainer.observeUserByIdUseCase,
                observeAllNotesUseCase = AppContainer.observeAllNotesUseCase
            )
        )[AccountViewModel::class.java]

        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.tvFullNameValue.text = user?.fullName ?: "-"
            binding.tvUsernameValue.text = user?.username ?: "-"
            binding.tvEmailValue.text = user?.email ?: "-"
            binding.tvPhoneValue.text = user?.phone ?: "-"
        }

        viewModel.noteCount.observe(viewLifecycleOwner) { count ->
            binding.tvNoteCountValue.text = count.toString()
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

