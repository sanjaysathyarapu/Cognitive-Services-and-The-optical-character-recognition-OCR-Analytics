package com.example.simpleocrapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.simpleocrapp.ui.theme.SimpleOCRAppTheme
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : ComponentActivity() {

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                onImageSelected(uri)
            }
        }
    }

    private fun onImageSelected(imageUri: Uri) {
        // Assuming imageUri is the URI of the image you want to process
        val image: InputImage
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            image = InputImage.fromBitmap(bitmap, 0)
            // Process the image using ML Kit
            recognizeText(image)
        } catch (e: Exception) {
            // Handle exception
        }
    }

    // Function to process image and recognize text
    private fun recognizeText(imageUri: Uri, onTextRecognized: (String) -> Unit, onError: (Exception) -> Unit) {
        val image: InputImage
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully, handle the recognized text
                    val resultText = visionText.text
                    onTextRecognized(resultText)
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception, handle the error
                    onError(e)
                }
        } catch (e: Exception) {
            // Handle exception from image loading
            onError(e)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleOCRAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OCRApp()
                }
            }
        }
    }

    @Composable
    fun OCRApp() {
        // State for selected image URI
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        // State for OCR text
        var ocrText by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }


        val onImageSelect: () -> Unit = {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImageLauncher.launch(intent)
        }

        val onOCRAnalyze: () -> Unit = {
            imageUri?.let { uri ->
                isLoading = true
                recognizeText(uri,
                    onTextRecognized = { text ->
                        isLoading = false
                        ocrText = text
                    },
                    onError = { exception ->
                        isLoading = false
                        ocrText = "Error recognizing text: ${exception.localizedMessage}"
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Selected Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = ocrText, modifier = Modifier.padding(8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onImageSelect) {
                Text(text = "Select Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onOCRAnalyze) {
                Text(text = "OCR Analyze")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SimpleOCRAppTheme {
        OCRApp()
    }
}
