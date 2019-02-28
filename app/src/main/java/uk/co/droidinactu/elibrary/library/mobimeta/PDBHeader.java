/*
 * Decompiled with CFR 0_115.
 */
package uk.co.droidinactu.elibrary.library.mobimeta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class PDBHeader {
    private byte[] name = new byte[32];
    private byte[] attributes = new byte[2];
    private byte[] version = new byte[2];
    private byte[] creationDate = new byte[4];
    private byte[] modificationDate = new byte[4];
    private byte[] lastBackupDate = new byte[4];
    private byte[] modificationNumber = new byte[4];
    private byte[] appInfoID = new byte[4];
    private byte[] sortInfoID = new byte[4];
    private byte[] type = new byte[4];
    private byte[] creator = new byte[4];
    private byte[] uniqueIDSeed = new byte[4];
    private byte[] nextRecordListID = new byte[4];
    private byte[] numRecords = new byte[2];
    private List<RecordInfo> recordInfoList;
    private byte[] gapToData = new byte[2];

    public PDBHeader(InputStream in) throws IOException {
        MobiCommon.logMessage("*** PDBHeader ***");
        StreamUtils.readByteArray(in, this.name);
        StreamUtils.readByteArray(in, this.attributes);
        StreamUtils.readByteArray(in, this.version);
        StreamUtils.readByteArray(in, this.creationDate);
        StreamUtils.readByteArray(in, this.modificationDate);
        StreamUtils.readByteArray(in, this.lastBackupDate);
        StreamUtils.readByteArray(in, this.modificationNumber);
        StreamUtils.readByteArray(in, this.appInfoID);
        StreamUtils.readByteArray(in, this.sortInfoID);
        StreamUtils.readByteArray(in, this.type);
        StreamUtils.readByteArray(in, this.creator);
        StreamUtils.readByteArray(in, this.uniqueIDSeed);
        StreamUtils.readByteArray(in, this.nextRecordListID);
        StreamUtils.readByteArray(in, this.numRecords);
        int recordCount = StreamUtils.byteArrayToInt(this.numRecords);
        MobiCommon.logMessage("numRecords: " + recordCount);
        this.recordInfoList = new LinkedList<RecordInfo>();
        int i = 0;
        while (i < recordCount) {
            this.recordInfoList.add(new RecordInfo(in));
            ++i;
        }
        StreamUtils.readByteArray(in, this.gapToData);
    }

    public long getOffsetAfterMobiHeader() {
        return this.recordInfoList.size() > 1 ? this.recordInfoList.get(1).getRecordDataOffset() : 0;
    }

    public void adjustOffsetsAfterMobiHeader(int newMobiHeaderSize) {
        if (this.recordInfoList.size() < 2) {
            return;
        }
        int delta = (int) ((long) newMobiHeaderSize - this.getMobiHeaderSize());
        int len = this.recordInfoList.size();
        int i = 1;
        while (i < len) {
            RecordInfo rec = this.recordInfoList.get(i);
            long oldOffset = rec.getRecordDataOffset();
            rec.setRecordDataOffset(oldOffset + (long) delta);
            ++i;
        }
    }

    public long getMobiHeaderSize() {
        return this.recordInfoList.size() > 1 ? this.recordInfoList.get(1).getRecordDataOffset() - this.recordInfoList.get(0).getRecordDataOffset() : 0;
    }

    public void write(OutputStream out) throws IOException {
        out.write(this.name);
        out.write(this.attributes);
        out.write(this.version);
        out.write(this.creationDate);
        out.write(this.modificationDate);
        out.write(this.lastBackupDate);
        out.write(this.modificationNumber);
        out.write(this.appInfoID);
        out.write(this.sortInfoID);
        out.write(this.type);
        out.write(this.creator);
        out.write(this.uniqueIDSeed);
        out.write(this.nextRecordListID);
        out.write(this.numRecords);
        for (RecordInfo rec : this.recordInfoList) {
            rec.write(out);
        }
        out.write(this.gapToData);
    }

    public String getName() {
        return StreamUtils.byteArrayToString(this.name);
    }

    public int getAttributes() {
        return StreamUtils.byteArrayToInt(this.attributes);
    }

    public int getVersion() {
        return StreamUtils.byteArrayToInt(this.version);
    }

    public long getCreationDate() {
        return StreamUtils.byteArrayToLong(this.creationDate);
    }

    public long getModificationDate() {
        return StreamUtils.byteArrayToLong(this.modificationDate);
    }

    public long getLastBackupDate() {
        return StreamUtils.byteArrayToLong(this.lastBackupDate);
    }

    public long getModificationNumber() {
        return StreamUtils.byteArrayToLong(this.modificationNumber);
    }

    public long getAppInfoID() {
        return StreamUtils.byteArrayToLong(this.appInfoID);
    }

    public long getSortInfoID() {
        return StreamUtils.byteArrayToLong(this.sortInfoID);
    }

    public long getType() {
        return StreamUtils.byteArrayToLong(this.type);
    }

    public long getCreator() {
        return StreamUtils.byteArrayToLong(this.creator);
    }

    public long getUniqueIDSeed() {
        return StreamUtils.byteArrayToLong(this.uniqueIDSeed);
    }
}

