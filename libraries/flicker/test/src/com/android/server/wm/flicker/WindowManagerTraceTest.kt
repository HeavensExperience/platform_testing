/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wm.flicker

import com.android.server.wm.flicker.assertions.assertFailed
import com.android.server.wm.flicker.assertions.assertPassed
import com.android.server.wm.flicker.common.Region
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTrace
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTrace.Companion.parseFrom
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTrace.Companion.parseFromDump
import com.google.common.truth.Truth
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * Contains [WindowManagerTrace] tests. To run this test: `atest
 * FlickerLibTest:WindowManagerTraceTest`
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class WindowManagerTraceTest {
    private val trace: WindowManagerTrace =
        readWindowManagerTraceFromFile("wm_trace_openchrome.pb")

    @Test
    fun canParseAllEntries() {
        val firstEntry = trace.entries[0]
        Truth.assertThat(firstEntry.timestamp).isEqualTo(9213763541297L)
        Truth.assertThat(firstEntry.windows.size).isEqualTo(10)
        Truth.assertThat(firstEntry.visibleWindows.size).isEqualTo(6)
        Truth.assertThat(trace.entries[trace.entries.size - 1].timestamp)
                .isEqualTo(9216093628925L)
    }

    @Test
    fun canDetectAboveAppWindowVisibility() {
        val entry = trace.getEntry(9213763541297L)
        entry.isAboveAppWindow("NavigationBar").assertPassed()
        entry.isAboveAppWindow("ScreenDecorOverlay").assertPassed()
        entry.isAboveAppWindow("StatusBar").assertPassed()
        entry.isAboveAppWindow("pip-dismiss-overlay").assertFailed("is invisible")
        entry.isAboveAppWindow("NotificationShade").assertFailed("is invisible")
        entry.isAboveAppWindow("InputMethod").assertFailed("is invisible")
        entry.isAboveAppWindow("AssistPreviewPanel").assertFailed("is invisible")
    }

    @Test
    fun canDetectWindowCoversAtLeastRegion() {
        val entry = trace.getEntry(9213763541297L)
        // Exact size
        entry.coversAtLeastRegion("StatusBar", Region(0, 0, 1440, 171)).assertPassed()
        entry.coversAtLeastRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2960))
                .assertPassed()
        // Smaller region
        entry.coversAtLeastRegion("StatusBar", Region(0, 0, 100, 100)).assertPassed()
        entry.coversAtLeastRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 100, 100))
                .assertPassed()
        // Larger region
        entry.coversAtLeastRegion("StatusBar", Region(0, 0, 1441, 171))
                .assertFailed("Uncovered region: SkRegion((1440,0,1441,171))")
        entry.coversAtLeastRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2961))
                .assertFailed("Uncovered region: SkRegion((0,2960,1440,2961))")
    }

    @Test
    fun canDetectWindowCoversAtMostRegion() {
        val entry = trace.getEntry(9213763541297L)
        // Exact size
        entry.coversAtMostRegion("StatusBar", Region(0, 0, 1440, 171)).assertPassed()
        entry.coversAtMostRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2960))
                .assertPassed()
        // Smaller region
        entry.coversAtMostRegion("StatusBar", Region(0, 0, 100, 100))
                .assertFailed("Out-of-bounds region: SkRegion((100,0,1440,100)(0,100,1440,171))")
        entry.coversAtMostRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 100, 100))
                .assertFailed("Out-of-bounds region: SkRegion((100,0,1440,100)(0,100,1440,2960))")
        // Larger region
        entry.coversAtMostRegion("StatusBar", Region(0, 0, 1441, 171)).assertPassed()
        entry.coversAtMostRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2961))
                .assertPassed()
    }

    @Test
    fun canDetectBelowAppWindowVisibility() {
        trace.getEntry(9213763541297L).hasNonAppWindow("wallpaper").assertPassed()
    }

    @Test
    fun canDetectAppWindow() {
        val appWindows = trace.getEntry(9213763541297L).appWindows
        Truth.assertWithMessage("Unable to detect app windows").that(appWindows.size).isEqualTo(2)
    }

    @Test
    fun canDetectAppWindowVisibility() {
        trace.getEntry(9213763541297L)
                .isAppWindowVisible("com.google.android.apps.nexuslauncher").assertPassed()
        trace.getEntry(9215551505798L).isAppWindowVisible("com.android.chrome").assertPassed()
    }

    @Test
    fun canFailWithReasonForVisibilityChecks_windowNotFound() {
        trace.getEntry(9213763541297L)
                .hasNonAppWindow("ImaginaryWindow")
                .assertFailed("ImaginaryWindow cannot be found")
    }

    @Test
    fun canFailWithReasonForVisibilityChecks_windowNotVisible() {
        trace.getEntry(9213763541297L)
                .hasNonAppWindow("InputMethod")
                .assertFailed("InputMethod is invisible")
    }

    @Test
    fun canDetectAppZOrder() {
        trace.getEntry(9215551505798L)
                .isAppWindowVisible("com.google.android.apps.nexuslauncher")
                .assertPassed()
        trace.getEntry(9215551505798L)
                .isVisibleAppWindowOnTop("com.android.chrome").assertPassed()
    }

    @Test
    fun canFailWithReasonForZOrderChecks_windowNotOnTop() {
        trace.getEntry(9215551505798L)
                .isVisibleAppWindowOnTop("com.google.android.apps.nexuslauncher")
                .assertFailed("wanted=com.google.android.apps.nexuslauncher")
        trace.getEntry(9215551505798L)
                .isVisibleAppWindowOnTop("com.google.android.apps.nexuslauncher")
                .assertFailed("found=Splash Screen com.android.chrome")
    }

    @Test
    fun canParseFromDump() {
        val trace = try {
            parseFromDump(readTestFile("wm_trace_dump.pb"))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        Truth.assertWithMessage("Unable to parse dump").that(trace.entries).hasSize(1)
    }

    companion object {
        private fun readWindowManagerTraceFromFile(relativePath: String): WindowManagerTrace {
            return try {
                parseFrom(readTestFile(relativePath))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}