package cit.edu.vestil.worklog.ui.common

import cit.edu.vestil.worklog.data.model.ApiMessageResponse
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorParser {
    private val gson = Gson()

    fun getErrorMessage(response: Response<*>?, fallback: String): String {
        if (response == null) return fallback
        return try {
            val body = response.errorBody()?.string().orEmpty()
            if (body.isBlank()) {
                fallback
            } else {
                gson.fromJson(body, ApiMessageResponse::class.java)?.message?.takeIf { it.isNotBlank() }
                    ?: fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    fun getThrowableMessage(throwable: Throwable, fallback: String): String {
        return when (throwable) {
            is UnknownHostException -> "Cannot reach the server. Please check your internet connection."
            is SocketTimeoutException -> "The server took too long to respond. Please try again."
            is IOException -> "A network error occurred. Please try again."
            else -> throwable.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }
}
