package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted

@Composable
fun CameraStreamView(
    modifier: Modifier = Modifier,
    isFrontFacing: Boolean = false,
    hasCameraPermission: Boolean = true,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCameraBound by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(DarkBackground)
            .fillMaxSize()
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = if (isFrontFacing) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                            isCameraBound = true
                        } catch (e: Exception) {
                            isCameraBound = false
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // If camera not bound or running on emulator without virtual sensor, show athletic track feed
        if (!hasCameraPermission || !isCameraBound) {
            AthleticTrackBackground(modifier = Modifier.fillMaxSize())
        }

        overlayContent()
    }
}

@Composable
fun AthleticTrackBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(androidx.compose.ui.graphics.Color(0xFF0A0D14)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Synthetic track lanes
            val laneWidth = w / 4
            for (i in 0..4) {
                val x = i * laneWidth
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0x3300E5FF),
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, h),
                    strokeWidth = 2f
                )
            }

            // Central finish line
            drawLine(
                color = androidx.compose.ui.graphics.Color(0x44FF1E27),
                start = androidx.compose.ui.geometry.Offset(w / 2, 0f),
                end = androidx.compose.ui.geometry.Offset(w / 2, h),
                strokeWidth = 2f
            )
        }
    }
}

/**
 * Creates a synthetic high-resolution bitmap representing a runner frame for testing or AI analysis
 */
fun createSampleRunnerBitmap(athleteName: String, phase: String): Bitmap {
    val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Dark track background
    val bgPaint = Paint().apply { color = Color.rgb(12, 16, 24) }
    canvas.drawRect(0f, 0f, 640f, 480f, bgPaint)

    // Red running track surface
    val trackPaint = Paint().apply { color = Color.rgb(139, 35, 35) }
    canvas.drawRect(0f, 280f, 640f, 480f, trackPaint)

    // Lane lines
    val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
    }
    canvas.drawLine(0f, 340f, 640f, 340f, linePaint)
    canvas.drawLine(0f, 420f, 640f, 420f, linePaint)

    // Runner silhouette / stick figure in sprint pose
    val runnerPaint = Paint().apply {
        color = Color.rgb(0, 229, 255)
        strokeWidth = 8f
        isAntiAlias = true
    }

    // Head
    canvas.drawCircle(320f, 160f, 22f, runnerPaint)
    // Torso (leaning forward 20 deg)
    canvas.drawLine(320f, 182f, 290f, 270f, runnerPaint)
    // Front Leg (high knee drive)
    canvas.drawLine(290f, 270f, 340f, 300f, runnerPaint)
    canvas.drawLine(340f, 300f, 330f, 360f, runnerPaint)
    // Back Leg (extension)
    canvas.drawLine(290f, 270f, 250f, 320f, runnerPaint)
    canvas.drawLine(250f, 320f, 220f, 370f, runnerPaint)
    // Arms (sprint arm swing 90 deg)
    canvas.drawLine(310f, 205f, 350f, 220f, runnerPaint)
    canvas.drawLine(350f, 220f, 340f, 175f, runnerPaint)
    canvas.drawLine(310f, 205f, 270f, 225f, runnerPaint)
    canvas.drawLine(270f, 225f, 260f, 260f, runnerPaint)

    // Text details
    val textPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 24f
        isAntiAlias = true
    }
    canvas.drawText("PSYCO TIMING AI CAPTURE", 20f, 40f, textPaint)
    textPaint.color = Color.WHITE
    textPaint.textSize = 18f
    canvas.drawText("ATLET: $athleteName | FASA: $phase", 20f, 70f, textPaint)
    canvas.drawText("WATERMARK: www.psycotimexpro.my", 20f, 100f, textPaint)

    return bitmap
}
