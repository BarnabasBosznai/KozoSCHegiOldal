package hu.bme.aut.android.kozoschegioldal.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentRegisterBinding
import hu.bme.aut.android.kozoschegioldal.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.getUserLiveData().observe(this, { user ->
            if (user != null) {
                findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoggedInFragment())
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        setupValidation()

        binding.btnRegister.setOnClickListener {
            authViewModel.register(
                binding.etName.text.toString(),
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupValidation() {
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    binding.tilName.error = "Please enter your Name"
                } else {
                    binding.tilName.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                checkInputsForError()
            }
        })

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
                checkPasswordMatch()
                checkInputsForError()
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                if (password.isEmpty()) {
                    binding.tilConfirmPassword.error = "Please enter your Password"
                } else {
                    binding.tilConfirmPassword.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
                checkInputsForError()
            }
        })
    }

    private fun checkInputsForError() {
        binding.btnRegister.isEnabled = (
                   binding.etName.text!!.isNotEmpty()
                && binding.tilName.error == null
                && binding.etEmail.text!!.isNotEmpty()
                && binding.tilEmail.error == null
                && binding.etPassword.text!!.isNotEmpty()
                && binding.tilPassword.error == null
                && binding.etConfirmPassword.text!!.isNotEmpty()
                && binding.tilConfirmPassword.error == null)
    }

    private fun checkPasswordMatch() {
        if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
            binding.tilConfirmPassword.error = "Password and Confirm Password does not match"
        } else {
            binding.tilConfirmPassword.error = null
        }
    }
}