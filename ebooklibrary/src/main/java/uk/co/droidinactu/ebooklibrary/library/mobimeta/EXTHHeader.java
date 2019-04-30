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
public class EXTHHeader {
    private byte[] identifier = new byte[]{69, 88, 84, 72};
    private byte[] headerLength = new byte[4];
    private byte[] recordCount = new byte[4];
    private List<EXTHRecord> recordList = null;

    public EXTHHeader() {
        this.recordList = new LinkedList<EXTHRecord>();
    }

    public EXTHHeader(List<EXTHRecord> list) {
        this.setRecordList(list);
    }

    public void recomputeFields() {
        StreamUtils.intToByteArray(this.size(), this.headerLength);
        StreamUtils.intToByteArray(this.recordList.size(), this.recordCount);
    }

    public int size() {
        int dataSize = this.dataSize();
        return 12 + dataSize + this.paddingSize(dataSize);
    }

    protected int dataSize() {
        int size = 0;
        for (EXTHRecord rec : this.recordList) {
            size += rec.size();
        }
        return size;
    }

    protected int paddingSize(int dataSize) {
        int paddingSize = dataSize % 4;
        if (paddingSize != 0) {
            paddingSize = 4 - paddingSize;
        }
        return paddingSize;
    }

    public EXTHHeader(InputStream in) throws IOException {
        MobiCommon.logMessage("*** EXTHHeader ***");
        StreamUtils.readByteArray(in, this.identifier);
        if (this.identifier[0] != 69 || this.identifier[1] != 88 || this.identifier[2] != 84 || this.identifier[3] != 72) {
            throw new IOException("Expected to find EXTH header identifier EXTH but got something else instead");
        }
        StreamUtils.readByteArray(in, this.headerLength);
        if (MobiCommon.debug) {
            MobiCommon.logMessage("EXTH header length: " + StreamUtils.byteArrayToLong(this.headerLength));
        }
        StreamUtils.readByteArray(in, this.recordCount);
        int count = StreamUtils.byteArrayToInt(this.recordCount);
        MobiCommon.logMessage("EXTH record count: " + count);
        this.recordList = new LinkedList<EXTHRecord>();
        int i = 0;
        while (i < count) {
            this.recordList.add(new EXTHRecord(in));
            ++i;
        }
        int padding = this.paddingSize(this.dataSize());
        MobiCommon.logMessage("padding size: " + padding);
        int i2 = 0;
        while (i2 < padding) {
            StreamUtils.readByte(in);
            ++i2;
        }
    }

    public List<EXTHRecord> getRecordList() {
        LinkedList<EXTHRecord> list = new LinkedList<EXTHRecord>();
        for (EXTHRecord rec : this.recordList) {
            list.add(rec.copy());
        }
        return list;
    }

    public void setRecordList(List<EXTHRecord> list) {
        this.recordList = new LinkedList<EXTHRecord>();
        if (list != null) {
            for (EXTHRecord rec : list) {
                this.recordList.add(rec.copy());
            }
        }
        this.recomputeFields();
    }

    public void removeRecordsWithType(int type) {
        boolean changed = false;
        for (EXTHRecord rec : this.recordList) {
            if (rec.getRecordType() != type) continue;
            this.recordList.remove(rec);
            changed = true;
        }
        if (changed) {
            this.recomputeFields();
        }
    }

    public boolean recordsWithTypeExist(int type) {
        for (EXTHRecord rec : this.recordList) {
            if (rec.getRecordType() != type) continue;
            return true;
        }
        return false;
    }

    public void setAllRecordsWithTypeToString(int type, String s, String encoding) {
        boolean changed = false;
        for (EXTHRecord rec : this.recordList) {
            if (rec.getRecordType() != type) continue;
            rec.setData(s, encoding);
            changed = true;
        }
        if (changed) {
            this.recomputeFields();
        }
    }

    public void addRecord(int recType, String s, String encoding) {
        EXTHRecord rec = new EXTHRecord(recType, StreamUtils.stringToByteArray(s, encoding));
        this.recordList.add(rec);
        this.recomputeFields();
    }

    public void addRecord(int recType, byte[] buffer) {
        this.recordList.add(new EXTHRecord(recType, buffer));
        this.recomputeFields();
    }

    public void write(OutputStream out) throws IOException {
        out.write(this.identifier);
        out.write(this.headerLength);
        out.write(this.recordCount);
        for (EXTHRecord rec : this.recordList) {
            rec.write(out);
        }
        int padding = this.paddingSize(this.dataSize());
        int i = 0;
        while (i < padding) {
            out.write(0);
            ++i;
        }
    }
}

