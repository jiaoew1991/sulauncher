/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.sulauncher.supad;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for SuPad and its settings.
 */
public class SuPadUtils {
    @SuppressWarnings("unused")
	private static final String TAG = "LockPatternUtils";
    /**
     * @param contentResolver Used to look up and save settings.
     */
    public SuPadUtils(Context context) {
    }

    /**
     * Deserialize a pattern.
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<SuPadView.Cell> stringToPattern(String string) {
        List<SuPadView.Cell> result = new ArrayList<SuPadView.Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(SuPadView.Cell.of(b / 4, b % 4));
        }
        return result;
    }

    /**
     * Serialize a pattern.
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<SuPadView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            SuPadView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 4 + cell.getColumn());
        }
        return new String(res);
    }
}
