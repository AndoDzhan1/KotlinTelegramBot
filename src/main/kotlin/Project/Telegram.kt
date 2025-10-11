package org.example.Project

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val botService = TelegramBotService(botToken)

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
        val chatId = chatIdString.toInt()

        println(chatId)

        if (text == "Hello") {
            botService.sendMessage(chatId, "Hello")
        }
    }
}

const val TELEGRAM_BASE_URL = "https://api.telegram.org/bot"