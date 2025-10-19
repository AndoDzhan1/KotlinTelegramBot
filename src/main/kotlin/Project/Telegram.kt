package org.example.Project

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val botService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()

    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex = """"chat"\s*:\s*\{\s*"id"\s*:\s*(\d+)""".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        val updateIdString = updateIdRegex.find(updates)?.groupValues?.get(1) ?: continue
        println(updateIdString)
        updateId = updateIdString.toInt() + 1

        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)

        val chatIdString = chatIdRegex.find(updates)?.groupValues?.get(1) ?: continue
        val chatId = chatIdString.toLongOrNull() ?: continue
        println(chatId)

        val data = dataRegex.find(updates)?.groupValues?.get(1)

        if (text == "Hello") {
            botService.sendMessage(chatId, "Hello")
        }
        if (text == "/start") {
            botService.sendMenu(chatId)
        }

        if (data == TelegramBotService.STATISTICS) {
            val statistics = trainer.getStatistics()
            val statsMessage = "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent} %"
            botService.sendMessage(chatId, statsMessage)
        }
        if (data == TelegramBotService.LEARNING_WORDS) {
            botService.checkNextQuestionAndSend(trainer, botService, chatId)
        }
    }
}