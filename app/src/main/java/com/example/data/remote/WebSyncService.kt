package com.example.data.remote

import android.util.Log
import com.example.data.local.entity.AthleteEntity
import com.example.data.local.entity.TimingRunEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSyncService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        const val DEFAULT_API_URL = "https://www.psycotimexpro.my/training-management/api.php"
        private const val TAG = "WebSyncDiagnostic"
    }

    suspend fun pushSyncData(
        endpointUrl: String = DEFAULT_API_URL,
        coachId: Long,
        athletes: List<AthleteEntity>,
        timingRuns: List<TimingRunEntity>
    ): SyncDiagnosticResult = withContext(Dispatchers.IO) {
        val rootJson = JSONObject().apply {
            put("action", "sync_push")
            put("coach_id", coachId)

            val athletesArr = JSONArray()
            athletes.forEach { a ->
                val athObj = JSONObject().apply {
                    put("id", a.id)
                    put("name", a.name)
                    put("ic_number", a.icNumber)
                    put("dob", a.dob)
                    put("age", a.age)
                    put("phone", a.phone)
                    put("height_cm", a.heightCm)
                    put("weight_kg", a.weightKg)
                    put("gender", a.gender)
                    put("sport_type", a.sportType)
                    put("category", a.category)
                    put("pb_seconds", a.pbSeconds)
                    put("previous_pb_seconds", a.previousPbSeconds)
                    put("distance_or_score", a.distanceOrScore)
                    put("previous_distance_or_score", a.previousDistanceOrScore)
                    put("monthly_fee", a.monthlyFee)
                    put("fee_due_date", a.feeDueDate)
                    put("notes", a.notes)
                    put("photo_uri", a.photoUri)
                }
                athletesArr.put(athObj)
            }
            put("athletes", athletesArr)

            val runsArr = JSONArray()
            timingRuns.forEach { r ->
                val runObj = JSONObject().apply {
                    put("id", r.id)
                    put("session_id", r.sessionId)
                    put("run_number", r.runNumber)
                    put("race_title", r.raceTitle)
                    put("formatted_time", r.formattedTime)
                    put("duration_millis", r.durationMillis)
                    put("date_string", r.dateString)
                    put("athletes_json", r.athletesJson)
                    put("cam_type", r.camType)
                }
                runsArr.put(runObj)
            }
            put("timing_runs", runsArr)
            put("attendances", JSONArray())
            put("fee_payments", JSONArray())
        }

        val jsonString = rootJson.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "PsycoTimeXPro-Android/2.0")
            .build()

        try {
            Log.d(TAG, "Sending sync_push to $endpointUrl (${athletes.size} athletes, ${timingRuns.size} runs)")
            val response = client.newCall(request).execute()
            val code = response.code
            val isSuccess = response.isSuccessful
            val rawBody = response.body?.string() ?: ""

            var parsedStatus: String? = null
            var parsedMessage: String? = null

            try {
                val respJson = JSONObject(rawBody)
                parsedStatus = respJson.optString("status")
                parsedMessage = respJson.optString("message")
            } catch (e: Exception) {
                Log.w(TAG, "Body is not JSON: $rawBody", e)
            }

            Log.i(TAG, "Sync Response Code: $code, Status: $parsedStatus, Message: $parsedMessage")

            SyncDiagnosticResult(
                endpointUrl = endpointUrl,
                requestMethod = "POST",
                httpStatusCode = code,
                isHttpSuccess = isSuccess,
                rawResponseBody = rawBody,
                errorMessage = if (!isSuccess) "HTTP $code: ${response.message}" else null,
                athletesCountSent = athletes.size,
                runsCountSent = timingRuns.size,
                parsedStatus = parsedStatus,
                parsedMessage = parsedMessage
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync Network Failure: ${e.message}", e)
            SyncDiagnosticResult(
                endpointUrl = endpointUrl,
                requestMethod = "POST",
                httpStatusCode = 0,
                isHttpSuccess = false,
                rawResponseBody = "",
                errorMessage = "${e.javaClass.simpleName}: ${e.message}",
                athletesCountSent = athletes.size,
                runsCountSent = timingRuns.size,
                parsedStatus = "error",
                parsedMessage = e.message ?: "Sambungan internet atau DNS gagal"
            )
        }
    }
}
