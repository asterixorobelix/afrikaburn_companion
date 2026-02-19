package io.asterixorobelix.afrikaburn

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab

@RunWith(AndroidJUnit4::class)
class ScreengrabScreenshotTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureCoreScreens() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Screengrab.screenshot("home")

        composeRule.onNodeWithContentDescription("Explore projects").performClick()
        composeRule.waitForIdle()
        Screengrab.screenshot("explore")

        composeRule.onNodeWithContentDescription("View map of event area").performClick()
        composeRule.waitForIdle()
        Screengrab.screenshot("map")

        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.waitForIdle()
        Screengrab.screenshot("more")

        composeRule.onNodeWithText("About").performClick()
        composeRule.waitForIdle()
        Screengrab.screenshot("about")
    }
}
