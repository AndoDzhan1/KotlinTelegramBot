package org.example.Project

import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.createNewFile()
    wordsFile.writeText("Hello|Привет|2")
    wordsFile.appendText("\n")
    wordsFile.appendText("Dog|Собака")
    wordsFile.appendText("\n")
    wordsFile.appendText("Cat|Кошка|5")

    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val parts = line.split("|")

        val count = parts.getOrNull(2)?.toIntOrNull() ?: 0

        val word = Word(original = parts[0], translate = parts[1], correctAnswersCount = count)
        dictionary.add(word)
    }
    println(dictionary)
}

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)