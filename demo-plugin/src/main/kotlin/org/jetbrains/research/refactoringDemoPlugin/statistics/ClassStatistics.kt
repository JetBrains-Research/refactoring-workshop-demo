package org.jetbrains.research.refactoringDemoPlugin.statistics

data class ClassStatistics(
    // name of file with the class
    val fileName: String,
    // count of methods in the class
    val methodCount: Int,
    // lines of code inside the class
    val loc: Int
)

/*
    Calculates the number of lines in the text.
 */
fun String?.countLines() = this?.lines()?.filter { it.isNotBlank() }?.size ?: 0
