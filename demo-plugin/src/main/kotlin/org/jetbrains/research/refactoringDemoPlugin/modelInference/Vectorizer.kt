package org.jetbrains.research.refactoringDemoPlugin.modelInference

import kotlin.math.max

/**
 * Transforms text into vector of dimension = size of vocabulary size.
 * @property vocabulary vocabulary which used to vectorize text.
 *
 * See [Sorrel plugin source code](https://github.com/JetBrains-Research/sorrel).
 */
class Vectorizer(val vocabulary: List<String>) {
    val vectorDim = vocabulary.size

    /**
     * Vectorizes (counting vectorization) given text into vector_dim size vector +
     * add length of text as last component of vector.
     * @param text text to be vectorized.
     * @return IntArray with vector_dim + 1 size (vector).
     */
    fun vectorizeWithLength(text: String): IntArray {
        val textVector = vectorize(text).toMutableList()
        textVector.add(text.length)
        return textVector.toIntArray()
    }

    /**
     * Vectorizes (counting vectorization) given text into vector_dim size vector.
     * @param text text to be vectorized.
     * @return IntArray with vector_dim size (vector).
     */
    fun vectorize(text: String): IntArray {
        // Initialize empty vector for text
        val textVector = IntArray(vectorDim)
        val featureCount = HashMap<List<String>, Int>()
        val textList = text.split(" ")
        var maxFeatureLength = 0

        // Preprocessing: initialize empty mapping
        for (index in 0 until vectorDim) {
            val feature = vocabulary.get(index).split(" ")
            featureCount[feature] = 0
            max(maxFeatureLength, feature.size).also { maxFeatureLength = it }
        }

        // Going through all possible lengths and count features
        for (length in 1 until maxFeatureLength) {
            for (window in textList.windowed(length)) {
                if (window in featureCount.keys) {
                    featureCount[window] = featureCount.get(window)!! + 1
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
