// ─────────────────────────────────────────────────────────────────────────────
// SafeApiCall.kt
// Package: com.example.getyourride.data.remote
//
// PURPOSE — Wrap a Retrofit call so it NEVER throws to the caller (except
// coroutine cancellation, which must propagate) and always returns a
// ResultResource.
//
//   safeApiCall { api.login(request) }          // for Response<T> endpoints
//   safeCall    { api.getAllStops() }           // for endpoints returning T directly
//
// The error body is read exactly once and parsed with Gson. Both the
// {"error": "..."} shape and {"message": "..."} are understood, and a missing,
// empty or malformed body just falls back to a friendly default.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote

import android.util.Log
import com.example.getyourride.UserSession
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

private const val TAG = "ApiError"

/**
 * @param onEmptyBody used when the call succeeds with no body (e.g. a list endpoint
 *                    returning 200 + empty). Without it an empty body is an error.
 */
suspend fun <T> safeApiCall(
    onEmptyBody: (() -> T)? = null,
    call: suspend () -> Response<T>,
): ResultResource<T> {
    // Captured before the request: a 401 only means "session expired" if we had a session.
    val hadSession = UserSession.token != null
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            when {
                body != null -> ResultResource.Success(body)
                onEmptyBody != null -> ResultResource.Success(onEmptyBody())
                else -> ResultResource.Error(ApiError.Unknown(IllegalStateException("Empty response body")))
            }
        } else {
            ResultResource.Error(buildHttpError(response.code(), readOnce(response.errorBody()), hadSession))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "API call failed", e)
        ResultResource.Error(e.toApiError(hadSession))
    }
}

/** Same guarantees for endpoints declared as `suspend fun x(): T` (Retrofit throws HttpException). */
suspend fun <T> safeCall(call: suspend () -> T): ResultResource<T> {
    val hadSession = UserSession.token != null
    return try {
        ResultResource.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "API call failed", e)
        ResultResource.Error(e.toApiError(hadSession))
    }
}

/** Maps any throwable to an [ApiError]. */
fun Throwable.toApiError(): ApiError = toApiError(UserSession.token != null)

private fun Throwable.toApiError(hadSession: Boolean): ApiError = when (this) {
    is ApiException -> apiError
    is HttpException -> buildHttpError(code(), readOnce(response()?.errorBody()), hadSession)
    is SocketTimeoutException -> ApiError.Timeout
    is IOException -> ApiError.Network // UnknownHostException, ConnectException, SSL, etc.
    else -> ApiError.Unknown(this)
}

internal fun buildHttpError(code: Int, rawBody: String?, hadSession: Boolean): ApiError {
    val message = parseErrorMessage(rawBody)
    Log.w(TAG, "HTTP $code body=${rawBody?.take(300)}")
    return when {
        code == 401 -> {
            if (hadSession) SessionEvents.notifyExpired()
            ApiError.Unauthorized(message, sessionExpired = hadSession)
        }
        code == 403 -> ApiError.Forbidden(message)
        code == 404 -> ApiError.NotFound(message)
        code == 409 -> ApiError.Conflict(message)
        code == 408 || code == 504 -> ApiError.Timeout
        // 5xx text can be technical, so users always get the friendly line.
        code >= 500 -> ApiError.Server(defaultServerMessage(code), code)
        else -> ApiError.Server(message ?: defaultServerMessage(code), code)
    }
}

private fun defaultServerMessage(code: Int): String = when {
    code >= 500 -> "Something went wrong on our side. Please try again shortly."
    code == 400 || code == 422 -> "That didn't go through. Please check your details and try again."
    code == 429 -> "Too many attempts. Please wait a moment and try again."
    else -> "Something went wrong. Please try again."
}

/** Reads the error body a single time; never throws. */
private fun readOnce(body: ResponseBody?): String? = try {
    body?.string()
} catch (e: Exception) {
    null
}

/** Extracts the human message from {"error": "..."} / {"message": "..."}; null if absent or malformed. */
internal fun parseErrorMessage(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val element = JsonParser.parseString(raw)
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject

        fun text(key: String): String? = obj.get(key)
            ?.takeIf { it.isJsonPrimitive }
            ?.asString
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        // Spring's default error body has "error" = reason phrase ("Bad Request"),
        // so there the real text lives in "message".
        val isSpringDefault = obj.has("status") && (obj.has("timestamp") || obj.has("path"))
        if (isSpringDefault) text("message") else text("error") ?: text("message")
    } catch (e: Exception) {
        null
    }
}