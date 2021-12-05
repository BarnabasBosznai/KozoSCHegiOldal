package hu.bme.aut.android.kozoschegioldal.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import hu.bme.aut.android.kozoschegioldal.model.User
import hu.bme.aut.android.kozoschegioldal.service.NotificationFirebaseMessagingService

class AuthRepository(private var application: Application) {
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()
    private var loggedOutLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private var ownUser: MutableLiveData<User?> = MutableLiveData()

    init {
        if (firebaseAuth.currentUser != null) {
            userLiveData.postValue(firebaseAuth.currentUser)
            loggedOutLiveData.postValue(false)
            getUser()
        }
    }

    fun login(email: String, password: String) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    userLiveData.postValue(firebaseAuth.currentUser)
                    loggedOutLiveData.postValue(false)
                    getUser()
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            if (tokenTask.result != null && tokenTask.result != NotificationFirebaseMessagingService.token) {
                                val userDockRef = Firebase.firestore.collection("users").document(firebaseAuth.currentUser!!.uid)
                                userDockRef.get().addOnCompleteListener { documentTask ->
                                    if (documentTask.isSuccessful) {
                                        userDockRef.update("fcmToken", tokenTask.result).addOnCompleteListener { upd ->
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
                    }
                } else {
                    Toast.makeText(application.applicationContext, "Failed to login", Toast.LENGTH_SHORT).show()
                    Log.d("LoginTag", it.exception?.message.toString())
                }
            }
    }

    fun register(name: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = firebaseAuth.currentUser
                getUser()
                user!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("NameUpdateTag", "User name updated")
                        }
                    }
                FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                    if (tokenTask.isSuccessful) {
                        val newUser = User(user.uid, tokenTask.result!!)
                        Firebase.firestore.collection("users").document(user.uid).set(newUser/*hashMapOf(
                                "uid" to user.uid,
                                "fcm_token" to tokenTask.result,
                                "photo_url" to ""
                        )*/).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                userLiveData.postValue(user)
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
                Toast.makeText(application.applicationContext, "Failed to register. Please try again later!", Toast.LENGTH_SHORT).show()
                Log.d("RegisterTag", it.exception?.message.toString())
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        userLiveData.postValue(null)
        loggedOutLiveData.postValue(true)
        ownUser.postValue(null)
    }

    fun getUserLiveData() = userLiveData
    fun getLoggedOutLiveData() = loggedOutLiveData
    fun getOwnUserLiveData() = ownUser

    private fun getUser() {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users").document(uid).get()
                .addOnSuccessListener {
                    ownUser.postValue(it.toObject(User::class.java))
                }
        }
    }
}