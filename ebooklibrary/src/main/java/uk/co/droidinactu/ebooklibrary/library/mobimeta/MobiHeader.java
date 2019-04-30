/*
 * Decompiled with CFR 0_115.
 */
package uk.co.droidinactu.ebooklibrary.library.mobimeta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MobiHeader {
    private byte[] compression = new byte[2];
    private byte[] unused0 = new byte[2];
    private byte[] textLength = new byte[4];
    private byte[] recordCount = new byte[2];
    private byte[] recordSize = new byte[2];
    private byte[] encryptionType = new byte[2];
    private byte[] unused1 = new byte[2];
    private byte[] identifier = new byte[4];
    private byte[] headerLength = new byte[4];
    private byte[] mobiType = new byte[4];
    private byte[] textEncoding = new byte[4];
    private byte[] uniqueID = new byte[4];
    private byte[] fileVersion = new byte[4];
    private byte[] orthographicIndex = new byte[4];
    private byte[] inflectionIndex = new byte[4];
    private byte[] indexNames = new byte[4];
    private byte[] indexKeys = new byte[4];
    private byte[] extraIndex0 = new byte[4];
    private byte[] extraIndex1 = new byte[4];
    private byte[] extraIndex2 = new byte[4];
    private byte[] extraIndex3 = new byte[4];
    private byte[] extraIndex4 = new byte[4];
    private byte[] extraIndex5 = new byte[4];
    private byte[] firstNonBookIndex = new byte[4];
    private byte[] fullNameOffset = new byte[4];
    private byte[] fullNameLength = new byte[4];
    private byte[] locale = new byte[4];
    private byte[] inputLanguage = new byte[4];
    private byte[] outputLanguage = new byte[4];
    private byte[] minVersion = new byte[4];
    private byte[] firstImageIndex = new byte[4];
    private byte[] huffmanRecordOffset = new byte[4];
    private byte[] huffmanRecordCount = new byte[4];
    private byte[] huffmanTableOffset = new byte[4];
    private byte[] huffmanTableLength = new byte[4];
    private byte[] exthFlags = new byte[4];
    private byte[] restOfMobiHeader = null;
    private EXTHHeader exthHeader = null;
    private byte[] remainder = null;
    private byte[] fullName = null;
    private String characterEncoding = null;

    public MobiHeader(InputStream in, long mobiHeaderSize) throws IOException {
        MobiCommon.logMessage("*** MobiHeader ***");
        MobiCommon.logMessage("compression");
        StreamUtils.readByteArray(in, this.compression);
        StreamUtils.readByteArray(in, this.unused0);
        StreamUtils.readByteArray(in, this.textLength);
        StreamUtils.readByteArray(in, this.recordCount);
        StreamUtils.readByteArray(in, this.recordSize);
        MobiCommon.logMessage("encryptionType");
        StreamUtils.readByteArray(in, this.encryptionType);
        StreamUtils.readByteArray(in, this.unused1);
        StreamUtils.readByteArray(in, this.identifier);
        if (MobiCommon.debug) {
            MobiCommon.logMessage("identifier: " + StreamUtils.byteArrayToString(this.identifier));
        }
        if (this.identifier[0] != 77 || this.identifier[1] != 79 || this.identifier[2] != 66 || this.identifier[3] != 73) {
            throw new IOException("Did not get expected MOBI identifier");
        }
        StreamUtils.readByteArray(in, this.headerLength);
        int headLen = StreamUtils.byteArrayToInt(this.headerLength);
        this.restOfMobiHeader = new byte[headLen + 16 - 132];
        if (MobiCommon.debug) {
            MobiCommon.logMessage("headerLength: " + headLen);
        }
        StreamUtils.readByteArray(in, this.mobiType);
        if (MobiCommon.debug) {
            MobiCommon.logMessage("mobiType: " + StreamUtils.byteArrayToInt(this.mobiType));
        }
        StreamUtils.readByteArray(in, this.textEncoding);
        switch (StreamUtils.byteArrayToInt(this.textEncoding)) {
            case 1252: {
                this.characterEncoding = "Cp1252";
                break;
            }
            case 65001: {
                this.characterEncoding = "UTF-8";
                break;
            }
            default: {
                this.characterEncoding = null;
            }
        }
        MobiCommon.logMessage("text encoding: " + this.characterEncoding);
        StreamUtils.readByteArray(in, this.uniqueID);
        StreamUtils.readByteArray(in, this.fileVersion);
        StreamUtils.readByteArray(in, this.orthographicIndex);
        StreamUtils.readByteArray(in, this.inflectionIndex);
        StreamUtils.readByteArray(in, this.indexNames);
        StreamUtils.readByteArray(in, this.indexKeys);
        StreamUtils.readByteArray(in, this.extraIndex0);
        StreamUtils.readByteArray(in, this.extraIndex1);
        StreamUtils.readByteArray(in, this.extraIndex2);
        StreamUtils.readByteArray(in, this.extraIndex3);
        StreamUtils.readByteArray(in, this.extraIndex4);
        StreamUtils.readByteArray(in, this.extraIndex5);
        StreamUtils.readByteArray(in, this.firstNonBookIndex);
        StreamUtils.readByteArray(in, this.fullNameOffset);
        if (MobiCommon.debug) {
            MobiCommon.logMessage("full name offset: " + StreamUtils.byteArrayToInt(this.fullNameOffset));
        }
        StreamUtils.readByteArray(in, this.fullNameLength);
        int fullNameLen = StreamUtils.byteArrayToInt(this.fullNameLength);
        MobiCommon.logMessage("full name length: " + fullNameLen);
        StreamUtils.readByteArray(in, this.locale);
        StreamUtils.readByteArray(in, this.inputLanguage);
        StreamUtils.readByteArray(in, this.outputLanguage);
        StreamUtils.readByteArray(in, this.minVersion);
        StreamUtils.readByteArray(in, this.firstImageIndex);
        StreamUtils.readByteArray(in, this.huffmanRecordOffset);
        StreamUtils.readByteArray(in, this.huffmanRecordCount);
        StreamUtils.readByteArray(in, this.huffmanTableOffset);
        StreamUtils.readByteArray(in, this.huffmanTableLength);
        StreamUtils.readByteArray(in, this.exthFlags);
        if (MobiCommon.debug) {
            MobiCommon.logMessage("exthFlags: " + StreamUtils.byteArrayToInt(this.exthFlags));
        }
        boolean exthExists = (StreamUtils.byteArrayToInt(this.exthFlags) & 64) != 0;
        MobiCommon.logMessage("exthExists: " + exthExists);
        StreamUtils.readByteArray(in, this.restOfMobiHeader);
        if (exthExists) {
            this.exthHeader = new EXTHHeader(in);
        }
        int currentOffset = 132 + this.restOfMobiHeader.length + this.exthHeaderSize();
        this.remainder = new byte[(int) (mobiHeaderSize - (long) currentOffset)];
        StreamUtils.readByteArray(in, this.remainder);
        int fullNameIndexInRemainder = StreamUtils.byteArrayToInt(this.fullNameOffset) - currentOffset;
        this.fullName = new byte[fullNameLen];
        MobiCommon.logMessage("fullNameIndexInRemainder: " + fullNameIndexInRemainder);
        MobiCommon.logMessage("fullNameLen: " + fullNameLen);
        if (fullNameIndexInRemainder >= 0 && fullNameIndexInRemainder < this.remainder.length && fullNameIndexInRemainder + fullNameLen <= this.remainder.length && fullNameLen > 0) {
            System.arraycopy(this.remainder, fullNameIndexInRemainder, this.fullName, 0, fullNameLen);
        }
        if (MobiCommon.debug) {
            MobiCommon.logMessage("full name: " + StreamUtils.byteArrayToString(this.fullName));
        }
    }

    private int exthHeaderSize() {
        return this.exthHeader == null ? 0 : this.exthHeader.size();
    }

    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    public String getFullName() {
        return StreamUtils.byteArrayToString(this.fullName, this.characterEncoding);
    }

    public void setFullName(String s) {
        byte[] fullBytes = StreamUtils.stringToByteArray(s, this.characterEncoding);
        int len = fullBytes.length;
        StreamUtils.intToByteArray(len, this.fullNameLength);
        int padding = (len + 2) % 4;
        if (padding != 0) {
            padding = 4 - padding;
        }
        byte[] buffer = new byte[len + (padding += 2)];
        System.arraycopy(fullBytes, 0, buffer, 0, len);
        int i = len;
        while (i < buffer.length) {
            buffer[i] = 0;
            ++i;
        }
        this.fullName = buffer;
    }

    public int getLocale() {
        return StreamUtils.byteArrayToInt(this.locale);
    }

    public void setLocale(int localeInt) {
        StreamUtils.intToByteArray(localeInt, this.locale);
    }

    public int getInputLanguage() {
        return StreamUtils.byteArrayToInt(this.inputLanguage);
    }

    public void setInputLanguage(int input) {
        StreamUtils.intToByteArray(input, this.inputLanguage);
    }

    public int getOutputLanguage() {
        return StreamUtils.byteArrayToInt(this.outputLanguage);
    }

    public void setOutputLanguage(int output) {
        StreamUtils.intToByteArray(output, this.outputLanguage);
    }

    public List<EXTHRecord> getEXTHRecords() {
        return this.exthHeader == null ? new LinkedList<EXTHRecord>() : this.exthHeader.getRecordList();
    }

    public void setEXTHRecords(List<EXTHRecord> list) {
        int flag = StreamUtils.byteArrayToInt(this.exthFlags) & 16777151;
        if (list == null || list.size() == 0) {
            this.exthHeader = null;
            StreamUtils.intToByteArray(flag, this.exthFlags);
        } else {
            if (this.exthHeader == null) {
                this.exthHeader = new EXTHHeader(list);
            } else {
                this.exthHeader.setRecordList(list);
            }
            StreamUtils.intToByteArray(flag | 64, this.exthFlags);
        }
    }

    public void pack() {
        if (!MobiCommon.safeMode) {
            this.remainder = new byte[this.fullName.length];
            System.arraycopy(this.fullName, 0, this.remainder, 0, this.remainder.length);
            StreamUtils.intToByteArray(132 + this.restOfMobiHeader.length + this.exthHeaderSize(), this.fullNameOffset);
        }
    }

    public int size() {
        return 132 + this.restOfMobiHeader.length + this.exthHeaderSize() + this.remainder.length;
    }

    public String getCompression() {
        int comp = StreamUtils.byteArrayToInt(this.compression);
        switch (comp) {
            case 1: {
                return "None";
            }
            case 2: {
                return "PalmDOC";
            }
            case 17480: {
                return "HUFF/CDIC";
            }
        }
        return "Unknown (" + comp + ")";
    }

    public long getTextLength() {
        return StreamUtils.byteArrayToLong(this.textLength);
    }

    public int getRecordCount() {
        return StreamUtils.byteArrayToInt(this.recordCount);
    }

    public int getRecordSize() {
        return StreamUtils.byteArrayToInt(this.recordSize);
    }

    public String getEncryptionType() {
        int enc = StreamUtils.byteArrayToInt(this.encryptionType);
        switch (enc) {
            case 0: {
                return "None";
            }
            case 1: {
                return "Old Mobipocket";
            }
            case 2: {
                return "Mobipocket";
            }
        }
        return "Unknown (" + enc + ")";
    }

    public long getHeaderLength() {
        return StreamUtils.byteArrayToLong(this.headerLength);
    }

    public String getMobiType() {
        long type = StreamUtils.byteArrayToLong(this.mobiType);
        if (type == 2) {
            return "Mobipocket Book";
        }
        if (type == 3) {
            return "PalmDoc Book";
        }
        if (type == 4) {
            return "Audio";
        }
        if (type == 257) {
            return "News";
        }
        if (type == 258) {
            return "News Feed";
        }
        if (type == 259) {
            return "News Magazine";
        }
        if (type == 513) {
            return "PICS";
        }
        if (type == 514) {
            return "WORD";
        }
        if (type == 515) {
            return "XLS";
        }
        if (type == 516) {
            return "PPT";
        }
        if (type == 517) {
            return "TEXT";
        }
        if (type == 518) {
            return "HTML";
        }
        return "Unknown (" + type + ")";
    }

    public long getUniqueID() {
        return StreamUtils.byteArrayToLong(this.uniqueID);
    }

    public long getFileVersion() {
        return StreamUtils.byteArrayToLong(this.fileVersion);
    }

    public long getOrthographicIndex() {
        return StreamUtils.byteArrayToLong(this.orthographicIndex);
    }

    public long getInflectionIndex() {
        return StreamUtils.byteArrayToLong(this.inflectionIndex);
    }

    public long getIndexNames() {
        return StreamUtils.byteArrayToLong(this.indexNames);
    }

    public long getIndexKeys() {
        return StreamUtils.byteArrayToLong(this.indexKeys);
    }

    public long getExtraIndex0() {
        return StreamUtils.byteArrayToLong(this.extraIndex0);
    }

    public long getExtraIndex1() {
        return StreamUtils.byteArrayToLong(this.extraIndex1);
    }

    public long getExtraIndex2() {
        return StreamUtils.byteArrayToLong(this.extraIndex2);
    }

    public long getExtraIndex3() {
        return StreamUtils.byteArrayToLong(this.extraIndex3);
    }

    public long getExtraIndex4() {
        return StreamUtils.byteArrayToLong(this.extraIndex4);
    }

    public long getExtraIndex5() {
        return StreamUtils.byteArrayToLong(this.extraIndex5);
    }

    public long getFirstNonBookIndex() {
        return StreamUtils.byteArrayToLong(this.firstNonBookIndex);
    }

    public long getFullNameOffset() {
        return StreamUtils.byteArrayToLong(this.fullNameOffset);
    }

    public long getFullNameLength() {
        return StreamUtils.byteArrayToLong(this.fullNameLength);
    }

    public long getMinVersion() {
        return StreamUtils.byteArrayToLong(this.minVersion);
    }

    public long getHuffmanRecordOffset() {
        return StreamUtils.byteArrayToLong(this.huffmanRecordOffset);
    }

    public long getHuffmanRecordCount() {
        return StreamUtils.byteArrayToLong(this.huffmanRecordCount);
    }

    public long getHuffmanTableOffset() {
        return StreamUtils.byteArrayToLong(this.huffmanTableOffset);
    }

    public long getHuffmanTableLength() {
        return StreamUtils.byteArrayToLong(this.huffmanTableLength);
    }

    public void write(OutputStream out) throws IOException {
        out.write(this.compression);
        out.write(this.unused0);
        out.write(this.textLength);
        out.write(this.recordCount);
        out.write(this.recordSize);
        out.write(this.encryptionType);
        out.write(this.unused1);
        out.write(this.identifier);
        out.write(this.headerLength);
        out.write(this.mobiType);
        out.write(this.textEncoding);
        out.write(this.uniqueID);
        out.write(this.fileVersion);
        out.write(this.orthographicIndex);
        out.write(this.inflectionIndex);
        out.write(this.indexNames);
        out.write(this.indexKeys);
        out.write(this.extraIndex0);
        out.write(this.extraIndex1);
        out.write(this.extraIndex2);
        out.write(this.extraIndex3);
        out.write(this.extraIndex4);
        out.write(this.extraIndex5);
        out.write(this.firstNonBookIndex);
        out.write(this.fullNameOffset);
        out.write(this.fullNameLength);
        out.write(this.locale);
        out.write(this.inputLanguage);
        out.write(this.outputLanguage);
        out.write(this.minVersion);
        out.write(this.firstImageIndex);
        out.write(this.huffmanRecordOffset);
        out.write(this.huffmanRecordCount);
        out.write(this.huffmanTableOffset);
        out.write(this.huffmanTableLength);
        out.write(this.exthFlags);
        out.write(this.restOfMobiHeader);
        if (this.exthHeader != null) {
            this.exthHeader.write(out);
        }
        out.write(this.remainder);
    }
}

