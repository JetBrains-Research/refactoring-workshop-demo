package org.jetbrains.research.refactoringDemoPlugin.modelInference

import io.kinference.core.KIEngine
import io.kinference.core.data.tensor.KITensor
import io.kinference.core.data.tensor.asTensor
import io.kinference.data.ONNXMap
import io.kinference.data.ONNXSequence
import io.kinference.ndarray.arrays.FloatNDArray
import io.kinference.ndarray.arrays.LongNDArray
import java.util.Locale
import org.jetbrains.research.refactoringDemoPlugin.modelInference.license.Apache_2_0
import org.jetbrains.research.refactoringDemoPlugin.modelInference.license.BSD_3_Clause
import org.jetbrains.research.refactoringDemoPlugin.modelInference.license.License
import org.jetbrains.research.refactoringDemoPlugin.modelInference.license.MIT
import kotlin.time.ExperimentalTime

/**
 * See [Sorrel plugin source code](https://github.com/JetBrains-Research/sorrel).
 */
class LicenseDetector {
    // Classes for decoding numeric predictions
    private val classes: List<String> = LicenseDetector::class.java.getResourceAsStream(
        "/model/license_level_classes_v2.txt"
    ).reader().readLines()

    // Detected License (string) to License class mapping
    private val licenseToClass = mapOf(
        "Apache-2.0" to Apache_2_0,
        "MIT" to MIT,
        "BSD-3-Clause" to BSD_3_Clause,
    )

    private val vectorizer = Vectorizer(
        LicenseDetector::class.java.getResourceAsStream(
            "/model/license_level_model_words_v2.txt"
        ).reader().readLines()
    )

    // Number of features the model accepts
    private val numFeatures = vectorizer.vectorDim + 1
    private val THRESHOLD = 0.8

    // Shape of input data
    private val inputShape = listOf(numFeatures).toIntArray()

    /**
     * Detects license class for a given text.
     * @param text for license detection
     * @return object of detected License.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun detectLicense(text: String): License? {
        // Model & vectorizer for detection of licenses on project level initializiation
        val mlModel = KIEngine.loadModel(
            LicenseDetector::class.java.getResource("/model/license_level_model_v2.onnx").readBytes()
        )
        // Convert text into vector
        val filteredText = filterText(text)
        val vector = vectorizer.vectorizeWithLength(filteredText)
        val tensorName = "features"
        val tensor = FloatNDArray(inputShape) { vector[it].toFloat() }.asTensor(tensorName)

        // Prediction
        val prediction = mlModel.predict(listOf(tensor))

        // Data transformation
        val predTensor = prediction[tensorName] as KITensor
        val data = predTensor.data as LongNDArray
        val array = data.array.blocks

        val classIndex = array[0][0].toInt()
        val license = classes[classIndex]

        val unpack1 = ((prediction[tensorName] as ONNXSequence<*, *>).data as ArrayList<ONNXMap<*,*>>)[0]
        val unpack2 = (unpack1.data as HashMap<Long, KITensor>)[classIndex.toLong()] as KITensor
        val probability = (unpack2.data as FloatNDArray).array.blocks[0][0]

        if (probability < THRESHOLD) {
            return null
        }
        return licenseToClass[license]
    }

    /**
     * Removes all non-alphanumeric characters and extra non-printable symbols from the text.
     * Also transforms characters to lowercase.
     * @param text the text to filter.
     * @return filtered text.
     */
    private fun filterText(text: String): String {
        val re = Regex("[^A-Za-z0-9 ]")
        val cleanText = text.replace("\\s+".toRegex(), " ")
        return re.replace(cleanText.lowercase(Locale.getDefault()), "")
    }
}
