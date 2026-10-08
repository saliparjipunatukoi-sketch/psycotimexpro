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
        SubscriptionPaymentEntity::class,
        InboxMessageEntity::class
    ],
    version = 6,
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
    abstract fun inboxDao(): InboxDao

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
            val coachDao = db.coachAccountDao()

            // Hanya daftarkan Master Admin Roger untuk membolehkan pengurusan log masuk
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
                subscriptionExpiresAt = System.currentTimeMillis() + (3650L * 24 * 3600 * 1000)
            )
            coachDao.insertCoach(admin)
            // KOSONGKAN SEMUA DETAIL: Tiada data tiruan/dummy coach atau atlit disuntik. Sistem bermula dengan bersih 100%.
        }
    }
}
