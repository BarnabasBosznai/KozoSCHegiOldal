package hu.bme.aut.android.kozoschegioldal.interfaces

interface Callback {
    fun onSuccess()
    fun onFailure(t: Throwable)
}