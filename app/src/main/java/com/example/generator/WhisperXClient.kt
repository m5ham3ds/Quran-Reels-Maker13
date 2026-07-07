package com.example.generator

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhisperXClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://qalam249-whisperx-frontend.hf.space"

    suspend fun processAudio(
        file: File?,
        urlInput: String,
        arabicText: String,
        onProgress: (String) -> Unit
    ): ProcessResult = withContext(Dispatchers.IO) {
        var fileDataObj: JSONObject? = null
        if (file != null && file.exists()) {
            onProgress("جاري رفع الملف الصوتي للخادم...")
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "files",
                    file.name,
                    file.asRequestBody("audio/*".toMediaType())
                )
                .build()
            val request = Request.Builder()
                .url("$baseUrl/gradio_api/upload")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to upload file: ${response.code}")
            val respStr = response.body?.string() ?: "[]"
            val jsonArray = JSONArray(respStr)
            if (jsonArray.length() > 0) {
                val serverPath = jsonArray.getString(0)
                fileDataObj = JSONObject().apply {
                    put("path", serverPath)
                    put("meta", JSONObject().put("_type", "gradio.FileData"))
                }
            }
        }

        onProgress("جاري معالجة الصوت والنص...")
        val payload = JSONObject().apply {
            val dataArray = JSONArray()
            dataArray.put(fileDataObj ?: JSONObject.NULL)
            dataArray.put(urlInput)
            dataArray.put(arabicText)
            put("data", dataArray)
        }

        val predictReq = Request.Builder()
            .url("$baseUrl/gradio_api/call/process")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val predictRes = client.newCall(predictReq).execute()
        if (!predictRes.isSuccessful) throw Exception("Predict API failed: ${predictRes.code}")
        val predictBody = predictRes.body?.string() ?: ""
        val eventId = JSONObject(predictBody).getString("event_id")

        val streamReq = Request.Builder()
            .url("$baseUrl/gradio_api/call/process/$eventId")
            .get()
            .build()

        var chunksJson = ""
        var outAudioUrl = ""
        var errorLog = ""

        client.newCall(streamReq).execute().use { streamRes ->
            val source = streamRes.body?.source()
            while (source != null && !source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("event: complete")) {
                    val dataLine = source.readUtf8Line()
                    if (dataLine != null && dataLine.startsWith("data:")) {
                        val dataJson = dataLine.substring(5).trim()
                        val dataArray = JSONArray(dataJson)
                        if (dataArray.length() >= 8) {
                            chunksJson = dataArray.optString(4, "[]")
                            val audioObj = dataArray.optJSONObject(6)
                            if (audioObj != null && audioObj.has("url")) {
                                outAudioUrl = audioObj.getString("url")
                            }
                            errorLog = dataArray.optString(7, "")
                        }
                    }
                } else if (line.startsWith("event: error")) {
                    val dataLine = source.readUtf8Line()
                    throw Exception("Server Error: $dataLine")
                }
            }
        }
        
        if (errorLog.contains("❌") || errorLog.contains("خطأ") || chunksJson.isBlank()) {
            if (errorLog.isNotBlank()) {
                throw Exception(errorLog)
            } else {
                throw Exception("فشلت عملية الموائمة عبر WhisperX")
            }
        }

        ProcessResult(chunksJson, outAudioUrl, errorLog)
    }
}

data class ProcessResult(
    val chunksJson: String,
    val audioUrl: String,
    val errorLog: String
)
