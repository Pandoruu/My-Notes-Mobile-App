package com.example.mynotes.view.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentLoginBinding
import com.example.mynotes.di.AppContainer
import com.example.mynotes.presentation.auth.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel
    private val args: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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

        if (args.username.isNotBlank()) {
            binding.etUsername.setText(args.username)
        }
        if (args.password.isNotBlank()) {
            binding.etPassword.setText(args.password)
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe
            findNavController().navigate(R.id.action_loginFragment_to_notesNav)
            viewModel.clearLoginResult()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(username, password)
        }

        binding.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
