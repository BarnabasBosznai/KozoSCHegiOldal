package hu.bme.aut.android.kozoschegioldal.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentLoggedInBinding
import hu.bme.aut.android.kozoschegioldal.viewmodel.AuthViewModel

class LoggedInFragment : Fragment() {
    private var _binding: FragmentLoggedInBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.getLoggedOutLiveData().observe(this, { loggedOut ->
            if (loggedOut) {
                findNavController().navigate(LoggedInFragmentDirections.actionGlobalLoginFragment())
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoggedInBinding.inflate(inflater, container, false)

        authViewModel.getUserLiveData().observe(viewLifecycleOwner, { user ->
            if (user != null) {
                binding.etName.text = user.displayName

            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //FirebaseMessaging.getInstance().subscribeToTopic()

        /*PushNotification(NotificationData("Helle there!", "I have come online"),
            "eqOGZOBKSNCrGgbx7nmmS0:APA91bE5vcJ29c_RkQvDVBye2i5PaT3rrJoRs5Xxe2YIwWPN6z8G2E2EI78HTJFshdkvpDIZdOlSoUcoAcSiE3VzvY3JcG6wBu5xNTGzECGLVE1LF6KvJLquoR2McLcc4Pui2h6fLhGE")
            .also {
                tempSendNotification(it)
            }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*private fun tempSendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = NotificationRetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d("SendNotification", "Response: ${Gson().toJson(response.body())}")
            } else {
                Log.d("SendNotification", response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.d("SendNotification", e.toString())
        }
    }*/
}