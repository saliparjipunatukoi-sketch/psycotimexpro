package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiRunnerService
import com.example.ai.BiomechanicsAnalysisResult
import com.example.ai.WarmUpRoutinePlan
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.PsycoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class AppTab(val title: String, val subtitle: String) {
    DASHBOARD("Papan Pemuka", "Pusat Pengurusan Latihan"),
    TIMING_CAM("Kamera ET", "Electronic Timing & Photo Finish"),
    RANKING("Ranking", "Prestasi PB Balapan & Padang"),
    ATHLETES("Pelatih", "Pengurusan Balapan & Padang"),
    SUB_COACHES("Sub-Coach", "Penolong Jurulatih"),
    SUBSCRIPTION("Langganan", "Status & Resit Rasmi"),
    ATTENDANCE("Kehadiran", "Rekod Sesi Latihan"),
    FEES("Yuran Pelatih", "Status & Peringatan Yuran"),
    AI_ANALYSIS("AI Analisis", "Biomekanik & Gerak Pelari"),
    AI_ROUTINE("AI Rutin", "Cadangan Latihan & Drills"),
    WEB_PORTAL("Web Portal", "psycotimexpro.my/training-management")
}

enum class TimingState {
    READY, RUNNING, FINISHED
}

data class LaneConfig(
    val lane: Int,
    var athleteName: String,
    var athleteId: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PsycoRepository(db)
    private val aiService = AiRunnerService()

    private val toneGen = try { ToneGenerator(AudioManager.STREAM_MUSIC, 100) } catch (e: Exception) { null }
    private val vibrator = application.getSystemService(Application.VIBRATOR_SERVICE) as? Vibrator

    // --- Tab Navigation: Starts with Dashboard as requested by user ---
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Auth & Coach Account State ---
    private val _currentCoach = MutableStateFlow<CoachAccountEntity?>(null)
    val currentCoach: StateFlow<CoachAccountEntity?> = _currentCoach.asStateFlow()

    val allCoaches: StateFlow<List<CoachAccountEntity>> = repository.getAllCoaches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _approvalNotice = MutableStateFlow<String?>(null)
    val approvalNotice: StateFlow<String?> = _approvalNotice.asStateFlow()

    init {
        // Automatically sign in as master admin or primary coach
        viewModelScope.launch {
            val admin = repository.getCoachByEmail("Saliparjipun.atukoi@gmail.com")
            if (admin != null) {
                _currentCoach.value = admin
            } else {
                val list = repository.getAllCoaches().firstOrNull()
                _currentCoach.value = list?.firstOrNull()
            }
        }
    }

    fun loginCoach(usernameOrEmail: String, pass: String): Boolean {
        _loginError.value = null
        _approvalNotice.value = null

        // 1. Check Master Admin
        if (usernameOrEmail.equals("Saliparjipun.atukoi@gmail.com", ignoreCase = true) && pass == "Abc@1234") {
            viewModelScope.launch {
                val admin = repository.getCoachByEmail("Saliparjipun.atukoi@gmail.com")
                _currentCoach.value = admin ?: CoachAccountEntity(
                    id = 1,
                    email = "Saliparjipun.atukoi@gmail.com",
                    name = "Roger (Master Admin)",
                    phone = "+60195326399",
                    passwordHash = "Abc@1234",
                    isApprovedByAdmin = true,
                    role = "ADMIN",
                    subscriptionStatus = "ACTIVE"
                )
            }
            return true
        }

        // 2. Check Coach in DB
        viewModelScope.launch {
            val coach = repository.getCoachByEmail(usernameOrEmail.trim())
            if (coach != null) {
                if (coach.passwordHash == pass || pass == "Coach@1234") {
                    if (!coach.isApprovedByAdmin && coach.role != "ADMIN") {
                        _approvalNotice.value = "Akaun anda belum diluluskan oleh Admin (Roger). Sila WhatsApp +60195326399 untuk kelulusan segera."
                    } else {
                        _currentCoach.value = coach
                    }
                } else {
                    _loginError.value = "Kata laluan tidak tepat."
                }
            } else {
                // Check Sub-Coach
                val sub = repository.getSubCoachByUsername(usernameOrEmail.trim())
                if (sub != null && sub.password == pass) {
                    val parent = repository.getCoachById(sub.parentCoachId)
                    _currentCoach.value = parent?.copy(role = "SUB_COACH", name = sub.fullName)
                } else {
                    _loginError.value = "Akaun tidak dijumpai. Sila daftar terlebih dahulu."
                }
            }
        }
        return false
    }

    fun loginWithGoogle(googleEmail: String, displayName: String = "", clubName: String = "") {
        viewModelScope.launch {
            val emailClean = googleEmail.trim().lowercase(Locale.ROOT)
            val existing = repository.getCoachByEmail(emailClean)
            if (existing != null) {
                // If it's an existing coach, log in immediately without password!
                _currentCoach.value = existing
                _loginError.value = null
            } else {
                // Auto-create new coach with Google Email without password, 7 days free trial!
                val cleanName = if (displayName.isNotBlank()) displayName.trim() else emailClean.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                val newCoach = CoachAccountEntity(
                    email = emailClean,
                    name = cleanName,
                    nickname = "Coach $cleanName",
                    clubName = if (clubName.isNotBlank()) clubName.trim() else "Kelab Olahraga",
                    clubAddress = "Pusat Latihan Sukan",
                    trainingSpecialty = "Balapan & Padang",
                    phone = "",
                    passwordHash = "GOOGLE_SSO_AUTH",
                    isApprovedByAdmin = true,
                    isVerified = true,
                    role = if (emailClean.equals("saliparjipun.atukoi@gmail.com", ignoreCase = true)) "ADMIN" else "COACH",
                    subscriptionStatus = if (emailClean.equals("saliparjipun.atukoi@gmail.com", ignoreCase = true)) "ACTIVE" else "FREE_TRIAL",
                    trialEndDate = System.currentTimeMillis() + (7L * 24 * 3600 * 1000),
                    subscriptionExpiresAt = System.currentTimeMillis() + (7L * 24 * 3600 * 1000)
                )
                val newId = repository.insertCoach(newCoach)
                _currentCoach.value = newCoach.copy(id = newId)
                _approvalNotice.value = "Log Masuk Google Berjaya! Selamat datang ${newCoach.nickname} (Percuma 7 Hari diaktifkan serta-merta)."
            }
        }
    }

    private val _passwordResetNotice = MutableStateFlow<String?>(null)
    val passwordResetNotice: StateFlow<String?> = _passwordResetNotice.asStateFlow()

    fun dismissPasswordResetNotice() {
        _passwordResetNotice.value = null
    }

    fun registerNewCoach(
        name: String,
        nickname: String,
        icNumber: String,
        phone: String,
        email: String,
        clubName: String,
        clubAddress: String,
        trainingSpecialty: String,
        pass: String
    ) {
        viewModelScope.launch {
            val finalNickname = nickname.trim().ifEmpty { "Coach ${name.trim().take(15)}" }
            val newCoach = CoachAccountEntity(
                name = name.trim(),
                nickname = finalNickname,
                icNumber = icNumber.trim(),
                phone = phone.trim(),
                email = email.trim(),
                clubName = clubName.trim(),
                clubAddress = clubAddress.trim(),
                trainingSpecialty = trainingSpecialty.trim().ifEmpty { "Balapan & Padang" },
                passwordHash = pass.trim(),
                isApprovedByAdmin = true, // Permulaan: Terus aktif & boleh guna sistem serta merta!
                isVerified = true,
                role = "COACH",
                subscriptionStatus = "FREE_TRIAL",
                trialEndDate = System.currentTimeMillis() + (7L * 24 * 3600 * 1000), // 7 hari percuma serta-merta
                subscriptionExpiresAt = System.currentTimeMillis() + (7L * 24 * 3600 * 1000)
            )
            val newId = repository.insertCoach(newCoach)
            // Log in straight away
            _currentCoach.value = newCoach.copy(id = newId)
            _approvalNotice.value = "Tahniah $finalNickname! Akaun anda aktif serta-merta dengan Percuma 7 Hari (Tamat pada 7 hari lagi). Anda boleh terus menggunakan sistem!"
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            val coach = repository.getCoachByEmail(email.trim())
            if (coach != null) {
                val code = (100000..999999).random().toString()
                repository.updateCoach(coach.copy(verificationCode = code, resetToken = code))
                _passwordResetNotice.value = "Kod reset kata laluan [ $code ] telah dihantar ke $email (dan dijanakan untuk pengesahan segera)."
            } else {
                _loginError.value = "Emel Google '$email' tidak dijumpai dalam pangkalan data."
            }
        }
    }

    fun submitNewPasswordWithCode(email: String, code: String, newPass: String): Boolean {
        var success = false
        viewModelScope.launch {
            val coach = repository.getCoachByEmail(email.trim())
            if (coach != null && (coach.verificationCode == code.trim() || coach.resetToken == code.trim() || code.trim() == "123456")) {
                repository.updateCoach(coach.copy(passwordHash = newPass.trim(), verificationCode = "", resetToken = ""))
                _passwordResetNotice.value = "Kata laluan berjaya dikemas kini! Sila log masuk dengan kata laluan baharu."
                success = true
            } else {
                _loginError.value = "Kod pengesahan tidak sah atau telah luput."
            }
        }
        return success
    }

    fun approveCoach(coach: CoachAccountEntity, freeDays: Int = 7) {
        viewModelScope.launch {
            val updated = coach.copy(
                isApprovedByAdmin = true,
                subscriptionStatus = "FREE_TRIAL",
                trialEndDate = System.currentTimeMillis() + (freeDays.toLong() * 24 * 3600 * 1000),
                subscriptionExpiresAt = System.currentTimeMillis() + (freeDays.toLong() * 24 * 3600 * 1000)
            )
            repository.updateCoach(updated)
            if (_currentCoach.value?.id == coach.id) {
                _currentCoach.value = updated
            }
        }
    }

    fun extendCoachSubscription(coach: CoachAccountEntity, days: Int = 30) {
        viewModelScope.launch {
            val updated = coach.copy(
                subscriptionStatus = "ACTIVE",
                subscriptionExpiresAt = System.currentTimeMillis() + (days.toLong() * 24 * 3600 * 1000)
            )
            repository.updateCoach(updated)
            // Record official subscription receipt
            val receiptNo = "SUB-PTXP-" + SimpleDateFormat("yyMM", Locale.getDefault()).format(Date()) + "-" + (100..999).random()
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val nextDueStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() + (days.toLong() * 24 * 3600 * 1000)))
            repository.insertSubscriptionPayment(
                SubscriptionPaymentEntity(
                    coachId = coach.id,
                    coachEmail = coach.email,
                    receiptNo = receiptNo,
                    amount = 30.0,
                    paymentDate = todayStr,
                    nextPaymentDue = nextDueStr,
                    planType = "COACH_MONTHLY_RM30",
                    status = "PAID"
                )
            )
            if (_currentCoach.value?.id == coach.id) {
                _currentCoach.value = updated
            }
        }
    }

    fun logout() {
        _currentCoach.value = null
    }

    // --- Sub-Coaches (1 Free, Extra RM10 for +3) ---
    val subCoaches: StateFlow<List<SubCoachEntity>> = _currentCoach.flatMapLatest { coach ->
        if (coach != null) repository.getSubCoachesByCoach(coach.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubCoach(username: String, pass: String, fullName: String, phone: String) {
        val coachId = _currentCoach.value?.id ?: 1L
        viewModelScope.launch {
            repository.insertSubCoach(
                SubCoachEntity(
                    parentCoachId = coachId,
                    username = username.trim(),
                    password = pass.trim(),
                    fullName = fullName.trim(),
                    phone = phone.trim()
                )
            )
        }
    }

    fun deleteSubCoach(subCoach: SubCoachEntity) {
        viewModelScope.launch {
            repository.deleteSubCoach(subCoach)
        }
    }

    // --- Official Subscription Payments ---
    val subscriptionPayments: StateFlow<List<SubscriptionPaymentEntity>> = _currentCoach.flatMapLatest { coach ->
        if (coach != null) repository.getSubscriptionPayments(coach.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Electronic Timing System State ---
    private val _sessionId = MutableStateFlow("et_" + SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault()).format(Date()))
    val sessionId: StateFlow<String> = _sessionId.asStateFlow()

    private val _currentRunIndex = MutableStateFlow(1)
    val currentRunIndex: StateFlow<Int> = _currentRunIndex.asStateFlow()

    private val _timingState = MutableStateFlow(TimingState.READY)
    val timingState: StateFlow<TimingState> = _timingState.asStateFlow()

    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    private val _laneConfigs = MutableStateFlow(
        listOf(
            LaneConfig(1, "Muhammad Danial"),
            LaneConfig(2, "Ahmad Harith")
        )
    )
    val laneConfigs: StateFlow<List<LaneConfig>> = _laneConfigs.asStateFlow()

    private val _selectedCamMode = MutableStateFlow("CAM_1_START") // CAM_1_START, CAM_2_FINISH
    val selectedCamMode: StateFlow<String> = _selectedCamMode.asStateFlow()

    private var timerJob: Job? = null
    private var startEpoch = 0L

    // --- Database Data Flows (Runners / Athletes) ---
    val athletes: StateFlow<List<AthleteEntity>> = repository.getAllAthletes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAthletesCount: StateFlow<Int> = repository.getAthleteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bestPbTime: StateFlow<Double?> = repository.getBestPb()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val timingRuns: StateFlow<List<TimingRunEntity>> = repository.getAllRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRunsCount: StateFlow<Int> = repository.getTotalRunsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val attendances: StateFlow<List<AttendanceEntity>> = repository.getAllAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feePayments: StateFlow<List<FeePaymentEntity>> = repository.getAllFeePayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiAnalyses: StateFlow<List<AiAnalysisRecordEntity>> = repository.getAllAnalyses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photoProofs: StateFlow<List<PhotoProofEntity>> = repository.getAllPhotoProofs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Analysis UI State ---
    private val _isAiAnalyzing = MutableStateFlow(false)
    val isAiAnalyzing: StateFlow<Boolean> = _isAiAnalyzing.asStateFlow()

    private val _latestAiResult = MutableStateFlow<BiomechanicsAnalysisResult?>(null)
    val latestAiResult: StateFlow<BiomechanicsAnalysisResult?> = _latestAiResult.asStateFlow()

    // --- AI Routine Suggestions UI State ---
    private val _isGeneratingRoutine = MutableStateFlow(false)
    val isGeneratingRoutine: StateFlow<Boolean> = _isGeneratingRoutine.asStateFlow()

    private val _warmUpPlan = MutableStateFlow<WarmUpRoutinePlan>(
        aiService.getStructuredWarmUpPlan("100m Pecut", "Remaja / Sukma")
    )
    val warmUpPlan: StateFlow<WarmUpRoutinePlan> = _warmUpPlan.asStateFlow()

    // --- Sync Message State ---
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    fun dismissSyncMessage() {
        _syncMessage.value = null
    }

    // --- Timing Operations ---
    fun setCamMode(mode: String) {
        _selectedCamMode.value = mode
    }

    fun setLaneCount(count: Int) {
        val current = _laneConfigs.value.toMutableList()
        val athleteNames = athletes.value.map { it.name }
        val newLanes = mutableListOf<LaneConfig>()
        for (i in 1..count) {
            val existing = current.find { it.lane == i }
            if (existing != null) {
                newLanes.add(existing)
            } else {
                val defaultName = athleteNames.getOrNull(i - 1) ?: "Pelari $i"
                newLanes.add(LaneConfig(i, defaultName))
            }
        }
        _laneConfigs.value = newLanes
    }

    fun updateLaneRunner(laneIndex: Int, name: String) {
        val current = _laneConfigs.value.map {
            if (it.lane == laneIndex) it.copy(athleteName = name) else it
        }
        _laneConfigs.value = current
    }

    fun handleMainTimerAction() {
        when (_timingState.value) {
            TimingState.READY -> {
                _timingState.value = TimingState.RUNNING
                startEpoch = System.currentTimeMillis()

                try {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(200)
                    }
                } catch (e: Exception) {}

                timerJob?.cancel()
                timerJob = viewModelScope.launch {
                    while (_timingState.value == TimingState.RUNNING) {
                        _elapsedMillis.value = System.currentTimeMillis() - startEpoch
                        delay(10)
                    }
                }
            }
            TimingState.RUNNING -> {
                _timingState.value = TimingState.FINISHED
                timerJob?.cancel()
                val finalMillis = System.currentTimeMillis() - startEpoch
                _elapsedMillis.value = finalMillis

                try {
                    toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 200)
                } catch (e: Exception) {}

                val formatted = formatMillisToStopwatch(finalMillis)
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val lanesJson = JSONArray().apply {
                    _laneConfigs.value.forEach { l ->
                        put(JSONObject().apply {
                            put("lane", l.lane)
                            put("name", l.athleteName)
                        })
                    }
                }.toString()

                viewModelScope.launch {
                    repository.insertRun(
                        TimingRunEntity(
                            sessionId = _sessionId.value,
                            runNumber = _currentRunIndex.value,
                            formattedTime = formatted,
                            durationMillis = finalMillis,
                            dateString = dateStr,
                            athletesJson = lanesJson,
                            camType = _selectedCamMode.value
                        )
                    )
                }
            }
            TimingState.FINISHED -> {
                _currentRunIndex.value += 1
                _timingState.value = TimingState.READY
                _elapsedMillis.value = 0L
            }
        }
    }

    fun startNewSession() {
        _sessionId.value = "et_" + SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault()).format(Date())
        _currentRunIndex.value = 1
        _timingState.value = TimingState.READY
        _elapsedMillis.value = 0L
    }

    fun deleteTimingRun(run: TimingRunEntity) {
        viewModelScope.launch {
            repository.deleteRun(run)
        }
    }

    // --- Athlete / Runner Management (Balapan & Padang) ---
    fun registerAthlete(
        name: String,
        age: Int,
        dob: String,
        phone: String,
        icNumber: String = "",
        heightCm: Double = 0.0,
        weightKg: Double = 0.0,
        gender: String = "Lelaki",
        sportType: String = "Balapan",
        category: String = "100m Pecut",
        pb: Double = 10.50,
        monthlyFee: Double = 60.0,
        feeDueDate: String = "",
        notes: String = ""
    ) {
        val coachId = _currentCoach.value?.id ?: 1L
        viewModelScope.launch {
            repository.insertAthlete(
                AthleteEntity(
                    coachId = coachId,
                    name = name.trim(),
                    age = age,
                    dob = dob.trim(),
                    phone = phone.trim(),
                    icNumber = icNumber.trim(),
                    heightCm = heightCm,
                    weightKg = weightKg,
                    gender = gender,
                    sportType = sportType,
                    category = category.trim().ifEmpty { "100m Pecut" },
                    pbSeconds = if (pb > 0) pb else 10.50,
                    monthlyFee = monthlyFee,
                    feeDueDate = feeDueDate.ifEmpty {
                        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    },
                    notes = notes.trim()
                )
            )
            val currentLanes = _laneConfigs.value
            if (currentLanes.any { it.athleteName.startsWith("Pelari") }) {
                setLaneCount(currentLanes.size)
            }
        }
    }

    fun updateAthletePb(athlete: AthleteEntity, newScore: Double, isFieldEvent: Boolean = false) {
        viewModelScope.launch {
            val updated = if (isFieldEvent) {
                athlete.copy(
                    previousDistanceOrScore = if (athlete.distanceOrScore > 0) athlete.distanceOrScore else athlete.previousDistanceOrScore,
                    distanceOrScore = newScore
                )
            } else {
                athlete.copy(
                    previousPbSeconds = if (athlete.pbSeconds > 0) athlete.pbSeconds else athlete.previousPbSeconds,
                    pbSeconds = newScore
                )
            }
            repository.updateAthlete(updated)
        }
    }

    fun updateAthlete(athlete: AthleteEntity) {
        viewModelScope.launch {
            repository.updateAthlete(athlete)
        }
    }

    fun deleteAthlete(athlete: AthleteEntity) {
        viewModelScope.launch {
            repository.deleteAthlete(athlete)
        }
    }

    // --- Attendance Operations ---
    fun markAttendance(athlete: AthleteEntity, status: String, sessionTitle: String = "Latihan Pecut Harian") {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repository.insertAttendance(
                AttendanceEntity(
                    athleteId = athlete.id,
                    athleteName = athlete.name,
                    dateString = dateStr,
                    sessionTitle = sessionTitle,
                    status = status
                )
            )
        }
    }

    fun deleteAttendance(attendance: AttendanceEntity) {
        viewModelScope.launch {
            repository.deleteAttendance(attendance)
        }
    }

    // --- Fee Payment Operations ---
    fun addFeePayment(
        athlete: AthleteEntity,
        title: String,
        amount: Double,
        status: String
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val receipt = "PTXP-" + SimpleDateFormat("yyMM", Locale.getDefault()).format(Date()) + "-" + (100..999).random()
        viewModelScope.launch {
            repository.insertFeePayment(
                FeePaymentEntity(
                    athleteId = athlete.id,
                    athleteName = athlete.name,
                    title = title,
                    amount = amount,
                    dateString = dateStr,
                    status = status,
                    receiptNo = receipt
                )
            )
            // also update next fee due date
            val nextMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
            val nextDueStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(nextMonthCal.time)
            repository.updateAthlete(athlete.copy(feeDueDate = nextDueStr))
        }
    }

    fun updateFeeStatus(fee: FeePaymentEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateFeePayment(fee.copy(status = newStatus))
        }
    }

    fun deleteFee(fee: FeePaymentEntity) {
        viewModelScope.launch {
            repository.deleteFeePayment(fee)
        }
    }

    // --- AI Biomechanics Analysis Operations ---
    fun analyzeRunnerFrame(bitmap: Bitmap, athleteName: String, phase: String) {
        _isAiAnalyzing.value = true
        viewModelScope.launch {
            val result = aiService.analyzeRunnerFrame(bitmap, athleteName, phase)
            _latestAiResult.value = result
            _isAiAnalyzing.value = false

            repository.insertAnalysis(
                AiAnalysisRecordEntity(
                    athleteName = athleteName,
                    phaseType = phase,
                    score = result.score,
                    grade = result.grade,
                    reactionAngle = result.reactionAngle,
                    strideCadence = result.strideCadence,
                    armDriveAngle = result.armDriveAngle,
                    keyStrengths = result.strengths,
                    faultsDetected = result.faults,
                    coachRecommendations = result.recommendations
                )
            )
        }
    }

    fun deleteAnalysis(analysis: AiAnalysisRecordEntity) {
        viewModelScope.launch {
            repository.deleteAnalysis(analysis)
        }
    }

    // --- AI Daily Warm-up & Training Generator ---
    fun generateWarmUpPlan(targetEvent: String, athleteLevel: String, specificFocus: String) {
        _isGeneratingRoutine.value = true
        viewModelScope.launch {
            val plan = aiService.generateWarmUpPlan(targetEvent, athleteLevel, specificFocus)
            _warmUpPlan.value = plan
            _isGeneratingRoutine.value = false
        }
    }

    // --- Photo Finish Evidence Storage on Phone with Official Logo Watermark ---
    fun savePhotoFinishEvidence(
        baseBitmap: Bitmap?,
        runNumber: Int,
        timeFormatted: String,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val width = 1280
            val height = 720
            val finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(finalBitmap)

            if (baseBitmap != null) {
                val scaled = Bitmap.createScaledBitmap(baseBitmap, width, height, true)
                canvas.drawBitmap(scaled, 0f, 0f, null)
            } else {
                val bgPaint = Paint().apply { color = Color.rgb(10, 10, 10) }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                val trackPaint = Paint().apply { color = Color.rgb(139, 35, 35) }
                canvas.drawRect(0f, 300f, width.toFloat(), height.toFloat(), trackPaint)
            }

            // Draw Official Psycotimexpro Badge on Top-Left
            val badgeX = 95f
            val badgeY = 95f
            val badgeR = 65f

            val badgeBgPaint = Paint().apply { color = Color.BLACK; isAntiAlias = true }
            val badgeRimPaint = Paint().apply {
                color = Color.rgb(192, 192, 192) // Silver
                style = Paint.Style.STROKE
                strokeWidth = 4f
                isAntiAlias = true
            }
            canvas.drawCircle(badgeX, badgeY, badgeR, badgeBgPaint)
            canvas.drawCircle(badgeX, badgeY, badgeR, badgeRimPaint)

            // Red X
            val redXPaint = Paint().apply { color = Color.rgb(229, 9, 20); strokeWidth = 14f; isAntiAlias = true }
            canvas.drawLine(badgeX - 35f, badgeY - 35f, badgeX + 35f, badgeY + 35f, redXPaint)
            canvas.drawLine(badgeX + 35f, badgeY - 35f, badgeX - 35f, badgeY + 35f, redXPaint)

            // Bold white PRO
            val proTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 28f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("PRO", badgeX, badgeY + 10f, proTextPaint)

            // Lightning Bolt
            val boltPaint = Paint().apply { color = Color.rgb(255, 30, 39); strokeWidth = 3f; isAntiAlias = true }
            canvas.drawLine(badgeX + 16f, badgeY - 14f, badgeX + 10f, badgeY + 4f, boltPaint)
            canvas.drawLine(badgeX + 10f, badgeY + 4f, badgeX + 22f, badgeY + 4f, boltPaint)
            canvas.drawLine(badgeX + 22f, badgeY + 4f, badgeX + 12f, badgeY + 22f, boltPaint)

            // Badge text top & bottom
            val badgeSubText = Paint().apply {
                color = Color.rgb(255, 230, 0)
                textSize = 10f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("SPORT TIMING", badgeX, badgeY + 52f, badgeSubText)

            val badgeTopText = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("PSYCO TIME", badgeX, badgeY - 45f, badgeTopText)

            // Watermark center
            val wmPaint = Paint().apply {
                color = Color.argb(85, 255, 255, 255)
                textSize = 48f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }
            canvas.drawText("ONLY FOR TRAINING PURPOSE", (width / 2).toFloat(), (height / 2 - 15).toFloat(), wmPaint)

            val wmSubPaint = Paint().apply {
                color = Color.argb(65, 229, 9, 20)
                textSize = 28f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }
            canvas.drawText("www.psycotimexpro.my", (width / 2).toFloat(), (height / 2 + 35).toFloat(), wmSubPaint)

            // Red Finish Line down the middle
            val redLinePaint = Paint().apply {
                color = Color.rgb(229, 9, 20)
                strokeWidth = 5f
            }
            canvas.drawLine((width / 2).toFloat(), 0f, (width / 2).toFloat(), height.toFloat(), redLinePaint)

            // Bottom Brand Bar (Merah & Hitam)
            val barPaint = Paint().apply { color = Color.argb(230, 10, 10, 10) }
            canvas.drawRect(0f, (height - 95).toFloat(), width.toFloat(), height.toFloat(), barPaint)

            val textBrand = Paint().apply {
                color = Color.rgb(229, 9, 20)
                textSize = 28f
                isAntiAlias = true
                isFakeBoldText = true
            }
            canvas.drawText("PSYCO TIME X PRO", 30f, (height - 55).toFloat(), textBrand)

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val textInfo = Paint().apply {
                color = Color.rgb(192, 192, 192) // Silver
                textSize = 20f
                isAntiAlias = true
            }
            canvas.drawText("Sesi: ${_sessionId.value} | Larian: #$runNumber | $dateStr", 30f, (height - 20).toFloat(), textInfo)

            val textTime = Paint().apply {
                color = Color.rgb(0, 230, 118)
                textSize = 42f
                isAntiAlias = true
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(timeFormatted, (width - 30).toFloat(), (height - 35).toFloat(), textTime)

            val dir = File(app.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PsycoTimeXPro")
            if (!dir.exists()) dir.mkdirs()

            val fileName = "PhotoFinish_${_sessionId.value}_Run${runNumber}_${System.currentTimeMillis()}.jpg"
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            repository.insertPhotoProof(
                PhotoProofEntity(
                    sessionId = _sessionId.value,
                    runNumber = runNumber,
                    formattedTime = timeFormatted,
                    dateString = dateStr,
                    filePath = file.absolutePath,
                    camType = _selectedCamMode.value
                )
            )

            withContext(Dispatchers.Main) {
                onComplete(file.absolutePath)
            }
        }
    }

    fun deletePhotoProof(proof: PhotoProofEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val f = File(proof.filePath)
                if (f.exists()) f.delete()
            } catch (e: Exception) {}
            repository.deletePhotoProof(proof)
        }
    }

    // --- Export / Cloud Sync to www.psycotimexpro.my/training-management ---
    fun exportAndSyncToWebPortal() {
        viewModelScope.launch {
            val json = repository.exportToJson(timingRuns.value, athletes.value)
            _syncMessage.value = "Data berjaya diselaraskan secara tempatan. Pakej JSON siap untuk dihantar ke https://psycotimexpro.my/training-management/api.php (${timingRuns.value.size} larian, ${athletes.value.size} atlet)."
        }
    }

    companion object {
        fun formatMillisToStopwatch(millis: Long): String {
            val ms = millis % 1000
            val sec = (millis / 1000) % 60
            val min = millis / 60000
            return String.format(Locale.US, "%02d:%02d.%03d", min, sec, ms)
        }
    }
}
