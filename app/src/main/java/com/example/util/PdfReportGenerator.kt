package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.AthleteEntity
import com.example.data.local.entity.CoachAccountEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateAthletesPdf(
        context: Context,
        athletes: List<AthleteEntity>,
        coach: CoachAccountEntity?
    ): File {
        val pdfDocument = PdfDocument()
        val pageNumber = 1
        val todayStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val pageWidth = 595 // A4 standard width (points)
        val pageHeight = 842 // A4 standard height (points)

        val titlePaint = Paint().apply {
            color = Color.rgb(229, 9, 20) // Racing Red
            textSize = 18f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 10f
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(18, 18, 20) // Dark Surface
        }

        val redBarPaint = Paint().apply {
            color = Color.rgb(229, 9, 20)
        }

        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(229, 9, 20)
            textSize = 12f
            isFakeBoldText = true
        }

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 9.5f
            isFakeBoldText = true
        }

        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 10.5f
            isFakeBoldText = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val boxBgPaint = Paint().apply {
            color = Color.rgb(248, 248, 250)
            style = Paint.Style.FILL
        }

        val watermarkPaint = Paint().apply {
            color = Color.argb(22, 0, 0, 0)
            textSize = 34f
            isFakeBoldText = true
        }

        // Each athlete gets an official formatted page
        athletes.forEachIndexed { index, athlete ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // 1. Watermark
            canvas.drawText("PSYCO TIME X PRO", 130f, 440f, watermarkPaint)

            // 2. Top Header Bar
            canvas.drawRect(Rect(0, 0, pageWidth, 75), headerBgPaint)
            canvas.drawRect(Rect(0, 75, pageWidth, 79), redBarPaint)

            val pTitle = Paint().apply {
                color = Color.WHITE
                textSize = 16f
                isFakeBoldText = true
            }
            canvas.drawText("PSYCO TIME X PRO • DOSSIER RASMI ATLIT", 30f, 32f, pTitle)

            val pClub = Paint().apply {
                color = Color.rgb(255, 215, 0) // Gold
                textSize = 10.5f
                isFakeBoldText = true
            }
            val clubText = if (!coach?.clubName.isNullOrBlank()) coach?.clubName!! else "Kelab Olahraga & Balapan"
            canvas.drawText("KELAB: $clubText | JURULATIH: ${coach?.name ?: "Pusat Latihan"}", 30f, 50f, pClub)

            val pDate = Paint().apply {
                color = Color.rgb(180, 180, 180)
                textSize = 9f
            }
            canvas.drawText("Tarikh Cetakan: $todayStr • Halaman ${index + 1}/${athletes.size}", 30f, 66f, pDate)

            // 3. Athlete Profile Card Header
            var y = 100f
            canvas.drawText("1. MAKLUMAT PERIBADI ATLIT", 30f, y, sectionHeaderPaint)
            y += 12f

            // Frame Box for Personal Info
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 155f, boxBgPaint)
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 155f, borderPaint)

            // Photo frame on the right
            val photoBoxLeft = (pageWidth - 145).toFloat()
            val photoBoxTop = y + 12f
            val photoBoxRight = (pageWidth - 45).toFloat()
            val photoBoxBottom = y + 135f
            canvas.drawRect(photoBoxLeft, photoBoxTop, photoBoxRight, photoBoxBottom, borderPaint)

            val avatarTextPaint = Paint().apply {
                color = Color.rgb(180, 180, 180)
                textSize = 9f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("FOTO ATLIT", photoBoxLeft + 50f, photoBoxTop + 60f, avatarTextPaint)
            canvas.drawText("(MANDATORI)", photoBoxLeft + 50f, photoBoxTop + 74f, avatarTextPaint)

            // Details list on the left
            var dY = y + 22f
            fun drawField(label: String, value: String) {
                canvas.drawText(label, 45f, dY, labelPaint)
                canvas.drawText(value, 195f, dY, valuePaint)
                dY += 20f
            }

            drawField("Nama Penuh:", athlete.name)
            drawField("No. Kad Pengenalan / SB:", if (athlete.icNumber.isNotBlank()) athlete.icNumber else "-")
            drawField("Umur & Tarikh Lahir:", "${athlete.age} Tahun (${athlete.dob.ifEmpty { "-" }})")
            drawField("Jantina:", athlete.gender)
            drawField("Fizikal (Tinggi / Berat):", "${athlete.heightCm} cm / ${athlete.weightKg} kg")
            drawField("No. Telefon / Waris:", athlete.phone.ifEmpty { "-" })

            y += 175f

            // 4. Sports & Performance Section
            canvas.drawText("2. REKOD PRESTASI & PENGKHUSUSAN SUKAN", 30f, y, sectionHeaderPaint)
            y += 12f

            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 130f, boxBgPaint)
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 130f, borderPaint)

            dY = y + 24f
            drawField("Jenis Sukan:", athlete.sportType.uppercase())
            drawField("Acara Pengkhususan:", athlete.category)

            val pbValue = if (athlete.sportType == "Balapan") {
                "${String.format(Locale.US, "%.2f", athlete.pbSeconds)} Saat (Personal Best)"
            } else {
                "${String.format(Locale.US, "%.2f", athlete.distanceOrScore)} Meter (Personal Best)"
            }
            val pPb = Paint().apply {
                color = Color.rgb(0, 150, 60)
                textSize = 11f
                isFakeBoldText = true
            }
            canvas.drawText("Catatan Masa / Jarak PB:", 45f, dY, labelPaint)
            canvas.drawText(pbValue, 195f, dY, pPb)
            dY += 22f

            drawField("Status Yuran Bulanan:", "RM ${String.format(Locale.US, "%.2f", athlete.monthlyFee)} / Bulan (Matang: ${athlete.feeDueDate.ifEmpty { "1hb" }})")
            drawField("Catatan Khas:", athlete.notes.ifEmpty { "Atlit aktif dalam program latihan berprestasi tinggi." })

            y += 150f

            // 5. Electronic Timing System Verification Box
            canvas.drawText("3. PENGESAHAN SISTEM ELECTRONIC TIMING (ET)", 30f, y, sectionHeaderPaint)
            y += 12f

            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 105f, boxBgPaint)
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 105f, borderPaint)

            val verifyTextPaint = Paint().apply {
                color = Color.rgb(60, 60, 60)
                textSize = 9.5f
            }
            canvas.drawText("• Rekod catatan masa atlit ini disahkan secara automatik melalui sistem Psyco Time X Pro.", 45f, y + 22f, verifyTextPaint)
            canvas.drawText("• Dilengkapi sokongan Cam 1 (Start / Biomekanik Larian) dan Cam 2 (Time Gate / Finisher).", 45f, y + 38f, verifyTextPaint)
            canvas.drawText("• Torso Gate piawaian World Athletics diiktiraf bagi merekodkan pencapaian rasmi atlit.", 45f, y + 54f, verifyTextPaint)
            canvas.drawText("• Sijil & dokumen ini sah sebagai bukti pendaftaran bagi kejohanan MSSD/MSSN/Terbuka.", 45f, y + 70f, verifyTextPaint)

            y += 135f

            // 6. Signature Block
            val signPaint = Paint().apply {
                color = Color.rgb(80, 80, 80)
                textSize = 9.5f
                isFakeBoldText = true
            }
            canvas.drawLine(40f, y + 50f, 210f, y + 50f, borderPaint)
            canvas.drawText("Tandatangan Jurulatih", 60f, y + 65f, signPaint)
            canvas.drawText("(${coach?.name ?: "Ketua Jurulatih"})", 55f, y + 78f, subtitlePaint)

            canvas.drawLine((pageWidth - 210).toFloat(), y + 50f, (pageWidth - 40).toFloat(), y + 50f, borderPaint)
            canvas.drawText("Cop Rasmi Kelab / Akademi", (pageWidth - 195).toFloat(), y + 65f, signPaint)
            canvas.drawText("(Tarikh: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())})", (pageWidth - 180).toFloat(), y + 78f, subtitlePaint)

            // 7. Footer
            val footerBg = Paint().apply { color = Color.rgb(240, 240, 242) }
            canvas.drawRect(0f, (pageHeight - 28).toFloat(), pageWidth.toFloat(), pageHeight.toFloat(), footerBg)
            val footerText = Paint().apply {
                color = Color.rgb(100, 100, 100)
                textSize = 8.5f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Sistem Pengurusan Latihan & Olahraga Psyco Time X Pro • www.psycotimexpro.my", (pageWidth / 2).toFloat(), (pageHeight - 11).toFloat(), footerText)

            pdfDocument.finishPage(page)
        }

        val outputFile = File(context.cacheDir, "Dossier_Atlit_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return outputFile
    }

    fun shareOrViewPdf(context: Context, pdfFile: File, title: String = "Laporan Biodata Atlit") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Dossier Biodata Atlit Rasmi - Psyco Time X Pro")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(viewIntent, "Buka atau Kongsi Dokumen PDF ($title)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
