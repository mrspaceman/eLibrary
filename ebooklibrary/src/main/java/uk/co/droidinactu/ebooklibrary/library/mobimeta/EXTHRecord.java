/*
 * Decompiled with CFR 0_115.
 */
package uk.co.droidinactu.ebooklibrary.library.mobimeta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class EXTHRecord {
    private static final int[] booleanTypes = new int[]{404};
    private static final int[] knownTypes = new int[]{100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 118, 119, 200, 404, 501, 503, 504};
    private static final String[] knownDesc = new String[]{"author", "publisher", "imprint", "description", "ISBN", "subject", "publishing date", "review", "contributor", "rights", "subject code", "type", "source", "ASIN", "version number", "retail price", "retail price currency", "dictionary short name", "TTS off", "CDE type", "updated title", "ASIN"};
    private static HashMap<Integer, String> typeHash = new HashMap(knownTypes.length);
    private static HashSet<Integer> booleanTypesSet;

    static {
        int i = 0;
        while (i < knownTypes.length) {
            typeHash.put(knownTypes[i], knownDesc[i]);
            ++i;
        }
        booleanTypesSet = new HashSet(booleanTypes.length);
        i = 0;
        while (i < booleanTypes.length) {
            booleanTypesSet.add(booleanTypes[i]);
            ++i;
        }
    }

    private byte[] recordType = new byte[4];
    private byte[] recordLength = new byte[4];
    private byte[] recordData;

    public EXTHRecord(int recType, String data, String characterEncoding) {
        this(recType, StreamUtils.stringToByteArray(data, characterEncoding));
    }

    EXTHRecord(int recType, byte[] data) {
        StreamUtils.intToByteArray(recType, this.recordType);
        int len = data == null ? 0 : data.length;
        StreamUtils.intToByteArray(len + 8, this.recordLength);
        this.recordData = new byte[len];
        if (len > 0) {
            System.arraycopy(data, 0, this.recordData, 0, len);
        }
    }

    public EXTHRecord(int recType, boolean data) {
        StreamUtils.intToByteArray(recType, this.recordType);
        this.recordData = new byte[1];
        this.recordData[0] = (byte) (data ? 1 : 0);
        StreamUtils.intToByteArray(this.size(), this.recordLength);
    }

    private static boolean isKnownType(int type) {
        return typeHash.containsKey(type);
    }

    private static String getDescriptionForType(int type) {
        return typeHash.get(type);
    }

    EXTHRecord(InputStream in) throws IOException {
        MobiCommon.logMessage("*** EXTHRecord ***");
        StreamUtils.readByteArray(in, this.recordType);
        StreamUtils.readByteArray(in, this.recordLength);
        int len = StreamUtils.byteArrayToInt(this.recordLength);
        if (len < 8) {
            throw new IOException("Invalid EXTH record length");
        }
        this.recordData = new byte[len - 8];
        StreamUtils.readByteArray(in, this.recordData);
        if (MobiCommon.debug) {
            int recType = StreamUtils.byteArrayToInt(this.recordType);
            System.out.print("EXTH record type: ");
            switch (recType) {
                case 100: {
                    MobiCommon.logMessage("author");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 101: {
                    MobiCommon.logMessage("publisher");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 103: {
                    MobiCommon.logMessage("description");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 104: {
                    MobiCommon.logMessage("isbn");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 105: {
                    MobiCommon.logMessage("subject");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 106: {
                    MobiCommon.logMessage("publishingdate");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 109: {
                    MobiCommon.logMessage("rights");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 113:
                case 504: {
                    MobiCommon.logMessage("asin");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 118: {
                    MobiCommon.logMessage("retail price");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 119: {
                    MobiCommon.logMessage("retail price currency");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 200: {
                    MobiCommon.logMessage("dictionary short name");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                case 404: {
                    MobiCommon.logMessage("text to speech");
                    int ttsflag = StreamUtils.byteArrayToInt(this.recordData);
                    MobiCommon.logMessage(ttsflag == 0 ? "enabled" : "disabled");
                    break;
                }
                case 501: {
                    MobiCommon.logMessage("cdetype");
                    MobiCommon.logMessage(StreamUtils.byteArrayToString(this.recordData));
                    break;
                }
                default: {
                    MobiCommon.logMessage(Integer.toString(recType));
                }
            }
        }
    }

    public static boolean isBooleanType(int type) {
        return booleanTypesSet.contains(type);
    }

    int getRecordType() {
        return StreamUtils.byteArrayToInt(this.recordType);
    }

    public byte[] getData() {
        return this.recordData;
    }

    public void setData(boolean value) {
        if (this.recordData == null) {
            this.recordData = new byte[1];
            StreamUtils.intToByteArray(this.size(), this.recordLength);
        }
        StreamUtils.intToByteArray(value ? 1 : 0, this.recordData);
    }

    public void setData(int value) {
        if (this.recordData == null) {
            this.recordData = new byte[4];
            StreamUtils.intToByteArray(this.size(), this.recordLength);
        }
        StreamUtils.intToByteArray(value, this.recordData);
    }

    void setData(String s, String encoding) {
        this.recordData = StreamUtils.stringToByteArray(s, encoding);
        StreamUtils.intToByteArray(this.size(), this.recordLength);
    }

    public EXTHRecord copy() {
        return new EXTHRecord(StreamUtils.byteArrayToInt(this.recordType), this.recordData);
    }

    public boolean isKnownType() {
        return EXTHRecord.isKnownType(StreamUtils.byteArrayToInt(this.recordType));
    }

    public int size() {
        return this.getDataLength() + 8;
    }

    public String getTypeDescription() {
        return EXTHRecord.getDescriptionForType(StreamUtils.byteArrayToInt(this.recordType));
    }

    private int getDataLength() {
        return this.recordData.length;
    }

    void write(OutputStream out) throws IOException {
        if (MobiCommon.debug) {
            MobiCommon.logMessage("*** Write EXTHRecord ***");
            MobiCommon.logMessage(StreamUtils.dumpByteArray(this.recordType));
            MobiCommon.logMessage(StreamUtils.dumpByteArray(this.recordLength));
            MobiCommon.logMessage(StreamUtils.dumpByteArray(this.recordData));
            MobiCommon.logMessage("************************");
        }
        out.write(this.recordType);
        out.write(this.recordLength);
        out.write(this.recordData);
    }
}

