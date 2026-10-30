package com.example.nativeengine;

import android.util.Log;

public class NativeBridge {

    private static final String TAG = "NativeBridge";
    private static boolean isLoaded = false;

    static {
        try {
            System.loadLibrary("mindvault");
            isLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "Native library 'mindvault' not loaded, using embedded fallback engine: " + e.getMessage());
            isLoaded = false;
        }
    }

    private static native String getEngineVersionNative();
    private static native int calculateTextLengthNative(String text);

    public boolean isNativeLoaded() {
        return isLoaded;
    }

    public String getEngineVersion() {
        if (isLoaded) {
            try {
                return getEngineVersionNative();
            } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "Fallback to Java implementation for getEngineVersion", e);
            }
        }
        return "MindVault Native Engine 1.0 (Embedded)";
    }

    public int calculateTextLength(String text) {
        if (text == null) {
            return 0;
        }
        if (isLoaded) {
            try {
                return calculateTextLengthNative(text);
            } catch (UnsatisfiedLinkError e) {
                Log.w(TAG, "Fallback to Java implementation for calculateTextLength", e);
            }
        }
        return text.length();
    }

    public int calculateWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    public int estimateReadingTimeMinutes(String text) {
        int words = calculateWordCount(text);
        if (words == 0) return 0;
        return Math.max(1, (int) Math.ceil(words / 200.0));
    }
}
