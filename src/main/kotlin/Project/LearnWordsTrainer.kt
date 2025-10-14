package org.example.Project

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val totalCount: Int,
    val learnedCount: Int,
) {
    val percent: Int
        get() = if (totalCount > 0) learnedCount * 100 / totalCount else 0
}

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainer(
    private val learnedThreshold: Int = 3,
    private val optionsCount: Int = 3
) {
    private var question: Question? = null
    private val dictionary: List<Word> = loadDictionary()

    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.count { it.correctAnswersCount >= learnedThreshold }
        return Statistics(totalCount, learnedCount)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedThreshold }
        if (notLearnedList.isEmpty()) return null

        val questionWords = mutableListOf<Word>()
        questionWords.addAll(notLearnedList.shuffled().take(optionsCount))
        if (questionWords.size < optionsCount) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedThreshold }
            val toAdd = (optionsCount - questionWords.size).coerceAtMost(learnedList.size)
            questionWords.addAll(learnedList.shuffled().take(toAdd))
        }

        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerId: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerId) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun createDataTest(wordsFile: File) {
        wordsFile.createNewFile()
        wordsFile.writeText("Hello|Привет|2\n")
        wordsFile.appendText("Dog|Собака\n")
        wordsFile.appendText("Cat|Кошка|5\n")
        wordsFile.appendText("Thank you|Спасибо|0\n")
        wordsFile.appendText("Hat|Шляпа|0\n")
        wordsFile.appendText("Horse|Лошадь|0\n")
        wordsFile.appendText("Red|Красный|0\n")
        wordsFile.appendText("Orange|Оранжевый|0\n")
        wordsFile.appendText("Blue|Синий|0\n")
        wordsFile.appendText("Phone|Телефон|0\n")
        wordsFile.appendText("Modern|Современный|0")
    }

    private fun loadDictionary(): List<Word> {
        try {
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
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }

    }

    private fun saveDictionary() {
        val wordsFile = File("words.txt")
        wordsFile.writeText(
            dictionary.joinToString("\n") {
                "${it.original}|${it.translate}|${it.correctAnswersCount}"
            }
        )
    }
}