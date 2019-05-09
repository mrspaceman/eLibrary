/*
 * Decompiled with CFR 0_115.
 */
package uk.co.droidinactu.elibrary.library.mobimeta;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class StreamUtils {
    public static String readCString(InputStream in, int len) throws IOException {
        byte[] buffer = new byte[len];
        int bytesLeft = len;
        int offset = 0;
        while (bytesLeft > 0) {
            int bytesRead = in.read(buffer, offset, bytesLeft);
            if (bytesRead == -1) {
                throw new IOException("Supposed to read a " + len + " byte C string, but could not");
            }
            offset += bytesRead;
            bytesLeft -= bytesRead;
        }
        String s = StreamUtils.byteArrayToString(buffer);
        MobiCommon.logMessage("readCString: " + s);
        return s;
    }

    public static String byteArrayToString(byte[] buffer) {
        return StreamUtils.byteArrayToString(buffer, null);
    }

    public static String byteArrayToString(byte[] buffer, String encoding) {
        int len = buffer.length;
        int zeroIndex = -1;
        int i = 0;
        while (i < len) {
            byte b = buffer[i];
            if (b == 0) {
                zeroIndex = i;
                break;
            }
            ++i;
        }
        if (encoding != null) {
            try {
                if (zeroIndex == -1) {
                    return new String(buffer, encoding);
                }
                return new String(buffer, 0, zeroIndex, encoding);
            } catch (UnsupportedEncodingException uee) {
                // empty catch block
            }
        }
        if (zeroIndex == -1) {
            return new String(buffer);
        }
        return new String(buffer, 0, zeroIndex);
    }

    public static byte readByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new IOException("Supposed to read a byte, but could not");
        }
        MobiCommon.logMessage("readByte: " + b);
        return (byte) (b & 255);
    }

    public static void readByteArray(InputStream in, byte[] buffer) throws IOException {
        int len;
        int bytesLeft = len = buffer.length;
        int offset = 0;
        while (bytesLeft > 0) {
            int bytesRead = in.read(buffer, offset, bytesLeft);
            if (bytesRead == -1) {
                throw new IOException("Supposed to read a " + len + " byte array, but could not");
            }
            offset += bytesRead;
            bytesLeft -= bytesRead;
        }
        if (MobiCommon.debug) {
            MobiCommon.logMessage(StreamUtils.dumpByteArray(buffer));
        }
    }

    public static String dumpByteArray(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        int len = buffer.length;
        int i = 0;
        while (i < len) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(buffer[i] & 255);
            ++i;
        }
        sb.append(" }");
        return sb.toString();
    }

    public static int byteArrayToInt(byte[] buffer) {
        int total = 0;
        int len = buffer.length;
        int i = 0;
        while (i < len) {
            total = (total << 8) + (buffer[i] & 255);
            ++i;
        }
        return total;
    }

    public static long byteArrayToLong(byte[] buffer) {
        long total = 0;
        int len = buffer.length;
        int i = 0;
        while (i < len) {
            total = (total << 8) + (long) (buffer[i] & 255);
            ++i;
        }
        return total;
    }

    public static void intToByteArray(int value, byte[] dest) {
        int lastIndex;
        int i = lastIndex = dest.length - 1;
        while (i >= 0) {
            dest[i] = (byte) (value & 255);
            value >>= 8;
            --i;
        }
    }

    public static void longToByteArray(long value, byte[] dest) {
        int lastIndex;
        int i = lastIndex = dest.length - 1;
        while (i >= 0) {
            dest[i] = (byte) (value & 255);
            value >>= 8;
            --i;
        }
    }

    public static byte[] stringToByteArray(String s) {
        return StreamUtils.stringToByteArray(s, null);
    }

    public static byte[] stringToByteArray(String s, String encoding) {
        if (encoding != null) {
            try {
                return s.getBytes(encoding);
            } catch (UnsupportedEncodingException var2_2) {
                // empty catch block
            }
        }
        return s.getBytes();
    }
}

