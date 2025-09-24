package org.example.Project

import java.io.File

fun main() {

    val dictionary = loadDictionary()

    while (true) {
        println("Меню")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")
        println()

        println("Выберите позицию:")
        val userInput = readln().toIntOrNull()

        when (userInput) {
            1 -> println("Учить слова")
            2 -> {
                val totalCount = dictionary.size
                val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }
                val learnedCount = learnedWords.size
                val percent = if (totalCount > 0) (learnedCount * 100 / totalCount) else 0

                println("Выучено $learnedCount из $totalCount слов | $percent %")
                println()
            }
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

fun loadDictionary(): List<Word> {
    val wordsFile: File = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    if (!wordsFile.exists()) {
        createDataTest(wordsFile)
    }

    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val parts = line.split("|")
        val count = parts.getOrNull(2)?.toIntOrNull() ?: 0
        val word = Word(original = parts[0], translate = parts[1], correctAnswersCount = count)
        dictionary.add(word)
    }
    return dictionary
}

fun createDataTest(wordsFile: File) {
    wordsFile.createNewFile()
    wordsFile.writeText("Hello|Привет|2")
    wordsFile.appendText("\n")
    wordsFile.appendText("Dog|Собака")
    wordsFile.appendText("\n")
    wordsFile.appendText("Cat|Кошка|5")
}

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)