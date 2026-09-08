package com.example.clearstackprototype1

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun ModelDownloadScreen(navController: androidx.navigation.NavHostController) {
    var progress by remember { mutableStateOf<Int?>(null) } // null means indeterminate
    var downloadedMB by remember { mutableStateOf(0L) }
    var isDownloading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val onDownloadClick: () -> Unit = {
        isDownloading = true
        error = null
        progress = null
        downloadedMB = 0L
        scope.launch {
            try {
                downloadModel(
                    context = context,
                    onProgress = { p, mb ->
                        progress = p
                        downloadedMB = mb
                    },
                    onError = { e ->
                        error = e
                        isDownloading = false
                    },
                    onSuccess = {
                        isDownloading = false
                        showSuccess = true
                    }
                )
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Unknown error"
                isDownloading = false
            }
        }
    }

    // If download succeeded, show snackbar and then navigate after a delay
    if (showSuccess) {
        LaunchedEffect(showSuccess) {
            // Show a snackbar for 2 seconds
            snackbarHostState.showSnackbar("All good to go!")
            delay(2000)
            navController.navigate("notifications") {
                // Clear the back stack so we don't go back to download screen
                popUpTo("model_download") { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Download Gemma Model",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (isDownloading) {
                    // Show progress indicator
                    if (progress != null) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = progress!! / 100f
                        )
                        Text(
                            text = "Downloading... ${progress}% ($downloadedMB MB)",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        )
                    } else {
                        // Indeterminate progress
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Downloading... $downloadedMB MB",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        )
                    }
                } else if (error != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: $error",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                error = null
                                // Reset and try again
                                onDownloadClick()
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                } else if (showSuccess) {
                    Text(
                        text = "All good to go!",
                        color = Color.Green,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Button(
                        onClick = onDownloadClick
                    ) {
                        Text("Download Model")
                    }
                }
            }
        }
    }
}

/**
 * Downloads the model from the given URL and saves it to the app's files directory.
 *
 * @param context          The application context.
 * @param onProgress       Callback for progress updates. [progress] is percentage (0-100) if known, otherwise null.
 *                         [downloadedMB] is the number of megabytes downloaded so far.
 * @param onError          Callback if an error occurs.
 * @param onSuccess        Callback when the download completes successfully.
 */
private suspend fun downloadModel(
    context: Context,
    onProgress: (progress: Int?, downloadedMB: Long) -> Unit,
    onError: (error: String) -> Unit,
    onSuccess: () -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "gemma3-1b-it.litertlm")
            // Delete existing file to start fresh
            if (file.exists()) {
                file.delete()
            }

            // TODO: swap this for your GitHub Releases URL once the asset is uploaded —
            // e.g. https://github.com/<you>/clearstack-android/releases/download/model-v1/gemma3-1b-it-int4.litertlm
            val urlString = "https://github.com/HWKDS/clearstack-android/releases/download/model-v1/gemma3-1b-it-int4.litertlm"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            // Some CDNs (including GitHub's asset storage) reject requests with no User-Agent header
            connection.setRequestProperty("User-Agent", "ClearStack-Android")
            connection.connect()

            val contentLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(file)

            val buffer = ByteArray(8192)
            var bytesRead = 0L
            var lastProgressPercent = -1
            var lastReportedMB = -1L

            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                bytesRead += read

                if (contentLength != -1) {
                    val progressPercent = (bytesRead * 100 / contentLength).toInt()
                    if (progressPercent - lastProgressPercent >= 1 || progressPercent == 100) {
                        onProgress(progressPercent, bytesRead / (1024 * 1024))
                        lastProgressPercent = progressPercent
                    }
                } else {
                    val mb = bytesRead / (1024 * 1024)
                    if (mb - lastReportedMB >= 1) {
                        onProgress(null, mb)
                        lastReportedMB = mb
                    }
                }
            }

            output.flush()
            output.close()
            input.close()
            connection.disconnect()

            // Verify the download actually completed before declaring success —
            // otherwise a dropped connection mid-download silently saves a truncated,
            // unusable file and the app thinks everything's fine.
            val expectedSize = 584_417_280L // known good size from your adb ls check
            if (contentLength != -1) {
                if (bytesRead != contentLength.toLong()) {
                    file.delete()
                    onError("Download incomplete — got $bytesRead of $contentLength bytes")
                    return@withContext
                }
            } else {
                // Server didn't report Content-Length (e.g. chunked encoding) —
                // fall back to checking against the known expected file size.
                if (bytesRead != expectedSize) {
                    file.delete()
                    onError("Download incomplete — got $bytesRead bytes, expected $expectedSize")
                    return@withContext
                }
            }

            onSuccess()
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unknown error")
        }
    }
}