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
            1 -> {
                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_THRESHOLD }
                    val questionWords = notLearnedList.shuffled().take(4)
                    val correctAnswer = questionWords.random()
                    val answer = questionWords.shuffled()

                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены")
                        return
                    }

                    println("${correctAnswer.original}")
                    answer.forEachIndexed { index, word ->
                        println("${index + 1} - ${word.translate}")
                    }

                    val userChoice = readln().toIntOrNull()
                    if (userChoice != null && userChoice in 1..answer.size) {
                        val selected = answer[userChoice - 1]
                        if (selected == correctAnswer) {
                            println("Верно")
                            correctAnswer.correctAnswersCount++
                            break
                        } else {
                            println("Неверно \nПравильный ответ: ${correctAnswer.translate}")
                        }
                    }
                }
            }

            2 -> {
                val totalCount = dictionary.size
                val learnedWords = dictionary.filter { it.correctAnswersCount >= LEARNED_THRESHOLD }
                val learnedCount = learnedWords.size
                val percent = if (totalCount > 0) (learnedCount * 100 / totalCount) else 0

                println("Выучено $learnedCount из $totalCount слов | $percent %\n")
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
    wordsFile.writeText("Hello|Привет|2\n")
    wordsFile.appendText("Dog|Собака\n")
    wordsFile.appendText("Cat|Кошка|5\n")
    wordsFile.appendText("Thank you|Спасибо|0\n")
    wordsFile.appendText("Hat|Шляпа|0")
}

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

const val LEARNED_THRESHOLD = 3