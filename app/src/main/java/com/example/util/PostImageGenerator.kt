package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class PostAspectRatio(
    val label: String,
    val subtitle: String,
    val width: Int,
    val height: Int,
    val ratioFloat: Float
) {
    SQUARE_1_1("1:1 Square", "Instagram Post / Feed", 1080, 1080, 1f),
    PORTRAIT_4_5("4:5 Portrait", "Instagram Feed (Full View)", 1080, 1350, 4f / 5f),
    STORY_9_16("9:16 Story / Reels", "Instagram Story, Reels, Status", 1080, 1920, 9f / 16f),
    LANDSCAPE_16_9("16:9 Banner", "Landscape / Cover Post", 1920, 1080, 16f / 9f)
}

enum class ImageFitMode(val label: String, val description: String) {
    FULL_FIT_COVER("Full Fit / Cover", "Scales & crops to fill entire canvas"),
    FIT_CENTER("Fit Center", "Keeps full photo visible with borders")
}

enum class PostTextAlignment(val label: String, val layoutAlignment: Layout.Alignment) {
    CENTER("Center (बीच में)", Layout.Alignment.ALIGN_CENTER),
    LEFT("Left (बाईं ओर)", Layout.Alignment.ALIGN_NORMAL),
    RIGHT("Right (दाईं ओर)", Layout.Alignment.ALIGN_OPPOSITE)
}

data class PostTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val gradientTop: Int,
    val gradientBottom: Int,
    val goldAccent: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val frameColor: Int
) {
    companion object {
        val THEMES = listOf(
            PostTheme(
                id = "emerald",
                name = "Emerald & Gold",
                subtitle = "Masjid an-Nabawi Vibe",
                gradientTop = Color.parseColor("#062B1B"),
                gradientBottom = Color.parseColor("#02140C"),
                goldAccent = Color.parseColor("#FFD700"),
                textPrimary = Color.parseColor("#FFFFFF"),
                textSecondary = Color.parseColor("#FFE082"),
                frameColor = Color.parseColor("#D4AF37")
            ),
            PostTheme(
                id = "midnight",
                name = "Kaaba Velvet",
                subtitle = "Noir & Golden Calligraphy",
                gradientTop = Color.parseColor("#181818"),
                gradientBottom = Color.parseColor("#050505"),
                goldAccent = Color.parseColor("#F5D77F"),
                textPrimary = Color.parseColor("#FFFFFF"),
                textSecondary = Color.parseColor("#E5C158"),
                frameColor = Color.parseColor("#D4AF37")
            ),
            PostTheme(
                id = "royal_maroon",
                name = "Royal Burgundy",
                subtitle = "Majestic Velvet Rose",
                gradientTop = Color.parseColor("#4A0E17"),
                gradientBottom = Color.parseColor("#1F0408"),
                goldAccent = Color.parseColor("#FFD54F"),
                textPrimary = Color.parseColor("#FFF8F8"),
                textSecondary = Color.parseColor("#FFCDD2"),
                frameColor = Color.parseColor("#E6B800")
            ),
            PostTheme(
                id = "sapphire",
                name = "Celestial Navy",
                subtitle = "Deep Night & Star Light",
                gradientTop = Color.parseColor("#0D2240"),
                gradientBottom = Color.parseColor("#040B17"),
                goldAccent = Color.parseColor("#80D8FF"),
                textPrimary = Color.parseColor("#FFFFFF"),
                textSecondary = Color.parseColor("#B3E5FC"),
                frameColor = Color.parseColor("#4FC3F7")
            ),
            PostTheme(
                id = "turquoise_mosaic",
                name = "Turquoise Arabesque",
                subtitle = "Persian Mosque Dome",
                gradientTop = Color.parseColor("#004D40"),
                gradientBottom = Color.parseColor("#00251A"),
                goldAccent = Color.parseColor("#80CBC4"),
                textPrimary = Color.parseColor("#FFFFFF"),
                textSecondary = Color.parseColor("#B2DFDB"),
                frameColor = Color.parseColor("#4DB6AC")
            ),
            PostTheme(
                id = "vintage_parchment",
                name = "Vintage Parchment",
                subtitle = "Antique Islamic Manuscript",
                gradientTop = Color.parseColor("#FAF7F0"),
                gradientBottom = Color.parseColor("#EFE8DA"),
                goldAccent = Color.parseColor("#0D5C3A"),
                textPrimary = Color.parseColor("#1F2421"),
                textSecondary = Color.parseColor("#2E5B3D"),
                frameColor = Color.parseColor("#8C7853")
            )
        )

        fun findById(id: String): PostTheme = THEMES.find { it.id == id } ?: THEMES[0]
    }
}

