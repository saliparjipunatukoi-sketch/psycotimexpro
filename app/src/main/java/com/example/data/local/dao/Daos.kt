package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AthleteDao {
    @Query("SELECT * FROM athletes ORDER BY pbSeconds ASC")
    fun getAllAthletes(): Flow<List<AthleteEntity>>

    @Query("SELECT * FROM athletes WHERE coachId = :coachId ORDER BY pbSeconds ASC")
    fun getAthletesByCoach(coachId: Long): Flow<List<AthleteEntity>>

    @Query("SELECT * FROM athletes WHERE id = :id LIMIT 1")
    suspend fun getAthleteById(id: Long): AthleteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthlete(athlete: AthleteEntity): Long

    @Update
    suspend fun updateAthlete(athlete: AthleteEntity)

    @Delete
    suspend fun deleteAthlete(athlete: AthleteEntity)

    @Query("SELECT COUNT(*) FROM athletes")
    fun getAthleteCount(): Flow<Int>

    @Query("SELECT MIN(pbSeconds) FROM athletes WHERE pbSeconds > 0")
    fun getBestPb(): Flow<Double?>
}

@Dao
interface TimingRunDao {
    @Query("SELECT * FROM timing_runs ORDER BY id DESC")
    fun getAllRuns(): Flow<List<TimingRunEntity>>

    @Query("SELECT * FROM timing_runs WHERE sessionId = :sessionId ORDER BY runNumber ASC")
    fun getRunsBySession(sessionId: String): Flow<List<TimingRunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: TimingRunEntity): Long

    @Delete
    suspend fun deleteRun(run: TimingRunEntity)

    @Query("DELETE FROM timing_runs WHERE sessionId = :sessionId AND runNumber = :runNumber")
    suspend fun deleteBySessionAndRun(sessionId: String, runNumber: Int)

    @Query("SELECT COUNT(*) FROM timing_runs")
    fun getTotalRunsCount(): Flow<Int>
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY id DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    fun getAttendanceByDate(dateString: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)
}

@Dao
interface FeePaymentDao {
    @Query("SELECT * FROM fee_payments ORDER BY id DESC")
    fun getAllFeePayments(): Flow<List<FeePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(feePayment: FeePaymentEntity): Long

    @Update
    suspend fun updateFeePayment(feePayment: FeePaymentEntity)

    @Delete
    suspend fun deleteFeePayment(feePayment: FeePaymentEntity)
}

@Dao
interface AiAnalysisDao {
    @Query("SELECT * FROM ai_analyses ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<AiAnalysisRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AiAnalysisRecordEntity): Long

    @Delete
    suspend fun deleteAnalysis(analysis: AiAnalysisRecordEntity)
}

@Dao
interface PhotoProofDao {
    @Query("SELECT * FROM photo_proofs ORDER BY timestamp DESC")
    fun getAllProofs(): Flow<List<PhotoProofEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProof(proof: PhotoProofEntity): Long

    @Delete
    suspend fun deleteProof(proof: PhotoProofEntity)
}

@Dao
interface CoachAccountDao {
    @Query("SELECT * FROM coach_accounts ORDER BY id DESC")
    fun getAllCoaches(): Flow<List<CoachAccountEntity>>

    @Query("SELECT * FROM coach_accounts WHERE email = :email LIMIT 1")
    suspend fun getCoachByEmail(email: String): CoachAccountEntity?

    @Query("SELECT * FROM coach_accounts WHERE id = :id LIMIT 1")
    suspend fun getCoachById(id: Long): CoachAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoach(coach: CoachAccountEntity): Long

    @Update
    suspend fun updateCoach(coach: CoachAccountEntity)

    @Delete
    suspend fun deleteCoach(coach: CoachAccountEntity)
}

@Dao
interface SubCoachDao {
    @Query("SELECT * FROM sub_coaches WHERE parentCoachId = :coachId ORDER BY id DESC")
    fun getSubCoachesByCoach(coachId: Long): Flow<List<SubCoachEntity>>

    @Query("SELECT * FROM sub_coaches WHERE username = :username LIMIT 1")
    suspend fun getSubCoachByUsername(username: String): SubCoachEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCoach(subCoach: SubCoachEntity): Long

    @Delete
    suspend fun deleteSubCoach(subCoach: SubCoachEntity)
}

@Dao
interface SubscriptionPaymentDao {
    @Query("SELECT * FROM subscription_payments WHERE coachId = :coachId ORDER BY id DESC")
    fun getPaymentsByCoach(coachId: Long): Flow<List<SubscriptionPaymentEntity>>

    @Query("SELECT * FROM subscription_payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<SubscriptionPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: SubscriptionPaymentEntity): Long
}
