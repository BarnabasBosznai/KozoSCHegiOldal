package hu.bme.aut.android.kozoschegioldal.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentLoginBinding
import hu.bme.aut.android.kozoschegioldal.viewmodel.AuthViewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.getOwnUserLiveData().observe(this, { user ->
            if (user != null) {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoggedInFragment(user))
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupValidation()

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        binding.btnLogin.setOnClickListener {
            authViewModel.login(binding.etEmail.text.toString(), binding.etPassword.text.toString())
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.btnLogin.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupValidation() {
        binding.btnLogin.isEnabled = false

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString()
                if (email.isEmpty()) {
                    binding.tilEmail.error = "Please enter your Email"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tilEmail.error = "Invalid Email"
                } else {
                    binding.tilEmail.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                checkInputsForError()
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    binding.tilPassword.error = "Please enter your Password"
                } else {
                    binding.tilPassword.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                checkInputsForError()
            }
        })
    }

    private fun checkInputsForError() {
        binding.btnLogin.isEnabled = (
                   binding.etEmail.text!!.isNotEmpty()
                && binding.tilEmail.error == null
                && binding.etPassword.text!!.isNotEmpty()
                && binding.tilPassword.error == null)
    }
}