data class GeminiVisionResponse(
    val candidates: List<VisionCandidate>
)

data class VisionCandidate(
    val content: VisionResponseContent
)

data class VisionResponseContent(
    val parts: List<VisionResponsePart>
)

data class VisionResponsePart(
    val text: String
)