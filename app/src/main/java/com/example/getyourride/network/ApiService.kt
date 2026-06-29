package com.example.getyourride.network

import com.example.getyourride.data.DeleteDriverProfileRequest
import com.example.getyourride.data.DeleteDriverProfileResponse
import com.example.getyourride.data.DriverApplicationRequest
import com.example.getyourride.data.DriverApplicationResponse
import com.example.getyourride.data.DriverApplicationStatus
import com.example.getyourride.data.DriverDocumentInfo
import com.example.getyourride.data.DriverDocumentType
import com.example.getyourride.data.OfferRideRequest
import com.example.getyourride.data.OfferRideResponse
import com.example.getyourride.data.StudentProfileRequest
import com.example.getyourride.data.StudentProfileResponse
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

interface ApiService {
    fun createStudentProfile(request: StudentProfileRequest): ApiResult<StudentProfileResponse>

    fun submitDriverApplication(request: DriverApplicationRequest): ApiResult<DriverApplicationResponse>

    fun uploadDriverDocument(
        applicationId: String,
        documentType: DriverDocumentType,
        fileName: String,
        contentType: String,
        fileBytes: ByteArray
    ): ApiResult<DriverDocumentInfo>

    fun offerRide(request: OfferRideRequest): ApiResult<OfferRideResponse>

    fun deleteDriverProfile(request: DeleteDriverProfileRequest): ApiResult<DeleteDriverProfileResponse>
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()

    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : ApiResult<Nothing>()
}

class SpringBootApiService(
    private val baseUrl: String
) : ApiService {

    override fun createStudentProfile(
        request: StudentProfileRequest
    ): ApiResult<StudentProfileResponse> {
        return postJson(
            endpoint = "/api/students",
            body = request.toJson()
        ) { responseBody ->
            StudentProfileResponse(
                userId = responseBody.extractJsonValue("userId").orEmpty(),
                universityEmail = request.universityEmail,
                message = responseBody.extractJsonValue("message")
                    ?: "Student profile created successfully."
            )
        }
    }

    override fun submitDriverApplication(
        request: DriverApplicationRequest
    ): ApiResult<DriverApplicationResponse> {
        return postJson(
            endpoint = "/api/driver-applications",
            body = request.toJson()
        ) { responseBody ->
            DriverApplicationResponse(
                applicationId = responseBody.extractJsonValue("applicationId").orEmpty(),
                status = DriverApplicationStatus.PendingVerification
            )
        }
    }

    override fun uploadDriverDocument(
        applicationId: String,
        documentType: DriverDocumentType,
        fileName: String,
        contentType: String,
        fileBytes: ByteArray
    ): ApiResult<DriverDocumentInfo> {
        val boundary = "GetYourRideBoundary${System.currentTimeMillis()}"

        return try {
            val connection = openConnection(
                "/api/driver-applications/$applicationId/documents"
            ).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("Accept", "application/json")
            }

            DataOutputStream(connection.outputStream).use { output ->
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"documentType\"\r\n\r\n")
                output.writeBytes("${documentType.name}\r\n")

                output.writeBytes("--$boundary\r\n")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"${fileName.escapeMultipartFileName()}\"\r\n"
                )
                output.writeBytes("Content-Type: $contentType\r\n\r\n")
                output.write(fileBytes)
                output.writeBytes("\r\n")
                output.writeBytes("--$boundary--\r\n")
                output.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = connection.readBody(responseCode)

            if (responseCode in 200..299) {
                ApiResult.Success(
                    DriverDocumentInfo(
                        documentType = documentType,
                        originalFileName = fileName,
                        localUri = "",
                        cloudUrl = responseBody.extractJsonValue("cloudUrl").orEmpty()
                    )
                )
            } else {
                ApiResult.Error("Document upload failed: HTTP $responseCode $responseBody")
            }
        } catch (error: Exception) {
            ApiResult.Error("Document upload failed: ${error.message}", error)
        }
    }

    override fun offerRide(
        request: OfferRideRequest
    ): ApiResult<OfferRideResponse> {
        return postJson(
            endpoint = "/api/carpool-trips",
            body = request.toJson()
        ) { responseBody ->
            OfferRideResponse(
                tripId = responseBody.extractJsonValue("tripId").orEmpty(),
                message = responseBody.extractJsonValue("message")
                    ?: "Ride posted successfully."
            )
        }
    }

    override fun deleteDriverProfile(
        request: DeleteDriverProfileRequest
    ): ApiResult<DeleteDriverProfileResponse> {
        return postJson(
            endpoint = "/api/driver-profiles/me/deactivate",
            body = request.toJson()
        ) { responseBody ->
            DeleteDriverProfileResponse(
                driverProfileId = responseBody.extractJsonValue("driverProfileId").orEmpty(),
                status = DriverApplicationStatus.Rejected,
                message = responseBody.extractJsonValue("message")
                    ?: "Driver profile deactivated."
            )
        }
    }

    private fun <T> postJson(
        endpoint: String,
        body: String,
        parser: (String) -> T
    ): ApiResult<T> {
        return try {
            val connection = openConnection(endpoint).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseBody = connection.readBody(responseCode)

            if (responseCode in 200..299) {
                ApiResult.Success(parser(responseBody))
            } else {
                ApiResult.Error("Request failed: HTTP $responseCode $responseBody")
            }
        } catch (error: Exception) {
            ApiResult.Error("Request failed: ${error.message}", error)
        }
    }

    private fun openConnection(endpoint: String): HttpURLConnection {
        return URL(baseUrl.trimEnd('/') + endpoint).openConnection() as HttpURLConnection
    }

    private fun HttpURLConnection.readBody(responseCode: Int): String {
        val stream = if (responseCode in 200..299) {
            inputStream
        } else {
            errorStream
        }

        return stream?.bufferedReader()?.use { reader ->
            reader.readText()
        }.orEmpty()
    }
}

