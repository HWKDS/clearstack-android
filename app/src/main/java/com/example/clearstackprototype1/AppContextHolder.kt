package com.example.clearstackprototype1

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.clearstackprototype1.GemmaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object AppContextHolder {
    var context: Context? = null
}

class ClearStackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.context = applicationContext

        // Kick off model load in the background at process start,
        // so it's ready (or nearly ready) by the time a notification arrives.
        CoroutineScope(Dispatchers.IO).launch {
            val modelFile = File(applicationContext.filesDir, "gemma3-1b-it.litertlm")
            if (modelFile.exists()) {
                Log.d("ClearStack", "Model file found: ${modelFile.absolutePath}")
                try {
                    GemmaService.initialize(modelFile.absolutePath)
                    Log.d("ClearStack", "Model initialized successfully")
                } catch (e: Exception) {
                    Log.e("ClearStack", "Failed to initialize model", e)
                }
            } else {
                Log.d("ClearStack", "Model file not found: ${modelFile.absolutePath}")
            }
        }
    }
}