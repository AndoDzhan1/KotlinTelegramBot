package org.example.Project

import kotlinx.serialization.json.Json
import kotlin.text.substringAfter

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val botService = TelegramBotService(botToken, json)
    val trainers = HashMap<Long, LearnWordsTrainer>()
    val trainer = LearnWordsTrainer()


    while (true) {
        Thread.sleep(2000)
        val response: Response = botService.getUpdates(lastUpdateId)
        println(response)

        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, botService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>, botService: TelegramBotService) {

    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

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

    if (data == TelegramBotService.RESET_CLICKED) {
        trainer.resetProgress()
        botService.sendMessage(chatId, "Прогресс сброшен")
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