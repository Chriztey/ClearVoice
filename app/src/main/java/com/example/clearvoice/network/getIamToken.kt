package com.example.clearvoice.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class IamTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("ims_user_id") val imsUserId: Long,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("expiration") val expiration: Long,
    @SerialName("scope") val scope: String
)

suspend fun getIamToken(apiKey: String): IamTokenResponse {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // <--- this line
            })
        }
    }

    return client.post("https://iam.cloud.ibm.com/identity/token") {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(FormDataContent(Parameters.build {
            append("grant_type", "urn:ibm:params:oauth:grant-type:apikey")
            append("apikey", apiKey)
        }))
    }.body()
}

