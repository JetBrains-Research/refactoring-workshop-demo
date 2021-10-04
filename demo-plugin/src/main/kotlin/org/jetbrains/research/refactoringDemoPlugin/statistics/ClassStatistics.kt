package org.jetbrains.research.refactoringDemoPlugin.statistics

data class ClassStatistics(
    // count of fields in the class
    val fieldCount: Int,
    // count of methods in the class
    val methodCount: Int,
    // lines of code inside the class
    val loc: Int
)
