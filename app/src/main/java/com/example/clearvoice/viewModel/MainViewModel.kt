package com.example.clearvoice.viewModel

import ScenarioItem
import WatsonMessage
import WatsonRequest
import WatsonResponse
import WrappedResponse
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clearvoice.network.getIamToken
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.collections.listOf

class MainViewModel : ViewModel() {

    private val _scenarios = MutableStateFlow<List<ScenarioItem>>(emptyList())
    private val _aiResponse = MutableStateFlow<List<String>>(emptyList())
    private val _LoadingFetchAI = MutableStateFlow<Boolean>(false)
    val scenarios: StateFlow<List<ScenarioItem>> = _scenarios
    val aiResponse: StateFlow<List<String>> = _aiResponse
    val loadingFetchAI: StateFlow<Boolean> = _LoadingFetchAI


    fun fetchData(
        apiKey: String,
        speech: String = "",
        conversRequest: List<WatsonMessage> = emptyList<WatsonMessage>()
    ) {
        viewModelScope.launch {
            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }

            val client = HttpClient(CIO) {
                expectSuccess = false               // let us read non-2xx bodies
                install(ContentNegotiation) { json(json) }
            }

            try {
                _LoadingFetchAI.value = true
                val token = getIamToken(apiKey)
                Log.d("Bearer Token", token.accessToken)

                val request = WatsonRequest(
                    messages =
                        conversRequest
//                        listOf(
//                        WatsonMessage(role = "user", content = speech)
//                    )
                )

                val url =
                    "https://us-south.ml.cloud.ibm.com/ml/v4/deployments/d2f95ace-8312-42f5-80d6-b0ac5f64da9f/ai_service?version=2021-05-01"

                // Single HTTP call → inspect status + raw text
                val httpResponse = client.post(url) {
                    header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                val status = httpResponse.status
                val rawText = httpResponse.bodyAsText()
                Log.d("HTTP", "status=${status.value}, len=${rawText.length}")
                Log.d("RawResponse", rawText.take(4000))

                if (!status.isSuccess()) {
                    throw IllegalStateException("Non-2xx (${status.value}) from server:\n$rawText")
                }

                // Try direct → fallback to { "result": { ... } }
                val response: WatsonResponse = try {
                    json.decodeFromString(rawText)
                } catch (_: Exception) {
                    json.decodeFromString<WrappedResponse>(rawText).result
                }

                Log.d("Response", response.choices.toString())

                if (response.choices.isNotEmpty()) {

                    val contentEl = response.choices.first().message.content
                    val scenarioItems = decodeScenarioItems(contentEl)

                    // Clean null/empty buckets
                    val cleaned = scenarioItems.map { item ->
                        val cleanedMap = item.scenarios
                            .mapValues { (_, v) ->
                                v.orEmpty().map(String::trim).filter { it.isNotBlank() }
                            }
                            .filterValues { it.isNotEmpty() }
                        item.copy(scenarios = cleanedMap)
                    }

                    // Case-insensitive key match for SMALL_TALK (handles variants like "Small_Talk")
                    val smallTalk = cleaned.asSequence()
                        .mapNotNull {
                            it.scenarios.entries.firstOrNull { e ->
                                e.key.equals(
                                    "SMALL_TALK",
                                    true
                                )
                            }?.value
                        }
                        .firstOrNull()
                        .orEmpty()

                    _scenarios.value = cleaned
                    _aiResponse.value = smallTalk

                    //1
//                    val innerJson = response.choices[0].message.content
//                    val scenarioItems = json.decodeFromString<List<ScenarioItem>>(innerJson)
//                    _scenarios.value = scenarioItems
//                    Log.d("ResponseScenario", _scenarios.value.toString())
//
//                    for (i in scenarioItems) {
//                        val smallTalk = i.scenarios.values
//                        smallTalk.forEach { i ->
//                            _aiResponse.value = i
//                        }
//                    }
// 2
//                    val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
//                    val contentEl = response.choices.first().message.content
//
//                    val scenarioItems: List<ScenarioItem> = when {
//                        contentEl is JsonArray -> json.decodeFromJsonElement(contentEl) // List<ScenarioItem>
//                        contentEl is JsonObject -> listOf(json.decodeFromJsonElement<ScenarioItem>(contentEl))
//                        contentEl is JsonPrimitive && contentEl.isString -> {
//                            val s = contentEl.content
//                            try { json.decodeFromString<List<ScenarioItem>>(s) }
//                            catch (_: Exception) { listOf(json.decodeFromString<ScenarioItem>(s)) }
//                        }
//                        else -> emptyList()
//                    }
//
//                    val cleaned = scenarioItems.map { item ->
//                        val cleanedMap = item.scenarios
//                            .mapValues { (_, v) -> v.orEmpty().map(String::trim).filter { it.isNotBlank() } }
//                            .filterValues { it.isNotEmpty() }
//                        item.copy(scenarios = cleanedMap)
//                    }
//                    val smallTalk = cleaned.asSequence()
//                        .mapNotNull { it.scenarios.entries.firstOrNull { e -> e.key.equals("SMALL_TALK", true) }?.value }
//                        .firstOrNull()
//                        .orEmpty()
//
//                    _scenarios.value = cleaned
//                    _aiResponse.value = smallTalk


                } else {
                    Log.w("Watson", "Empty choices or unexpected payload")
                }
            } catch (e: Exception) {
                Log.e("Watson", "Error: ${e.message}", e)
            } finally {
                _LoadingFetchAI.value = false
                client.close()
            }
        }
    }

    fun resetAISuggestion() {
        viewModelScope.launch {
            _aiResponse.value = emptyList<String>()
        }
    }


}


private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

private fun extractJsonBlock(s: String): String {
    var t = s.trim()
    // strip common transcript trailers
    t = t.substringBefore("\n\nUSER:").substringBefore("\nUSER:")
        .substringBefore("\n\nASSISTANT").substringBefore("\nASSISTANT")
        .trim()
    // if it still starts mid-text, grab first { or [
    if (t.isNotEmpty() && t.first() !in charArrayOf('[', '{')) {
        val iObj = t.indexOf('{');
        val iArr = t.indexOf('[')
        val i = listOf(iObj, iArr).filter { it >= 0 }.minOrNull() ?: -1
        if (i >= 0) t = t.substring(i).trim()
    }
    return t
}

private fun decodeScenarioItems(content: JsonElement): List<ScenarioItem> = when (content) {
    is JsonArray -> json.decodeFromJsonElement(content) // already an array
    is JsonObject -> listOf(json.decodeFromJsonElement<ScenarioItem>(content)) // single object
    is JsonPrimitive -> {
        val body = extractJsonBlock(content.content)
        when {
            body.startsWith("[") -> json.decodeFromString(body)
            body.startsWith("{") -> listOf(json.decodeFromString<ScenarioItem>(body))
            else -> emptyList()
        }
    }

    else -> emptyList()
}


