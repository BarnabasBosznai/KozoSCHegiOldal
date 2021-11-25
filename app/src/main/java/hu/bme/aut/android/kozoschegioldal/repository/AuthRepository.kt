package hu.bme.aut.android.kozoschegioldal.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class AuthRepository(private var application: Application) {
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userLiveData: MutableLiveData<FirebaseUser> = MutableLiveData()
    private var loggedOutLiveData: MutableLiveData<Boolean> = MutableLiveData()

    init {
        if (firebaseAuth.currentUser != null) {
            userLiveData.postValue(firebaseAuth.currentUser)
            loggedOutLiveData.postValue(false)
        }
    }

    fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    userLiveData.postValue(firebaseAuth.currentUser)
                    loggedOutLiveData.postValue(false)

                    Firebase.firestore.collection("users").document(firebaseAuth.currentUser!!.uid).get()
                        .addOnCompleteListener { doc ->
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    if (doc.isSuccessful && doc.result!!.get("fcm_token") != tokenTask.result) {
                                        Firebase.firestore.collection("users")
                                            .document(firebaseAuth.currentUser!!.uid)
                                            .update("fcm_token", tokenTask.result)
                                            .addOnCompleteListener { upd ->
                                                if (upd.isSuccessful) {
                                                    Log.d("TokenUpdate", "Successful")
                                                } else {
                                                    Log.d("TokenUpdate", "Error")
                                                }
                                            }
                                    }
                                }
                            }

                        }
                } else {
                    Toast.makeText(application.applicationContext, "Login failure: " + it.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun register(name: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("NameUpdateTag", "User name updated")
                            }
                        }
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            Firebase.firestore.collection("users").document(user.uid).set(hashMapOf(
                                "uid" to user.uid,
                                "fcm_token" to tokenTask.result,
                                "photo_url" to ""
                            )).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("UserCreation", "Successful")
                                } else {
                                    Log.d("UserCreation", "Error")
                                }
                            }
                        }
                    }

                    userLiveData.postValue(user)
                    loggedOutLiveData.postValue(false)
                } else {
                    Toast.makeText(application.applicationContext, "Registration failure: " + it.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
        userLiveData.postValue(null)
        loggedOutLiveData.postValue(true)
    }

    fun getUserLiveData() = userLiveData
    fun getLoggedOutLiveData() = loggedOutLiveData
}