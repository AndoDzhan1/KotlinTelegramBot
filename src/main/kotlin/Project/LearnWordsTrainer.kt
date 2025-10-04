package org.example.Project

import java.io.File

data class Statistics(
    val totalCount: Int,
    val learnedCount: Int,
) {
    val precent: Int
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
    private val dictionary: MutableList<Word> = loadDictionary().toMutableList()

    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.count { it.correctAnswersCount >= learnedThreshold }
        return Statistics(totalCount, learnedCount)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedThreshold }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.shuffled().take(optionsCount)
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
                saveDictionary(dictionary)
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
        wordsFile.appendText("Hat|Шляпа|0")
    }

    private fun loadDictionary(): List<Word> {
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

    private fun saveDictionary(dictionary: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.writeText(
            dictionary.joinToString("\n") {
                "${it.original}|${it.translate}|${it.correctAnswersCount}"
            }
        )
    }
}