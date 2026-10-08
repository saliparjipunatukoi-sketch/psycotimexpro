package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athletes")
data class AthleteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coachId: Long = 1,
    val name: String,
    val age: Int,
    val dob: String = "",
    val phone: String = "",
    val icNumber: String = "",
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val gender: String = "Lelaki", // Lelaki, Perempuan
    val sportType: String = "Balapan", // Balapan, Padang
    val category: String = "100m Sprint",
    val pbSeconds: Double = 10.50, // PB Terkini
    val previousPbSeconds: Double = 10.75, // PB Lama untuk banding peningkatan
    val distanceOrScore: Double = 0.0, // Catatan Acara Padang terkini (meter)
    val previousDistanceOrScore: Double = 0.0, // Catatan Padang lama (meter)
    val photoUri: String = "", // Gambar mandatori atlit untuk rekod & kejohanan
    val monthlyFee: Double = 50.0,
    val feeDueDate: String = "", // e.g. "2026-10-15"
    val notes: String = "",
    val registeredDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "timing_runs")
data class TimingRunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val runNumber: Int,
    val raceTitle: String = "Race 1",
    val formattedTime: String,
    val durationMillis: Long,
    val dateString: String,
    val athletesJson: String = "[]",
    val camType: String = "CAM_1_START",
    val snapshotUri: String? = null,
    val videoUri: String? = null,
    val torsoDetected: Boolean = false,
    val finishPhotoUri: String? = null,
    val isSynced: Boolean = false
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val athleteId: Long,
    val athleteName: String,
    val dateString: String,
    val sessionTitle: String = "Latihan Pecut Harian",
    val status: String = "HADIR", // HADIR, LEWAT, TIDAK_HADIR, SAKIT
    val notes: String = ""
)

@Entity(tableName = "fee_payments")
data class FeePaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val athleteId: Long,
    val athleteName: String,
    val title: String,
    val amount: Double,
    val dateString: String,
    val status: String = "LUNAS", // LUNAS, TERTUNGGAK
    val receiptNo: String = ""
)

@Entity(tableName = "ai_analyses")
data class AiAnalysisRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val athleteId: Long = 0,
    val athleteName: String,
    val phaseType: String = "SPRINT_STRIDE", // STARTING_BLOCK, SPRINT_STRIDE, WARM_UP_POSTURE, FINISH_DIP
    val score: Int = 85,
    val grade: String = "A",
    val reactionAngle: String = "45° Drive",
    val strideCadence: String = "4.2 langkah/s",
    val armDriveAngle: String = "Ayunan 90°",
    val keyStrengths: String = "Tolakan blok yang kuat dan stabil.",
    val faultsDetected: String = "Kepala mendongak terlalu awal di fasa pemecutan.",
    val coachRecommendations: String = "Kekalkan pandangan ke bawah 15-20m pertama untuk fasa 'drive'.",
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "photo_proofs")
data class PhotoProofEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val runNumber: Int,
    val formattedTime: String,
    val dateString: String,
    val filePath: String,
    val camType: String = "CAM_2_FINISH",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "coach_accounts")
data class CoachAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val name: String, // Nama Penuh Coach
    val nickname: String = "", // Gelaran / Nick Name cth: "Coach Roger"
    val icNumber: String = "", // No. Kad Pengenalan Coach
    val phone: String = "",
    val clubName: String = "", // Nama Kelab / Akademi Sukan
    val clubAddress: String = "", // Alamat Kelab / Lokasi Latihan
    val clubLogoUri: String = "", // Logo Kelab Rasmi (100x100)
    val profilePhotoUri: String = "", // Gambar Profil Coach
    val achievements: String = "", // Pencapaian Coach cth: Jurulatih Pecut MSSM Negeri, Kejohanan Terbuka, Sukma
    val licenses: String = "", // Lesen cth: Sport Science Level 1, Sport Science Level 2, World Athletics Level 1
    val bio: String = "", // Maklumat & Latar Belakang Kejurulatihan
    val hasUpdatedProfileDetails: Boolean = false, // Menandakan coach telah kemaskini pencapaian & lesen
    val trainingSpecialty: String = "Balapan & Padang", // Latihan yang diajar
    val passwordHash: String = "",
    val isApprovedByAdmin: Boolean = true, // Permulaan: Terus aktif & boleh guna 7 hari percuma serta merta
    val isVerified: Boolean = true, // Verify email/phone
    val verificationCode: String = "",
    val resetToken: String = "",
    val role: String = "COACH", // ADMIN, COACH, SUB_COACH
    val trialStartDate: Long = System.currentTimeMillis(),
    val trialEndDate: Long = System.currentTimeMillis() + (7L * 24 * 3600 * 1000), // 7 days free
    val subscriptionStatus: String = "FREE_TRIAL", // FREE_TRIAL, ACTIVE, EXPIRED, PENDING_APPROVAL
    val subscriptionExpiresAt: Long = System.currentTimeMillis() + (7L * 24 * 3600 * 1000),
    val subCoachSlots: Int = 1, // 1 free sub-coach included
    val parentCoachId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inbox_messages")
data class InboxMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coachId: Long,
    val senderName: String = "Sistem Psyco Time X Pro",
    val title: String,
    val content: String,
    val dateString: String,
    val messageType: String = "MONTHLY_REPORT", // MONTHLY_REPORT, ADMIN_MEMO, DOCUMENT_NOTICE, SYSTEM_REMINDER
    val isRead: Boolean = false,
    val attachedDocumentTitle: String? = null,
    val attachedDataJson: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sub_coaches")
data class SubCoachEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentCoachId: Long,
    val username: String,
    val password: String,
    val fullName: String,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscription_payments")
data class SubscriptionPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coachId: Long,
    val coachEmail: String,
    val receiptNo: String,
    val amount: Double = 30.0,
    val paymentDate: String,
    val nextPaymentDue: String,
    val planType: String = "COACH_MONTHLY_RM30",
    val status: String = "PAID",
    val paymentMethod: String = "TUNAI / ONLINE TRANSFER",
    val timestamp: Long = System.currentTimeMillis()
)
