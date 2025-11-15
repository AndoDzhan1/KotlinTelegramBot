package org.example.Project

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

class TelegramBotService(
    private val botToken: String,
    private val json: Json,
) {

    companion object {
        const val TELEGRAM_BASE_URL = "https://api.telegram.org/bot"
        const val STATISTICS = "statistics"
        const val LEARNING_WORDS = "learning_word"
        const val RESET_CLICKED = "reset_clicked"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
    }

    private var client: HttpClient = HttpClient.newBuilder().build()

    private fun <T> withRetry(block: () -> T): T {
        var attempts = 0
        while (attempts < 3) {
            try {
                return block()
            } catch (e: java.io.IOException) {
                if (e.message?.contains("GOAWAY", ignoreCase = true) == true) {
                    println("INFO: GOAWAY received, recreating client and retrying")
                    client = HttpClient.newBuilder().build()
                    attempts++
                    if (attempts >= 3) {
                        println("ERROR: Max retries reached after GOAWAY.")
                        throw e
                    }
                    Thread.sleep(2000)
                    continue
                } else {
                    throw e
                }
            }
        }
        throw java.lang.RuntimeException("Max retries exceeded")
    }

    fun getUpdates(updateId: Long): Response {
        val urlGetUpdates = "$TELEGRAM_BASE_URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()

        return this.json.decodeFromString(responseBody)
    }

    fun sendMessage(chatId: Long, text: String): String {

        if (text.isBlank()) return "Сообщение пустое"
        if (text.length > 4096) return "Сообщение слишком длинное"

        val sendMessage = "$TELEGRAM_BASE_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        val requestBodyString = this.json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long): String {
        val url = "$TELEGRAM_BASE_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(listOf(
                    InlineKeyboard(text = "Учить слова", callbackData = LEARNING_WORDS),
                    InlineKeyboard(text = "Статистика", callbackData = STATISTICS),
                ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED),
                    )
                    )
            )
        )
        val requestBodyString = this.json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return withRetry {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val responseBody = response.body()
            println("DEBUG: Telegram response: $responseBody") // <-- Добавлено
            responseBody
        }
    }

    fun sendQuestion(chatId: Long, question: Question): String {
        val url = "$TELEGRAM_BASE_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                })
            )
        )

        val requestBodyString = this.json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        chatId: Long,
    ) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Все слова в словаре выучены")
        } else {
            sendQuestion(chatId, question)
        }
    }
}