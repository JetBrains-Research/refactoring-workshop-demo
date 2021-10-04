package org.jetbrains.research.refactoringDemoPlugin.modelInference

import io.kinference.data.map.ONNXMap
import io.kinference.data.seq.ONNXSequence
import io.kinference.data.tensors.Tensor
import io.kinference.data.tensors.asTensor
import io.kinference.model.Model
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

    // Model & vectorizer for detection of licenses on project level initializiation
    @OptIn(ExperimentalTime::class)
    private val mlModel: Model = Model.load(
        LicenseDetector::class.java.getResource("/model/license_level_model_v2.onnx").readBytes()
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
     * @param  text for license detection
     * @return object of detected License.
     */
    @OptIn(ExperimentalTime::class)
    fun detectLicense(text: String): License? {
        // Convert text into vector
        val filteredText = filterText(text)
        val vector = vectorizer.vectorizeWithLength(filteredText)
        val tensor = FloatNDArray(inputShape) { vector[it].toFloat() }.asTensor("features")

        // Prediction
        val prediction = mlModel.predict(listOf(tensor))

        // Data transformation
        val predTensor = prediction[0] as Tensor
        val data = predTensor.data as LongNDArray
        val array = data.array.blocks

        val classIndex = array[0][0].toInt()
        val license = classes[classIndex]

        val unpack1 = ((prediction[1] as ONNXSequence).data as ArrayList<ONNXMap>)[0]
        val unpack2 = (unpack1.data as HashMap<Long, Tensor>)[classIndex.toLong()] as Tensor
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
