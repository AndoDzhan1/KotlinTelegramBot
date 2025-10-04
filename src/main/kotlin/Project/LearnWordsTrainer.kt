package org.example.Project

import java.io.File

data class Statistics(
    val totalCount: Int,
    val learnedWords: List<Word>,
    val learnedCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainer {
    private var question: Question? = null
    private var dictionary: MutableList<Word> = loadDictionary().toMutableList()

    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val learnedWords = dictionary.filter { it.correctAnswersCount >= LEARNED_THRESHOLD }
        val learnedCount = learnedWords.size
        val percent = if (totalCount > 0) (learnedCount * 100 / totalCount) else 0
        return Statistics(totalCount, learnedWords, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_THRESHOLD }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.shuffled().take(4)
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
        this.dictionary = dictionary.toMutableList()
    }
}