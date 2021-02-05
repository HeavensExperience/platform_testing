/*
 * Copyright (C) 2021 The Android Open Source Project
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

package android.platform.tests;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.app.UiModeManager;
import android.platform.helpers.AutoUtility;
import android.platform.helpers.IAutoAppInfoSettingsHelper;
import android.platform.helpers.IAutoSettingHelper;
import android.platform.helpers.IAutoSettingHelper.DayNightMode;
import android.platform.helpers.HelperAccessor;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SettingTest {
    private static final int DAY_MODE_VALUE = 0;
    private HelperAccessor<IAutoAppInfoSettingsHelper> mAppInfoSettingsHelper;
    private HelperAccessor<IAutoSettingHelper> mSettingHelper;

    public SettingTest() throws Exception {
        mAppInfoSettingsHelper = new HelperAccessor<>(IAutoAppInfoSettingsHelper.class);
        mSettingHelper = new HelperAccessor<>(IAutoSettingHelper.class);
    }

    @BeforeClass
    public static void exitSuw() {
        AutoUtility.exitSuw();
    }

    @Before
    public void closeSettingsApplicationIfOpen() {
        mSettingHelper.get().stopSettingsApplication();
    }

    @After
    public void goBackToSettingsScreen() {
        mSettingHelper.get().goBackToSettingsScreen();
    }

    @Test
    public void testDisplaySettings() {
        mSettingHelper.get().openSetting(AutoUtility.DISPLAY_SETTINGS);
    }

    @Test
    public void testSoundSettings() {
        mSettingHelper.get().openSetting(AutoUtility.SOUND_SETTINGS);
    }

    @Test
    public void testAppinfoSettings() {
        mSettingHelper.get().openSetting(AutoUtility.APPS_AND_NOTIFICATIONS_SETTINGS);
        mAppInfoSettingsHelper.get().showAllApps();
    }

    @Test
    public void testDateTimeSettings() {
        mSettingHelper.get().openSetting(AutoUtility.DATE_AND_TIME_SETTINGS);
    }

    @Test
    public void testUsersSettings() {
        mSettingHelper.get().openSetting(AutoUtility.USER_SETTINGS);
    }

    @Test
    public void testAccountsSettings() {
        mSettingHelper.get().openSetting(AutoUtility.ACCOUNT_SETTINGS);
    }

    @Test
    public void testSystemSettings() {
        mSettingHelper.get().openSetting(AutoUtility.SYSTEM_SETTINGS);
    }

    @Test
    public void testBluetoothSettings() {
        mSettingHelper.get().openSetting(AutoUtility.BLUETOOTH_SETTINGS);
        mSettingHelper.get().turnOnOffBluetooth(false);
        assertFalse(mSettingHelper.get().isBluetoothOn());
        mSettingHelper.get().turnOnOffBluetooth(true);
    }

    @Test
    public void testDayMode() {
        mSettingHelper.get().openQuickSettings();
        mSettingHelper.get().setDayNightMode(DayNightMode.DAY_MODE);
        assertThat(mSettingHelper.get().getDayNightModeStatus().getValue(), is(DAY_MODE_VALUE));
    }

    @Test
    public void testNightMode() {
        mSettingHelper.get().openQuickSettings();
        mSettingHelper.get().setDayNightMode(DayNightMode.NIGHT_MODE);
        assertThat(
                mSettingHelper.get().getDayNightModeStatus().getValue(),
                is(UiModeManager.MODE_NIGHT_YES));
    }
}