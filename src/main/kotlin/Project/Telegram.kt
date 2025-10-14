package org.example.Project

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val botService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()
        val updateIdString = updateIdRegex.find(updates)?.groupValues?.get(1) ?: continue

        println(updateIdString)
        updateId = updateIdString.toInt() + 1

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)

        val chatIdRegex = """"chat"\s*:\s*\{\s*"id"\s*:\s*(\d+)""".toRegex()
        val chatIdString = chatIdRegex.find(updates)?.groupValues?.get(1) ?: continue
        val chatId = chatIdString.toLong()
        println(chatId)

        val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()
        val data = dataRegex.find(updates)?.groupValues?.get(1)


        if (text == "Hello") {
            botService.sendMessage(chatId, "Hello")
        }
        if (text == "/start") {
            botService.sendMenu(chatId)
        }

        if (data == "static") {
            botService.sendMessage(chatId, "Выучено 11 из 11 слов | 100%")
        }
    }
}