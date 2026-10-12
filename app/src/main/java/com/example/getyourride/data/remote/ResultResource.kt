// ─────────────────────────────────────────────────────────────────────────────
// ResultResource.kt
// Package: com.example.getyourride.data.remote
//
// PURPOSE — Success / Error result returned by safeApiCall { }.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote

sealed class ResultResource<out T> {
    data class Success<out T>(val data: T) : ResultResource<T>()
    data class Error(val error: ApiError) : ResultResource<Nothing>()
}

/** Bridge to kotlin.Result; the failure carries an [ApiException] with a friendly message. */
fun <T> ResultResource<T>.toResult(): Result<T> = when (this) {
    is ResultResource.Success -> Result.success(data)
    is ResultResource.Error -> Result.failure(ApiException(error))
}

/** For repositories that already signal failure by throwing. */
fun <T> ResultResource<T>.getOrThrow(): T = when (this) {
    is ResultResource.Success -> data
    is ResultResource.Error -> throw ApiException(error)
}

inline fun <T, R> ResultResource<T>.map(transform: (T) -> R): ResultResource<R> = when (this) {
    is ResultResource.Success -> ResultResource.Success(transform(data))
    is ResultResource.Error -> this
}

fun <T> ResultResource<T>.withFallbackMessage(fallback: String): ResultResource<T> = when (this) {
    is ResultResource.Success -> this
    is ResultResource.Error -> ResultResource.Error(error.withFallbackMessage(fallback))
}