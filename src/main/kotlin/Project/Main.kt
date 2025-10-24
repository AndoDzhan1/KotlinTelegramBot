package org.example.Project

fun Question.asConsoleString(): String {
    return this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
        .joinToString(
            separator = "\n",
            prefix = "${this.correctAnswer.original}\n",
            postfix = "\n0 - Меню",
        )
}

fun main() {
    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

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

                    if (trainer.checkAnswer(userChoice?.minus(1))) {
                        println("Правильно!")
                    } else {
                        println("Неправильно! ${question.correctAnswer.original} - это ${question.correctAnswer.translate}")
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