private fun StudentProfileRequest.toJson(): String {
    return """
    {
      "firstName":"${firstName.escapeJson()}",
      "surname":"${surname.escapeJson()}",
      "studentNumber":"${studentNumber.escapeJson()}",
      "contactNumber":"${contactNumber.escapeJson()}",
      "universityEmail":"${universityEmail.escapeJson()}",
      "password":"${password.escapeJson()}",
      "nsfasFunded":$nsfasFunded
    }
    """.trimIndent()
}

private fun DriverApplicationRequest.toJson(): String {
    val documentJson = documents.joinToString(separator = ",") { document ->
        """
        {
          "documentType":"${document.documentType.name}",
          "originalFileName":"${document.originalFileName.escapeJson()}",
          "cloudUrl":"${document.cloudUrl.escapeJson()}"
        }
        """.trimIndent()
    }

    return """
    {
      "surname":"${personalInfo.surname.escapeJson()}",
      "firstName":"${personalInfo.firstName.escapeJson()}",
      "studentNumber":"${personalInfo.studentNumber.escapeJson()}",
      "contactNumber":"${personalInfo.contactNumber.escapeJson()}",
      "universityEmail":"${personalInfo.universityEmail.escapeJson()}",
      "password":"${personalInfo.password.escapeJson()}",
      "vehicleRegistrationNumber":"${vehicleInfo.vehicleRegistrationNumber.escapeJson()}",
      "vehicleMake":"${vehicleInfo.vehicleMake.escapeJson()}",
      "vehicleModel":"${vehicleInfo.vehicleModel.escapeJson()}",
      "vehicleColour":"${vehicleInfo.vehicleColour.escapeJson()}",
      "seatingCapacity":${vehicleInfo.seatingCapacity},
      "status":"${status.name}",
      "documents":[$documentJson]
    }
    """.trimIndent()
}

private fun OfferRideRequest.toJson(): String {
    return """
    {
      "pickupLocation":"${pickupLocation.escapeJson()}",
      "destination":"${destination.escapeJson()}",
      "rideDate":"${rideDate.escapeJson()}",
      "rideTime":"${rideTime.escapeJson()}",
      "availableSeats":$availableSeats,
      "farePerSeat":$farePerSeat
    }
    """.trimIndent()
}

private fun DeleteDriverProfileRequest.toJson(): String {
    return """
    {
      "reason":"${reason.escapeJson()}"
    }
    """.trimIndent()
}

private fun String.escapeJson(): String {
    return replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

private fun String.escapeMultipartFileName(): String {
    return replace("\\", "_")
        .replace("\"", "_")
        .replace("\r", "")
        .replace("\n", "")
}

private fun String.extractJsonValue(key: String): String? {
    val pattern = """"$key"\s*:\s*"([^"]*)"""".toRegex()
    return pattern.find(this)?.groupValues?.getOrNull(1)
}