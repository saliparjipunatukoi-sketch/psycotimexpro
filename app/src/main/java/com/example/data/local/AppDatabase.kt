package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AthleteEntity::class,
        TimingRunEntity::class,
        AttendanceEntity::class,
        FeePaymentEntity::class,
        AiAnalysisRecordEntity::class,
        PhotoProofEntity::class,
        CoachAccountEntity::class,
        SubCoachEntity::class,
        SubscriptionPaymentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun athleteDao(): AthleteDao
    abstract fun timingRunDao(): TimingRunDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feePaymentDao(): FeePaymentDao
    abstract fun aiAnalysisDao(): AiAnalysisDao
    abstract fun photoProofDao(): PhotoProofDao
    abstract fun coachAccountDao(): CoachAccountDao
    abstract fun subCoachDao(): SubCoachDao
    abstract fun subscriptionPaymentDao(): SubscriptionPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "psyco_timex_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val athleteDao = db.athleteDao()
            val feeDao = db.feePaymentDao()
            val timingRunDao = db.timingRunDao()
            val coachDao = db.coachAccountDao()
            val subCoachDao = db.subCoachDao()
            val subPaymentDao = db.subscriptionPaymentDao()

            // 1. Seed Master Admin
            val admin = CoachAccountEntity(
                email = "Saliparjipun.atukoi@gmail.com",
                name = "Roger (Master Admin)",
                nickname = "Coach Roger",
                icNumber = "850412-12-5678",
                phone = "+60195326399",
                clubName = "Kelab Olahraga Psyco Time X Pro",
                clubAddress = "Kompleks Sukan Keningau / Stadium Likas, Sabah",
                trainingSpecialty = "Balapan & Padang (Pecut, Jarak Jauh & Padang)",
                passwordHash = "Abc@1234",
                isApprovedByAdmin = true,
                role = "ADMIN",
                subscriptionStatus = "ACTIVE",
                subscriptionExpiresAt = System.currentTimeMillis() + (365L * 24 * 3600 * 1000)
            )
            val adminId = coachDao.insertCoach(admin)

            // Seed 1 Demo Coach with Free Trial
            val demoCoach = CoachAccountEntity(
                email = "coach.danial@gmail.com",
                name = "Danial Iskandar bin Rosli",
                nickname = "Coach Danial",
                icNumber = "900512-12-6677",
                phone = "011-2345678",
                clubName = "Akademi Pecut Elit",
                clubAddress = "Stadium Likas, Kota Kinabalu",
                trainingSpecialty = "Larian Pecut (100m, 200m, 4x100m)",
                passwordHash = "Coach@1234",
                isApprovedByAdmin = true,
                role = "COACH",
                subscriptionStatus = "FREE_TRIAL",
                trialEndDate = System.currentTimeMillis() + (7L * 24 * 3600 * 1000),
                subCoachSlots = 1
            )
            val demoCoachId = coachDao.insertCoach(demoCoach)

            // Seed 1 Free Sub-Coach
            subCoachDao.insertSubCoach(
                SubCoachEntity(
                    parentCoachId = demoCoachId,
                    username = "asst.haris",
                    password = "harispassword",
                    fullName = "Haris Iskandar (Penolong Jurulatih)",
                    phone = "017-6543210"
                )
            )

            // Seed Subscription Payment Receipt for demo
            subPaymentDao.insertPayment(
                SubscriptionPaymentEntity(
                    coachId = demoCoachId,
                    coachEmail = "coach.danial@gmail.com",
                    receiptNo = "SUB-PTXP-2610-01",
                    amount = 30.0,
                    paymentDate = "05/10/2026",
                    nextPaymentDue = "05/11/2026",
                    planType = "COACH_MONTHLY_RM30",
                    status = "PAID",
                    paymentMethod = "TUNAI / ONLINE TRANSFER"
                )
            )

            // 2. Seed initial athletes with rich details
            val a1 = AthleteEntity(
                coachId = demoCoachId,
                name = "Muhammad Danial",
                age = 17,
                dob = "2009-04-12",
                phone = "011-2345678",
                icNumber = "090412-12-5567",
                heightCm = 178.0,
                weightKg = 68.5,
                gender = "Lelaki",
                sportType = "Balapan",
                category = "100m Pecut",
                pbSeconds = 10.45,
                previousPbSeconds = 10.72,
                monthlyFee = 60.0,
                feeDueDate = "2026-10-10",
                notes = "Atlet MSSD & Sukma, pecutan 30m cemerlang"
            )
            val a2 = AthleteEntity(
                coachId = demoCoachId,
                name = "Ahmad Harith",
                age = 16,
                dob = "2010-07-25",
                phone = "012-9876543",
                icNumber = "100725-12-8871",
                heightCm = 172.5,
                weightKg = 62.0,
                gender = "Lelaki",
                sportType = "Balapan",
                category = "100m Pecut",
                pbSeconds = 10.68,
                previousPbSeconds = 10.85,
                monthlyFee = 60.0,
                feeDueDate = "2026-10-08",
                notes = "Fokus fasa drive pelepasan blok"
            )
            val a3 = AthleteEntity(
                coachId = demoCoachId,
                name = "Nur Aisyah Binti Zulkifli",
                age = 16,
                dob = "2010-11-03",
                phone = "019-8765432",
                icNumber = "101103-12-6632",
                heightCm = 165.0,
                weightKg = 54.0,
                gender = "Perempuan",
                sportType = "Balapan",
                category = "100m Pecut",
                pbSeconds = 11.85,
                previousPbSeconds = 12.10,
                monthlyFee = 60.0,
                feeDueDate = "2026-10-15",
                notes = "Pelari pertama baton 4x100m"
            )
            val a4 = AthleteEntity(
                coachId = demoCoachId,
                name = "Farhan Hakimi",
                age = 18,
                dob = "2008-02-14",
                phone = "013-4455667",
                icNumber = "080214-12-3321",
                heightCm = 182.0,
                weightKg = 72.0,
                gender = "Lelaki",
                sportType = "Padang",
                category = "Lompat Jauh",
                pbSeconds = 0.0,
                previousPbSeconds = 0.0,
                distanceOrScore = 7.15,
                previousDistanceOrScore = 6.85,
                monthlyFee = 60.0,
                feeDueDate = "2026-10-12",
                notes = "Pemenang Emas MSSM Lompat Jauh 7.15m"
            )
            val a5 = AthleteEntity(
                coachId = demoCoachId,
                name = "Siti Sarah Binti Osman",
                age = 17,
                dob = "2009-08-19",
                phone = "017-3322114",
                icNumber = "090819-12-4412",
                heightCm = 168.0,
                weightKg = 58.0,
                gender = "Perempuan",
                sportType = "Padang",
                category = "Lompat Jauh",
                pbSeconds = 0.0,
                previousPbSeconds = 0.0,
                distanceOrScore = 5.42,
                previousDistanceOrScore = 5.20,
                monthlyFee = 60.0,
                feeDueDate = "2026-10-20",
                notes = "Peningkatan lonjakan fasa pelepasan"
            )

            val id1 = athleteDao.insertAthlete(a1)
            val id2 = athleteDao.insertAthlete(a2)
            val id3 = athleteDao.insertAthlete(a3)
            val id4 = athleteDao.insertAthlete(a4)
            val id5 = athleteDao.insertAthlete(a5)

            feeDao.insertFeePayment(
                FeePaymentEntity(
                    athleteId = id1,
                    athleteName = "Muhammad Danial",
                    title = "Yuran Bulanan Latihan (Oktober)",
                    amount = 60.0,
                    dateString = "2026-10-01",
                    status = "LUNAS",
                    receiptNo = "PTXP-2610-01"
                )
            )
            feeDao.insertFeePayment(
                FeePaymentEntity(
                    athleteId = id2,
                    athleteName = "Ahmad Harith",
                    title = "Yuran Bulanan Latihan (Oktober)",
                    amount = 60.0,
                    dateString = "2026-10-02",
                    status = "TERTUNGGAK",
                    receiptNo = "PTXP-2610-02"
                )
            )

            // Seed sample timing run
            timingRunDao.insertRun(
                TimingRunEntity(
                    sessionId = "et_261005_0830",
                    runNumber = 1,
                    formattedTime = "00:10.582",
                    durationMillis = 10582,
                    dateString = "05/10/2026",
                    athletesJson = """[{"lane":1,"name":"Muhammad Danial"},{"lane":2,"name":"Ahmad Harith"}]""",
                    camType = "CAM_1_START"
                )
            )
        }
    }
}
