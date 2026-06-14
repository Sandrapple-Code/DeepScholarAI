package com.example.services

import com.example.BuildConfig
import com.example.data.local.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ChatbotService {
    suspend fun getChatResponse(
        history: List<ChatMessage>,
        prompt: String,
        systemInstructionOverride: String? = null
    ): String
}

class GeminiChatbotService : ChatbotService {

    override suspend fun getChatResponse(
        history: List<ChatMessage>,
        prompt: String,
        systemInstructionOverride: String?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isDummyKey = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.length < 10

        if (isDummyKey) {
            return@withContext getMockChatResponse(prompt, systemInstructionOverride)
        }

        // Map ChatMessage to Gemini API Content objects
        val apiContents = mutableListOf<Content>()
        
        // Add previous message turns (limit to last 20 for context/performance)
        val relevantHistory = history.takeLast(20)
        for (msg in relevantHistory) {
            val role = if (msg.sender == "USER") "user" else "model"
            apiContents.add(
                Content(
                    role = role,
                    parts = listOf(Part(text = msg.text))
                )
            )
        }

        // Add user prompt
        apiContents.add(
            Content(
                role = "user",
                parts = listOf(Part(text = prompt))
            )
        )

        val finalSysInstruction = systemInstructionOverride ?: "You are DeepScholar Chatbot, an expert research partner. You answer academic questions, explain technology, code, or science with rich, structured descriptions. Keep a scholarly, friendly, and analytical tone."

        val request = GenerateContentRequest(
            contents = apiContents,
            systemInstruction = Content(
                parts = listOf(Part(text = finalSysInstruction))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("The AI service returned an empty candidate list.")
        } catch (e: Exception) {
            getMockChatResponse(prompt, finalSysInstruction, e.message)
        }
    }

    private fun getMockChatResponse(prompt: String, systemInstructionOverride: String? = null, errorMsg: String? = null): String {
        val lcPrompt = prompt.lowercase()
        val suffix = if (errorMsg != null) "\n\n*(Notice: Currently running in offline demo mode: $errorMsg)*" else ""

        val sysText = systemInstructionOverride?.lowercase() ?: ""
        val stylePrefix = when {
            sysText.contains("5 year old") -> "[Explain Like I'm 5 Mode]: Let's think of it like a beautiful box of building blocks! "
            sysText.contains("summarize") -> "[Summary Mode]: Key takeaways from analysis:\n"
            sysText.contains("insights") -> "[Expert Insights Mode]: Critical study of methodology showing major paradigm limits:\n"
            else -> ""
        }

        var baseReply = when {
            lcPrompt.contains("time") || lcPrompt.contains("take") || lcPrompt.contains("weeks") || lcPrompt.contains("days") || lcPrompt.contains("duration") -> {
                "Based on standard scientific timelines, researching this specific topic will take approximately **3 weeks** to synthesize correctly. I recommend a focused study schedule.\n\nHere is your proposed study plan:\n\n```study_plan_proposal\n{\n  \"topic\": \"Focused Scholar Research Plan\",\n  \"durationWeeks\": 3,\n  \"tasks\": [\n    \"Review major literature and define key parameters\",\n    \"Analyze core variables and write detailed synopsis\",\n    \"Synthesize final findings and export to knowledge folder\"\n  ]\n}\n```"
            }
            lcPrompt.contains("hello") || lcPrompt.contains("hi") -> {
                "${stylePrefix}Hello! I am DeepScholar AI, your expert research partner. How can I assist you with your project, schedules, notes, or planner folders today?"
            }
            lcPrompt.contains("quantum") -> {
                "${stylePrefix}Quantum mechanics explores subatomic behavior, including Superposition (existing in multiple states simultaneously) and Entanglement (correlated states across distance)."
            }
            lcPrompt.contains("ai") || lcPrompt.contains("artificial intelligence") || lcPrompt.contains("llm") -> {
                "${stylePrefix}Large Language Models use multi-headed self-attention transformer blocks to model sequence relationships and synthesize dense research ideas."
            }
            else -> {
                "${stylePrefix}Interesting inquiry! Under standard academic analysis, this topic requires exploring historically verified sources, structural factors, and dynamic variables. I am fully ready to help you analyze this."
            }
        }

        // Add researcher follow-ups
        val followUps = "\n\n## DeepScholar Researcher Follow-ups\n- How does this thesis align with contemporary empirical results?\n- What are the primary structural/energy limits of this methodology?\n- How can we implement this model in our active study folders?\n\nDo you want me to explain these questions or any other question you have in your mind?"

        return baseReply + followUps + suffix
    }
}
