import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class WatsonResponse(
    val choices: List<Choice> = emptyList()  // ✅ make it optional or default
)

@Serializable
data class Choice(
    val index: Int,
    val message: Message
)

@Serializable
data class Message(

    val content: JsonElement,
    val role: String? = null// <-- this is a JSON string, not parsed yet
)

// Your scenario model
@Serializable
data class ScenarioItem(
    val scenarios: Map<String, List<String>?> = emptyMap(),
    @SerialName("default_path") val defaultPath: String? = null,
    @SerialName("speak_text") val speakText: String? = null,
    val rationale: String? = null
)



@Serializable
data class WatsonRequest(
    val messages: List<WatsonMessage>
)

@Serializable
data class WatsonMessage(
    val role: String,
    val content: String
)

@Serializable
data class WrappedResponse(val result: WatsonResponse)
