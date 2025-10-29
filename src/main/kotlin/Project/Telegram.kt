package org.example.Project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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



fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val botService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()

    val json = Json {
        ignoreUnknownKeys = true
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = botService.getUpdates(lastUpdateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (message == "Hello") {
            botService.sendMessage(json, chatId, "Hello")
        }

        if (message == "/start") {
            botService.sendMenu(json, chatId)
        }

        if (data == TelegramBotService.STATISTICS) {
            val statistics = trainer.getStatistics()
            val statsMessage = "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent} %"
            botService.sendMessage(json, chatId, statsMessage)
        }

        if (data == TelegramBotService.LEARNING_WORDS) {
            val question = trainer.getNextQuestion()
            if (question != null) {
                botService.sendQuestion(json, chatId, question)
            }
        }

        if (data?.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX).toInt()

            val currentQuestion = trainer.currentQuestion

            if (currentQuestion != null) {
                val isCorrect = trainer.checkAnswer(userAnswerIndex)

                if (isCorrect) {
                    botService.sendMessage(json, chatId, "Правильно!")
                } else {
                    val correct = currentQuestion.correctAnswer.original
                    val translate = currentQuestion.correctAnswer.translate
                    botService.sendMessage(json, chatId, "Неправильно! $correct - это $translate")
                }
                botService.checkNextQuestionAndSend(trainer, chatId, json)
            }
        }
    }
}