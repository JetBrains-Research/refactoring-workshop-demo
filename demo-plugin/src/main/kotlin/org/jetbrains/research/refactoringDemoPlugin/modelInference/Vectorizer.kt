package org.jetbrains.research.refactoringDemoPlugin.modelInference

import kotlin.math.max

/**
 * Transforms the text into a vector of dimension equal to the size of the vocabulary.
 * @property vocabulary vocabulary which is used to vectorize the text.
 *
 * See [Sorrel plugin source code](https://github.com/JetBrains-Research/sorrel).
 */
class Vectorizer(private val vocabulary: List<String>) {
    val vectorDim = vocabulary.size

    /**
     * Vectorizes (counting vectorization) given text into a vector_dim size vector and
     * adds the length of the text as a last component of the vector.
     * @param text text to vectorize.
     * @return IntArray with vector_dim + 1 size (vector).
     */
    fun vectorizeWithLength(text: String): IntArray {
        val textVector = vectorize(text).toMutableList()
        textVector.add(text.length)
        return textVector.toIntArray()
    }

    /**
     * Vectorizes (counting vectorization) given text into a vector_dim size vector.
     * @param text text to vectorize.
     * @return IntArray with vector_dim size (vector).
     */
    private fun vectorize(text: String): IntArray {
        // Initialize empty vector for the text
        val textVector = IntArray(vectorDim)
        val featureCount = HashMap<List<String>, Int>()
        val textList = text.split(" ")
        var maxFeatureLength = 0

        // Preprocessing: initialize empty mapping
        for (index in 0 until vectorDim) {
            val feature = vocabulary[index].split(" ")
            featureCount[feature] = 0
            max(maxFeatureLength, feature.size).also { maxFeatureLength = it }
        }

        // Go through all possible lengths and count features
        for (length in 1 until maxFeatureLength) {
            for (window in textList.windowed(length)) {
                if (window in featureCount.keys) {
                    featureCount[window] = featureCount[window]!! + 1
                }
            }
        }

        // Convert mapping count into vector
        for (index in 0 until vectorDim) {
            val feature = vocabulary[index].split(" ")
            textVector[index] = featureCount[feature]!!
        }

        return textVector
    }
}
