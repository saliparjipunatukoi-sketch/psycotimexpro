package com.example.data.remote

data class SyncPushPayload(
    val action: String = "sync_push",
    val coach_id: Long,
    val athletes: List<Map<String, Any?>>,
    val timing_runs: List<Map<String, Any?>>,
    val attendances: List<Map<String, Any?>> = emptyList(),
    val fee_payments: List<Map<String, Any?>> = emptyList()
)

data class SyncPushResponse(
    val status: String,
    val message: String,
    val synced_athletes: Int? = null,
    val synced_runs: Int? = null,
    val server_time: String? = null
)

data class SyncDiagnosticResult(
    val timestamp: Long = System.currentTimeMillis(),
    val endpointUrl: String,
    val requestMethod: String,
    val httpStatusCode: Int,
    val isHttpSuccess: Boolean,
    val rawResponseBody: String,
    val errorMessage: String? = null,
    val athletesCountSent: Int,
    val runsCountSent: Int,
    val parsedStatus: String? = null,
    val parsedMessage: String? = null
)
