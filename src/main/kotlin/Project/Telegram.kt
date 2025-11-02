package org.example.Project

import kotlinx.serialization.json.Json

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }

    val botService = TelegramBotService(botToken, json)
    val trainer = LearnWordsTrainer()


    while (true) {
        Thread.sleep(2000)
        val response: Response = botService.getUpdates(lastUpdateId)
        println(response)

        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: continue
        val data = firstUpdate.callbackQuery?.data

        if (message == "Hello") {
            botService.sendMessage(chatId, "Hello")
        }

        if (message == "/start") {
            botService.sendMenu(chatId)
        }

        if (data == TelegramBotService.STATISTICS) {
            val statistics = trainer.getStatistics()
            val statsMessage = "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent} %"
            botService.sendMessage(chatId, statsMessage)
        }

        if (data == TelegramBotService.LEARNING_WORDS) {
            val question = trainer.getNextQuestion()
            if (question != null) {
                botService.sendQuestion(chatId, question)
            }
        }

        if (data?.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX).toInt()

            val currentQuestion = trainer.currentQuestion

            if (currentQuestion != null) {
                val isCorrect = trainer.checkAnswer(userAnswerIndex)

                if (isCorrect) {
                    botService.sendMessage(chatId, "Правильно!")
                } else {
                    val correct = currentQuestion.correctAnswer.original
                    val translate = currentQuestion.correctAnswer.translate
                    botService.sendMessage(chatId, "Неправильно! $correct - это $translate")
                }
                botService.checkNextQuestionAndSend(trainer, chatId)
            }
        }
    }
}