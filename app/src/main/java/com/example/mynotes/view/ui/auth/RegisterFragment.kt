package com.example.mynotes.view.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mynotes.databinding.FragmentRegisterBinding
import com.example.mynotes.di.AppContainer
import com.example.mynotes.presentation.auth.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            AuthViewModel.Factory(
                observeCurrentUserIdUseCase = AppContainer.observeCurrentUserIdUseCase,
                loginUseCase = AppContainer.loginUseCase,
                registerUseCase = AppContainer.registerUseCase
            )
        )[AuthViewModel::class.java]

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe
            val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment(
                username = user.username,
                password = user.password
            )
            findNavController().navigate(action)
            viewModel.clearRegisterResult()
        }

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            viewModel.register(fullName, email, phone, username, password)
        }

        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

