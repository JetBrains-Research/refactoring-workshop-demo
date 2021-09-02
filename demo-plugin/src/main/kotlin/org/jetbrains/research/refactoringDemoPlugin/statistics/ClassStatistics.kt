package org.jetbrains.research.refactoringDemoPlugin.statistics

data class ClassStatistics(
    // count of methods in the class
    val methodCount: Int,
    // count of fields in the class
    val fieldCount: Int,
    // lines of code inside the class
    val loc: Int,
    // count of subclasses
    val subclassCount: Int
)
