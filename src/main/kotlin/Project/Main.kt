package org.example.Project

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.original + "\n" + variants + "\n" + "0 - Меню"
}

fun main() {

    val trainer = LearnWordsTrainer()

    while (true) {
        println("Меню: \n1 - Учить слова \n2 - Статистика \n0 - Выход\n")

        println("Выберите позицию:")
        val userInput = readln().toIntOrNull()

        when (userInput) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Все слова в словаре выучены")
                        break
                    } else {
                        println(question.asConsoleString())
                    }

                    val userChoice = readln().toIntOrNull()
                    if (userChoice == 0) break

                    val correctAnswerId = question.variants.indexOf(question.correctAnswer)

                    if (trainer.checkAnswer(userChoice?.minus(1))) {
                        println("Правильно! $correctAnswerId\n")
                    } else {
                        println("Неправильно! ${question.correctAnswer.translate} - это ${question.correctAnswer.original}")
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent} %\n")
            }

            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

fun createDataTest(wordsFile: File) {
    wordsFile.createNewFile()
    wordsFile.writeText("Hello|Привет|2\n")
    wordsFile.appendText("Dog|Собака\n")
    wordsFile.appendText("Cat|Кошка|5\n")
    wordsFile.appendText("Thank you|Спасибо|0\n")
    wordsFile.appendText("Hat|Шляпа|0")
}

const val LEARNED_THRESHOLD = 3