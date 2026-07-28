import com.google.gson.annotations.SerializedName

data class GeminiVisionRequest(
    val contents: List<VisionContent>
)

data class VisionContent(
    val parts: List<VisionPart>
)

data class VisionPart(
    val text: String? = null,

    @SerializedName("inline_data")
    val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mime_type")
    val mimeType: String,
    val data: String
)