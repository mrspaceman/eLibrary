/*
 * Decompiled with CFR 0_115.
 */
package uk.co.droidinactu.ebooklibrary.library.mobimeta;

public class MobiCommon {
    public static boolean debug = false;
    public static boolean safeMode = false;

    public static void logMessage(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}

