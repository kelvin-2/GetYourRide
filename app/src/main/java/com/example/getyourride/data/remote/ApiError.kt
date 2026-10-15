// ─────────────────────────────────────────────────────────────────────────────
// ApiError.kt
// Package: com.example.getyourride.data.remote
//
// PURPOSE — One typed description of "what went wrong with an API call", plus
// the single mapper that turns it into a short, friendly sentence for the user.
//
// Nothing here shows raw HTTP codes, stack traces or exception class names.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote

const val SESSION_EXPIRED_MESSAGE = "Your session expired. Please log in again."

sealed class ApiError {

    /** Backend answered with an error. [message] comes from the {"error": "..."} body when present. */
    data class Server(val message: String, val code: Int, val errorCode: String? = null) : ApiError()

    /** 401. [sessionExpired] is true when a logged-in user's JWT was rejected. */
    data class Unauthorized(
        val serverMessage: String? = null,
        val sessionExpired: Boolean = false,
    ) : ApiError()

    /** 403 */
    data class Forbidden(val serverMessage: String? = null) : ApiError()

    /** 404 */
    data class NotFound(val serverMessage: String? = null) : ApiError()

    /** 409 — e.g. duplicate booking / account that already exists. */
    data class Conflict(val serverMessage: String? = null) : ApiError()

    /** No internet / host unreachable. */
    object Network : ApiError()

    /** Request timed out. */
    object Timeout : ApiError()

    /** Anything else (malformed response, unexpected exception...). */
    data class Unknown(val cause: Throwable? = null) : ApiError()
}

/** Short, human-friendly message. Backend messages are shown as-is. */
fun ApiError.toUserMessage(): String = when (this) {
    is ApiError.Server -> message
    is ApiError.Unauthorized -> when {
        sessionExpired -> SESSION_EXPIRED_MESSAGE
        !serverMessage.isNullOrBlank() -> serverMessage
        else -> "Please log in to continue."
    }
    is ApiError.Forbidden -> serverMessage?.takeIf { it.isNotBlank() }
        ?: "You don't have permission to do that."
    is ApiError.NotFound -> serverMessage?.takeIf { it.isNotBlank() }
        ?: "We couldn't find what you were looking for."
    is ApiError.Conflict -> serverMessage?.takeIf { it.isNotBlank() }
        ?: "That conflicts with something that already exists. Please check and try again."
    ApiError.Network -> "No internet connection. Please check your network and try again."
    ApiError.Timeout -> "The request took too long. Please try again."
    is ApiError.Unknown -> "Something went wrong. Please try again."
}

/**
 * Lets a call site supply a context-specific sentence for when the backend sent
 * no message of its own (e.g. a 409 on booking). A backend message always wins.
 */
fun ApiError.withFallbackMessage(fallback: String): ApiError = when (this) {
    is ApiError.Unauthorized ->
        if (!sessionExpired && serverMessage.isNullOrBlank()) copy(serverMessage = fallback) else this
    is ApiError.Forbidden -> if (serverMessage.isNullOrBlank()) copy(serverMessage = fallback) else this
    is ApiError.NotFound -> if (serverMessage.isNullOrBlank()) copy(serverMessage = fallback) else this
    is ApiError.Conflict -> if (serverMessage.isNullOrBlank()) copy(serverMessage = fallback) else this
    else -> this
}

/** Exception that carries an [ApiError]; its message is already user-friendly. */
class ApiException(val apiError: ApiError) : Exception(apiError.toUserMessage())

/** For messages we wrote ourselves for the user (e.g. "No shuttle available..."). */
class UserMessageException(message: String) : Exception(message)

/** Friendly text for any throwable, safe to put straight on screen. */
fun Throwable.userFacingMessage(): String = when (this) {
    is ApiException -> message ?: apiError.toUserMessage()
    is UserMessageException -> message ?: ApiError.Unknown().toUserMessage()
    else -> toApiError().toUserMessage()
}