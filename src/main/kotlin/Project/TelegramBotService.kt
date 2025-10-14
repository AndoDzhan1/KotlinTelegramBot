package org.example.Project

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {

    companion object {
        const val TELEGRAM_BASE_URL = "https://api.telegram.org/bot"
        const val STATISTICS = "statistics"
        const val LEARNING_WORDS = "learning_word"
    }

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_BASE_URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {

        if (text.isNullOrBlank()) return "Сообщение пустое"
        if (text.length > 4096) return "Сообщение слишком длинное"

        val url = "$TELEGRAM_BASE_URL$botToken/sendMessage?chat_id=$chatId&text=${URLEncoder.encode(text, "UTF-8")}"
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMenu(chatId: Long): String {
        val url = "$TELEGRAM_BASE_URL$botToken/sendMessage"
        val sendMenuBody = """
        {
            "chat_id": $chatId,
            "text": "Основное меню",
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {
                            "text": "Учить слова",
                            "callback_data": "$LEARNING_WORDS"
                        },
                        {
                            "text": "Статистика",
                            "callback_data": "$STATISTICS"
                        }
                    ]
                ]
            }
        }
    """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}