package hu.bme.aut.android.kozoschegioldal.notification

import hu.bme.aut.android.kozoschegioldal.constant.Constants.Companion.CONTENT_TYPE
import hu.bme.aut.android.kozoschegioldal.constant.Constants.Companion.FCM_SERVER_KEY
import hu.bme.aut.android.kozoschegioldal.model.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {
    @Headers("Authorization: key=$FCM_SERVER_KEY", "Content-Type: $CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>
}