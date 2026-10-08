package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class PsycoRepository(private val database: AppDatabase) {

    // --- Athletes ---
    fun getAllAthletes(): Flow<List<AthleteEntity>> = database.athleteDao().getAllAthletes()
    fun getAthletesByCoach(coachId: Long): Flow<List<AthleteEntity>> = database.athleteDao().getAthletesByCoach(coachId)
    fun getAthleteCount(): Flow<Int> = database.athleteDao().getAthleteCount()
    fun getBestPb(): Flow<Double?> = database.athleteDao().getBestPb()
    suspend fun getAthleteById(id: Long) = database.athleteDao().getAthleteById(id)
    suspend fun insertAthlete(athlete: AthleteEntity) = database.athleteDao().insertAthlete(athlete)
    suspend fun updateAthlete(athlete: AthleteEntity) = database.athleteDao().updateAthlete(athlete)
    suspend fun deleteAthlete(athlete: AthleteEntity) = database.athleteDao().deleteAthlete(athlete)

    // --- Timing Runs ---
    fun getAllRuns(): Flow<List<TimingRunEntity>> = database.timingRunDao().getAllRuns()
    fun getRunsBySession(sessionId: String): Flow<List<TimingRunEntity>> = database.timingRunDao().getRunsBySession(sessionId)
    fun getTotalRunsCount(): Flow<Int> = database.timingRunDao().getTotalRunsCount()
    suspend fun insertRun(run: TimingRunEntity) = database.timingRunDao().insertRun(run)
    suspend fun deleteRun(run: TimingRunEntity) = database.timingRunDao().deleteRun(run)
    suspend fun deleteRunBySession(sessionId: String, runNumber: Int) =
        database.timingRunDao().deleteBySessionAndRun(sessionId, runNumber)

    // --- Attendance ---
    fun getAllAttendance(): Flow<List<AttendanceEntity>> = database.attendanceDao().getAllAttendance()
    fun getAttendanceByDate(dateString: String): Flow<List<AttendanceEntity>> =
        database.attendanceDao().getAttendanceByDate(dateString)
    suspend fun insertAttendance(attendance: AttendanceEntity) = database.attendanceDao().insertAttendance(attendance)
    suspend fun insertAllAttendance(list: List<AttendanceEntity>) = database.attendanceDao().insertAll(list)
    suspend fun updateAttendance(attendance: AttendanceEntity) = database.attendanceDao().updateAttendance(attendance)
    suspend fun deleteAttendance(attendance: AttendanceEntity) = database.attendanceDao().deleteAttendance(attendance)

    // --- Fee Payments ---
    fun getAllFeePayments(): Flow<List<FeePaymentEntity>> = database.feePaymentDao().getAllFeePayments()
    suspend fun insertFeePayment(fee: FeePaymentEntity) = database.feePaymentDao().insertFeePayment(fee)
    suspend fun updateFeePayment(fee: FeePaymentEntity) = database.feePaymentDao().updateFeePayment(fee)
    suspend fun deleteFeePayment(fee: FeePaymentEntity) = database.feePaymentDao().deleteFeePayment(fee)

    // --- AI Analyses ---
    fun getAllAnalyses(): Flow<List<AiAnalysisRecordEntity>> = database.aiAnalysisDao().getAllAnalyses()
    suspend fun insertAnalysis(analysis: AiAnalysisRecordEntity) = database.aiAnalysisDao().insertAnalysis(analysis)
    suspend fun deleteAnalysis(analysis: AiAnalysisRecordEntity) = database.aiAnalysisDao().deleteAnalysis(analysis)

    // --- Photo Proofs Stored in Phone ---
    fun getAllPhotoProofs(): Flow<List<PhotoProofEntity>> = database.photoProofDao().getAllProofs()
    suspend fun insertPhotoProof(proof: PhotoProofEntity) = database.photoProofDao().insertProof(proof)
    suspend fun deletePhotoProof(proof: PhotoProofEntity) = database.photoProofDao().deleteProof(proof)

    // --- Coach Accounts ---
    fun getAllCoaches(): Flow<List<CoachAccountEntity>> = database.coachAccountDao().getAllCoaches()
    suspend fun getCoachByEmail(email: String): CoachAccountEntity? = database.coachAccountDao().getCoachByEmail(email)
    suspend fun getCoachById(id: Long): CoachAccountEntity? = database.coachAccountDao().getCoachById(id)
    suspend fun insertCoach(coach: CoachAccountEntity): Long = database.coachAccountDao().insertCoach(coach)
    suspend fun updateCoach(coach: CoachAccountEntity) = database.coachAccountDao().updateCoach(coach)
    suspend fun deleteCoach(coach: CoachAccountEntity) = database.coachAccountDao().deleteCoach(coach)

    // --- Sub-Coaches ---
    fun getSubCoachesByCoach(coachId: Long): Flow<List<SubCoachEntity>> = database.subCoachDao().getSubCoachesByCoach(coachId)
    suspend fun getSubCoachByUsername(username: String): SubCoachEntity? = database.subCoachDao().getSubCoachByUsername(username)
    suspend fun insertSubCoach(subCoach: SubCoachEntity): Long = database.subCoachDao().insertSubCoach(subCoach)
    suspend fun deleteSubCoach(subCoach: SubCoachEntity) = database.subCoachDao().deleteSubCoach(subCoach)

    // --- Subscription Payments ---
    fun getSubscriptionPayments(coachId: Long): Flow<List<SubscriptionPaymentEntity>> = database.subscriptionPaymentDao().getPaymentsByCoach(coachId)
    fun getAllSubscriptionPayments(): Flow<List<SubscriptionPaymentEntity>> = database.subscriptionPaymentDao().getAllPayments()
    suspend fun insertSubscriptionPayment(payment: SubscriptionPaymentEntity): Long = database.subscriptionPaymentDao().insertPayment(payment)

    suspend fun clearAllStoredData() {
        database.clearAllTables()
    }

    // --- Inbox & Monthly Report Methods ---
    fun getInboxMessages(coachId: Long): Flow<List<InboxMessageEntity>> = database.inboxDao().getMessagesByCoach(coachId)
    fun getUnreadInboxCount(coachId: Long): Flow<Int> = database.inboxDao().getUnreadCount(coachId)
    suspend fun insertInboxMessage(message: InboxMessageEntity): Long = database.inboxDao().insertMessage(message)
    suspend fun markInboxMessageAsRead(id: Long) = database.inboxDao().markAsRead(id)
    suspend fun deleteInboxMessage(id: Long) = database.inboxDao().deleteMessage(id)
    suspend fun findInboxMessageByTitlePrefix(coachId: Long, prefix: String): InboxMessageEntity? = database.inboxDao().findMessageByTitlePrefix(coachId, prefix)

    // --- Export / Sync Utility to merge with psycotimexpro.my ---
    suspend fun exportToJson(runs: List<TimingRunEntity>, athletes: List<AthleteEntity>): String {
        val root = JSONObject()
        root.put("app", "Psyco Time X Pro")
        root.put("version", "2.0")
        root.put("website", "https://psycotimexpro.my")
        root.put("portal", "https://psycotimexpro.my/training-management")
        root.put("exported_at", System.currentTimeMillis())

        val athletesArray = JSONArray()
        athletes.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("coach_id", a.coachId)
            obj.put("name", a.name)
            obj.put("age", a.age)
            obj.put("dob", a.dob)
            obj.put("ic_number", a.icNumber)
            obj.put("height_cm", a.heightCm)
            obj.put("weight_kg", a.weightKg)
            obj.put("gender", a.gender)
            obj.put("sport_type", a.sportType)
            obj.put("category", a.category)
            obj.put("pb", a.pbSeconds)
            obj.put("monthly_fee", a.monthlyFee)
            obj.put("fee_due_date", a.feeDueDate)
            athletesArray.put(obj)
        }
        root.put("athletes", athletesArray)

        val runsArray = JSONArray()
        runs.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("sid", r.sessionId)
            obj.put("run", r.runNumber)
            obj.put("time", r.formattedTime)
            obj.put("date", r.dateString)
            obj.put("athletes", JSONArray(r.athletesJson))
            runsArray.put(obj)
        }
        root.put("runs", runsArray)

        return root.toString(2)
    }
}
