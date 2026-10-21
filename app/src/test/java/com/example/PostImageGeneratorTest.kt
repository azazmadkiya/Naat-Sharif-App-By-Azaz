package com.example

import android.graphics.Bitmap
import com.example.util.ImageFitMode
import com.example.util.PostAspectRatio
import com.example.util.PostImageGenerator
import com.example.util.PostTextAlignment
import com.example.util.PostTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PostImageGeneratorTest {

    @Test
    fun testAllAspectRatiosRenderExpectedDimensions() {
        PostAspectRatio.values().forEach { ratio ->
            val bitmap = PostImageGenerator.renderPostBitmap(
                title = "फासला को तकल्लुफ है हमसे अगर",
                poet = "हजरत अल्लामा इक़बाल",
                lyrics = "हम भी मदीना जाएंगे, आज नहीं तो कल\nआका हमें बुलाएंगे, आज नहीं तो कल",
                aspectRatio = ratio,
                theme = PostTheme.THEMES[0],
                fontSizeSp = 24f,
                autoFit = true,
                textAlignment = PostTextAlignment.CENTER
            )

            assertNotNull("Bitmap for ${ratio.name} should not be null", bitmap)
            assertEquals("Width should match aspect ratio", ratio.width, bitmap.width)
            assertEquals("Height should match aspect ratio", ratio.height, bitmap.height)
        }
    }

    @Test
    fun testAutoFitCalculatesPositiveOptimalFontSize() {
        val calculatedSize = PostImageGenerator.calculateOptimalFontSize(
            text = "मुस्तफा जाने रहमत पे लाखों सलाम\nशमए बज़्मे हिदायत पे लाखों सलाम",
            targetWidth = 800,
            maxHeight = 600,
            minSp = 18f,
            maxSp = 58f
        )
        assertTrue("Calculated font size should be >= 18sp", calculatedSize >= 18f)
        assertTrue("Calculated font size should be <= 58sp", calculatedSize <= 58f)
    }

    @Test
    fun testRenderWithCustomImageBitmap() {
        val sampleBmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val postBmp = PostImageGenerator.renderPostBitmap(
            title = "अजमेर वाले ख्वाजा",
            poet = "सूफी",
            lyrics = "तेरा दर हो और मेरा सर हो",
            aspectRatio = PostAspectRatio.SQUARE_1_1,
            theme = PostTheme.THEMES[1],
            fontSizeSp = 22f,
            autoFit = false,
            customImageBitmap = sampleBmp,
            imageDimAlpha = 0.6f,
            imageFitMode = ImageFitMode.FULL_FIT_COVER
        )

        assertNotNull(postBmp)
        assertEquals(1080, postBmp.width)
        assertEquals(1080, postBmp.height)
    }
}
