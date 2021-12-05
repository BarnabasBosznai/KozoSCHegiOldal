package hu.bme.aut.android.kozoschegioldal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import hu.bme.aut.android.kozoschegioldal.repository.AuthRepository

class AuthViewModel(private val app: Application) : AndroidViewModel(app) {
    private val authRepository: AuthRepository = AuthRepository(app)

    fun login(email: String, password: String) {
        authRepository.login(email, password)
    }

    fun register(name: String, email: String, password: String) {
        authRepository.register(name, email, password)
    }

    fun logout() {
        authRepository.logout()
    }

    fun getUserLiveData() = authRepository.getUserLiveData()
    fun getLoggedOutLiveData() = authRepository.getLoggedOutLiveData()
}