package org.example.Project

import java.io.File

fun main() {

    val wordsFile: File = File ("words.txt")

    wordsFile.createNewFile()
    wordsFile.writeText("Hello - Привет")
    wordsFile.appendText("Dog - Собака")
    wordsFile.appendText("Cat - Кошка")

    wordsFile.readLines().forEach { println(it) }
}