object PostImageGenerator {

    /**
     * Loads a Bitmap safely from a content Uri
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                val stream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)
                stream?.close()
                bitmap
            }
        } catch (e: Exception) {
            Log.e("PostImageGenerator", "Error loading bitmap from uri: ${e.message}", e)
            null
        }
    }

    /**
     * Calculates the optimal font size so that the text fits perfectly inside the image content area
     */
    fun calculateOptimalFontSize(
        text: String,
        targetWidth: Int,
        maxHeight: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_CENTER,
        minSp: Float = 18f,
        maxSp: Float = 68f
    ): Float {
        var low = minSp
        var high = maxSp
        var bestSize = low

        while (high - low > 0.8f) {
            val mid = (low + high) / 2f
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = mid * 2.1f // canvas scale multiplier for 1080p
                typeface = Typeface.DEFAULT_BOLD
            }
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, targetWidth)
                .setAlignment(alignment)
                .setLineSpacing(16f, 1.35f)
                .build()

            if (layout.height <= maxHeight) {
                bestSize = mid
                low = mid
            } else {
                high = mid
            }
        }
        return bestSize
    }

    /**
     * Renders a high-resolution 1080p/4K social media post image with ornate Islamic styling
     */
    fun renderPostBitmap(
        context: Context,
        title: String,
        poet: String,
        lyrics: String,
        aspectRatio: PostAspectRatio,
        theme: PostTheme,
        fontSizeSp: Float,
        autoFit: Boolean,
        textAlignment: PostTextAlignment = PostTextAlignment.CENTER,
        customImageBitmap: Bitmap? = null,
        imageDimAlpha: Float = 0.55f,
        imageFitMode: ImageFitMode = ImageFitMode.FULL_FIT_COVER,
        showBismillah: Boolean = true,
        showFrame: Boolean = true,
        showPoetBadge: Boolean = true,
        showWatermark: Boolean = true,
        showFooterCredit: Boolean = true,
        footerCreditText: String = "/Azazmadkiya",
        showFooterSocialIcons: Boolean = true,
        footerWithoutBackground: Boolean = false,
        footerSizeScale: Float = 1.0f,
        footerVerticalOffset: Float = 0f,
        customTextColor: Int? = null
    ): Bitmap {
        val width = aspectRatio.width
        val height = aspectRatio.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background (Custom Photo OR Gradient + Islamic Geometry)
        if (customImageBitmap != null && !customImageBitmap.isRecycled) {
            val imgW = customImageBitmap.width.toFloat()
            val imgH = customImageBitmap.height.toFloat()

            if (imageFitMode == ImageFitMode.FULL_FIT_COVER) {
                // Scale and crop to fill the whole canvas
                val scale = maxOf(width.toFloat() / imgW, height.toFloat() / imgH)
                val scaledW = imgW * scale
                val scaledH = imgH * scale
                val dx = (width - scaledW) / 2f
                val dy = (height - scaledH) / 2f
                val destRect = RectF(dx, dy, dx + scaledW, dy + scaledH)
                val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                canvas.drawBitmap(customImageBitmap, null, destRect, filterPaint)
            } else {
                // Fit center with theme background behind
                val bgPaint = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, 0f, height.toFloat(),
                        theme.gradientTop, theme.gradientBottom,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                val scale = minOf(width.toFloat() / imgW, height.toFloat() / imgH)
                val scaledW = imgW * scale
                val scaledH = imgH * scale
                val dx = (width - scaledW) / 2f
                val dy = (height - scaledH) / 2f
                val destRect = RectF(dx, dy, dx + scaledW, dy + scaledH)
                val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                canvas.drawBitmap(customImageBitmap, null, destRect, filterPaint)
            }

            // Darkening Dim Overlay for optimal text readability over images
            val dimPaint = Paint().apply {
                color = Color.BLACK
                alpha = (imageDimAlpha.coerceIn(0f, 0.95f) * 255).toInt()
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        } else {
            // Gradient Background
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    theme.gradientTop, theme.gradientBottom,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Subtle Islamic Arabesque / Geometric Lattice Lines
            val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                alpha = 18
                strokeWidth = 1.5f
                style = Paint.Style.STROKE
            }
            val step = 120f
            var x = 0f
            while (x <= width) {
                canvas.drawLine(x, 0f, x + height, height.toFloat(), patternPaint)
                canvas.drawLine(x, 0f, x - height, height.toFloat(), patternPaint)
                x += step
            }
        }

        // 2. Ornate Double Gold Border & Islamic Corner Accents
        val margin = (width * 0.045f)
        val innerMargin = margin + 12f
        val rectOuter = RectF(margin, margin, width - margin, height - margin)
        val rectInner = RectF(innerMargin, innerMargin, width - innerMargin, height - innerMargin)

        if (showFrame) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.frameColor
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(rectOuter, 28f, 28f, borderPaint)

            val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                alpha = 140
                style = Paint.Style.STROKE
                strokeWidth = 1.8f
            }
            canvas.drawRoundRect(rectInner, 20f, 20f, innerBorderPaint)

            // Ornate Corner Accents
            val cornerLength = 36f
            val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                style = Paint.Style.STROKE
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
            }
            // Top-Left
            canvas.drawLine(margin - 4f, margin + cornerLength, margin - 4f, margin - 4f, cornerPaint)
            canvas.drawLine(margin - 4f, margin - 4f, margin + cornerLength, margin - 4f, cornerPaint)
            // Top-Right
            canvas.drawLine(width - margin + 4f, margin + cornerLength, width - margin + 4f, margin - 4f, cornerPaint)
            canvas.drawLine(width - margin + 4f, margin - 4f, width - margin - cornerLength, margin - 4f, cornerPaint)
            // Bottom-Left
            canvas.drawLine(margin - 4f, height - margin - cornerLength, margin - 4f, height - margin + 4f, cornerPaint)
            canvas.drawLine(margin - 4f, height - margin + 4f, margin + cornerLength, height - margin + 4f, cornerPaint)
            // Bottom-Right
            canvas.drawLine(width - margin + 4f, height - margin - cornerLength, width - margin + 4f, height - margin + 4f, cornerPaint)
            canvas.drawLine(width - margin + 4f, height - margin + 4f, width - margin - cornerLength, height - margin + 4f, cornerPaint)
        }

        var currentY = margin + 40f

        // 3. Bismillah Header ﷽
        if (showBismillah) {
            val bismillahPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                textSize = 58f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                setShadowLayer(8f, 0f, 2f, Color.argb(160, 0, 0, 0))
            }
            canvas.drawText("﷽", width / 2f, currentY + 45f, bismillahPaint)
            currentY += 80f
        }

        // 4. Title Header
        if (title.isNotBlank()) {
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = customTextColor ?: theme.textSecondary
                textSize = 44f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.03f
                setShadowLayer(6f, 0f, 2f, Color.argb(140, 0, 0, 0))
            }
            val titleText = if (title.length > 40) title.take(37) + "..." else title
            canvas.drawText(titleText, width / 2f, currentY + 36f, titlePaint)
            currentY += 60f
        }

        // 5. Poet Badge (शायर)
        if (poet.isNotBlank() && showPoetBadge) {
            val poetText = "शायर: $poet"
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                alpha = 40
                style = Paint.Style.FILL
            }
            val badgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                alpha = 160
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val poetTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val textWidth = poetTextPaint.measureText(poetText)
            val badgeRect = RectF(
                (width - textWidth) / 2f - 24f,
                currentY + 10f,
                (width + textWidth) / 2f + 24f,
                currentY + 54f
            )
            canvas.drawRoundRect(badgeRect, 18f, 18f, badgePaint)
            canvas.drawRoundRect(badgeRect, 18f, 18f, badgeStrokePaint)
            canvas.drawText(poetText, width / 2f, currentY + 42f, poetTextPaint)
            currentY += 75f
        }

        // 6. Decorative Divider with Center Gold Gem
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.goldAccent
            alpha = 110
            strokeWidth = 2.5f
        }
        val divMargin = width * 0.28f
        canvas.drawLine(divMargin, currentY, width - divMargin, currentY, divPaint)
        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.goldAccent
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, currentY, 5.5f, diamondPaint)
        currentY += 35f

        // 7. Lyrics Content Area with Auto-Fit Calculation
        val footerReserved = if (showWatermark) 100f else 60f
        val availableLyricsHeight = (height - margin - footerReserved - currentY).toInt().coerceAtLeast(200)
        val contentWidth = (width - margin * 2.8f).toInt()

        val actualFontSizeSp = if (autoFit) {
            calculateOptimalFontSize(
                text = lyrics,
                targetWidth = contentWidth,
                maxHeight = availableLyricsHeight,
                alignment = textAlignment.layoutAlignment,
                minSp = 20f,
                maxSp = 58f
            )
        } else {
            fontSizeSp
        }

        val lyricsPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = customTextColor ?: theme.textPrimary
            textSize = actualFontSizeSp * 2.1f
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(8f, 0f, 3f, Color.argb(190, 0, 0, 0))
        }

        val lyricsLayout = StaticLayout.Builder.obtain(lyrics, 0, lyrics.length, lyricsPaint, contentWidth)
            .setAlignment(textAlignment.layoutAlignment)
            .setLineSpacing(16f, 1.35f)
            .build()

        // Center lyrics vertically inside the available height
        val verticalOffset = ((availableLyricsHeight - lyricsLayout.height) / 2f).coerceAtLeast(0f)
        canvas.save()
        canvas.translate((width - contentWidth) / 2f, currentY + verticalOffset)
        lyricsLayout.draw(canvas)
        canvas.restore()

        // 8. Watermark Footer
        if (showWatermark) {
            val footerY = height - margin - 55f
            val watermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.goldAccent
                alpha = 210
                textSize = 26f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.05f
                setShadowLayer(4f, 0f, 2f, Color.argb(120, 0, 0, 0))
            }
            canvas.drawText("✦  NAAT SHARIF  •  READ & SHARE  ✦", width / 2f, footerY, watermarkPaint)
        }

        // 9. Footer Credit & Socials (Matching reference design with size, position & background options)
        if (showFooterCredit) {
            val baseScale = footerSizeScale
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 38f * baseScale
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(6f, 0f, 2f, Color.argb(220, 0, 0, 0))
            }
            
            val iconSize = 48f * baseScale
            val iconSpacing = 14f * baseScale
            val iconsCount = if (showFooterSocialIcons) 3 else 0
            val socialWidth = if (iconsCount > 0) (iconsCount * iconSize) + ((iconsCount - 1) * iconSpacing) else 0f

            val spacing = 24f * baseScale
            val textWidth = if (footerCreditText.isNotBlank()) textPaint.measureText(footerCreditText) else 0f
            val totalWidth = socialWidth + (if (socialWidth > 0f && textWidth > 0f) spacing else 0f) + textWidth

            val startX = (width - totalWidth) / 2f
            val startY = height - margin - 20f + footerVerticalOffset

            if (!footerWithoutBackground) {
                val padH = 32f * baseScale
                val padV = 18f * baseScale
                val bgRect = RectF(
                    startX - padH,
                    startY - maxOf(iconSize, 38f * baseScale) - padV,
                    startX + totalWidth + padH,
                    startY + 15f + padV
                )
                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E6000000")
                    style = Paint.Style.FILL
                }
                val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = theme.goldAccent
                    alpha = 140
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRoundRect(bgRect, 24f, 24f, badgePaint)
                canvas.drawRoundRect(bgRect, 24f, 24f, badgeBorder)
            }

            var cursorX = startX
            val centerY = startY - (iconSize / 2f)

            if (showFooterSocialIcons) {
                val icons = listOf("FB", "INSTA", "YT")
                for (iconType in icons) {
                    val iconRect = RectF(cursorX, centerY - (iconSize / 2f), cursorX + iconSize, centerY + iconSize / 2f)
                    
                    // Background color fill (authentic brand colors)
                    val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = when (iconType) {
                            "FB" -> Color.parseColor("#1877F2")     // Facebook Blue
                            "INSTA" -> Color.parseColor("#C13584")  // Instagram Original Gradient Magenta/Purple
                            else -> Color.parseColor("#FF0000")     // YouTube Red
                        }
                    }
                    canvas.drawRoundRect(iconRect, iconSize * 0.28f, iconSize * 0.28f, brandPaint)

                    // Crisp white border
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        color = Color.WHITE
                        strokeWidth = 2f * baseScale
                    }
                    canvas.drawRoundRect(iconRect, iconSize * 0.28f, iconSize * 0.28f, borderPaint)

                    // Vector Glyph Drawing (Clean SVG paths/shapes)
                    val fillWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = Color.WHITE
                    }
                    val strokeWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        color = Color.WHITE
                        strokeWidth = 2.5f * baseScale
                        strokeCap = Paint.Cap.ROUND
                    }

                    when (iconType) {
                        "FB" -> {
                            val fbTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = Color.WHITE
                                textSize = iconSize * 0.72f
                                typeface = Typeface.DEFAULT_BOLD
                                textAlign = Paint.Align.CENTER
                            }
                            val yPos = iconRect.centerY() - ((fbTextPaint.descent() + fbTextPaint.ascent()) / 2f)
                            canvas.drawText("f", iconRect.centerX() + 2f, yPos, fbTextPaint)
                        }
                        "INSTA" -> {
                            // Original Instagram Camera Icon
                            val camBox = RectF(
                                iconRect.left + iconSize * 0.22f,
                                iconRect.top + iconSize * 0.22f,
                                iconRect.right - iconSize * 0.22f,
                                iconRect.bottom - iconSize * 0.22f
                            )
                            canvas.drawRoundRect(camBox, iconSize * 0.3f, iconSize * 0.3f, strokeWhite)
                            // Inner lens circle
                            canvas.drawCircle(iconRect.centerX(), iconRect.centerY(), iconSize * 0.17f, strokeWhite)
                            // Flash dot top-right
                            canvas.drawCircle(iconRect.right - iconSize * 0.32f, iconRect.top + iconSize * 0.32f, iconSize * 0.07f, fillWhite)
                        }
                        "YT" -> {
                            // YouTube Play Triangle
                            val ytPath = Path().apply {
                                moveTo(iconRect.centerX() - iconSize * 0.18f, iconRect.centerY() - iconSize * 0.22f)
                                lineTo(iconRect.centerX() + iconSize * 0.22f, iconRect.centerY())
                                lineTo(iconRect.centerX() - iconSize * 0.18f, iconRect.centerY() + iconSize * 0.22f)
                                close()
                            }
                            canvas.drawPath(ytPath, fillWhite)
                        }
                    }

                    cursorX += iconSize + iconSpacing
                }
                cursorX += spacing - iconSpacing
            }

            if (footerCreditText.isNotBlank()) {
                canvas.drawText(footerCreditText, cursorX, startY, textPaint)
            }
        }

        return bitmap
    }

    /**
     * Saves bitmap to internal cache and returns a shareable content Uri via FileProvider
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "naat_post_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.e("PostImageGenerator", "Failed to cache bitmap for sharing: ${e.message}", e)
            null
        }
    }

    /**
     * Saves the post image into the device MediaStore / Pictures directory
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Result<String> {
        val fileName = "Naat_Post_${System.currentTimeMillis()}.png"
        return try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NaatSharif")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return Result.failure(Exception("Failed to create MediaStore entry"))

                outputStream = context.contentResolver.openOutputStream(uri)
                outputStream?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "NaatSharif").apply { mkdirs() }
                val file = File(appDir, fileName)
                outputStream = FileOutputStream(file)
                outputStream.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
            Result.success("Post saved to Gallery / Pictures/NaatSharif")
        } catch (e: Exception) {
            Log.e("PostImageGenerator", "Error saving to gallery: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Launches Android Share Intent with the generated post image
     */
    fun sharePost(context: Context, bitmap: Bitmap, caption: String) {
        val uri = saveBitmapToCache(context, bitmap)
        if (uri == null) {
            Log.e("PostImageGenerator", "Cannot share: Uri is null")
            return
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Naat Post to Instagram / WhatsApp")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
