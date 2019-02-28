/*
 * Decompiled with CFR 0_115.
 */
package uk.co.droidinactu.elibrary.library.mobimeta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecordInfo {
    private byte[] recordDataOffset = new byte[4];
    private byte recordAttributes = 0;
    private byte[] uniqueID = new byte[3];

    public RecordInfo(InputStream in) throws IOException {
        StreamUtils.readByteArray(in, this.recordDataOffset);
        this.recordAttributes = StreamUtils.readByte(in);
        StreamUtils.readByteArray(in, this.uniqueID);
        if (MobiCommon.debug) {
            MobiCommon.logMessage("RecordInfo uniqueID: " + StreamUtils.byteArrayToInt(this.uniqueID));
        }
    }

    public long getRecordDataOffset() {
        return StreamUtils.byteArrayToLong(this.recordDataOffset);
    }

    public void setRecordDataOffset(long newOffset) {
        StreamUtils.longToByteArray(newOffset, this.recordDataOffset);
    }

    public void write(OutputStream out) throws IOException {
        out.write(this.recordDataOffset);
        out.write(this.recordAttributes);
        out.write(this.uniqueID);
    }
}

