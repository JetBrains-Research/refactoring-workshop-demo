package org.jetbrains.research.refactoringDemoPlugin.statistics

data class ClassStatistics(
    // number of methods in the class
    val methodNumber: Int,
    // number of fields in the class
    val fieldsNumber: Int,
    // lines of code inside the class
    val loc: Int,
    // number of children (subclasses)
    val noc: Int
)
