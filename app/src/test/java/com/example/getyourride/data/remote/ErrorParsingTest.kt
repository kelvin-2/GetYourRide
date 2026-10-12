package com.example.getyourride.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ErrorParsingTest {

    @Test
    fun parsesBackendErrorField() {
        val body = """{"error":"Student already has a booking for this trip"}"""
        assertEquals("Student already has a booking for this trip", parseErrorMessage(body))
    }

    @Test
    fun fallsBackToMessageField() {
        assertEquals("Invalid email", parseErrorMessage("""{"message":"Invalid email"}"""))
    }

    @Test
    fun springDefaultBodyUsesMessageNotReasonPhrase() {
        val body = """{"timestamp":"x","status":400,"error":"Bad Request","message":"Seat limit reached","path":"/a"}"""
        assertEquals("Seat limit reached", parseErrorMessage(body))
    }

    @Test
    fun missingEmptyOrMalformedBodiesDoNotCrash() {
        assertNull(parseErrorMessage(null))
        assertNull(parseErrorMessage(""))
        assertNull(parseErrorMessage("   "))
        assertNull(parseErrorMessage("<html>oops</html>"))
        assertNull(parseErrorMessage("{not json"))
        assertNull(parseErrorMessage("[1,2]"))
        assertNull(parseErrorMessage("""{"error":{"nested":true}}"""))
    }

    @Test
    fun userMessagesAreFriendly() {
        assertEquals("Boom", ApiError.Server("Boom", 400).toUserMessage())
        assertEquals(
            "No internet connection. Please check your network and try again.",
            ApiError.Network.toUserMessage()
        )
        assertEquals(SESSION_EXPIRED_MESSAGE, ApiError.Unauthorized(null, true).toUserMessage())
        assertEquals("Taken", ApiError.Conflict("Taken").toUserMessage())
    }

    @Test
    fun fallbackOnlyAppliesWhenBackendSentNothing() {
        assertEquals("Mine", ApiError.Conflict(null).withFallbackMessage("Mine").toUserMessage())
        assertEquals("Theirs", ApiError.Conflict("Theirs").withFallbackMessage("Mine").toUserMessage())
    }
}