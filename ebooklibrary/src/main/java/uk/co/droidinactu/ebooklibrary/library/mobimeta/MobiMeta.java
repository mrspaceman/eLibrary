/*
 * Decompiled with CFR 0_115.
 */
package uk.co.droidinactu.ebooklibrary.library.mobimeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MobiMeta {
    public static final int BUFFER_SIZE = 4096;
    protected PDBHeader pdbHeader;
    protected MobiHeader mobiHeader;
    protected String characterEncoding;
    protected List<EXTHRecord> exthRecords;
    private File inputFile;

    public MobiMeta(File f) throws MobiMetaException {
        this.inputFile = f;
        FileInputStream in = null;
        try {
            try {
                in = new FileInputStream(f);
                this.pdbHeader = new PDBHeader(in);
                this.mobiHeader = new MobiHeader(in, this.pdbHeader.getMobiHeaderSize());
                this.exthRecords = this.mobiHeader.getEXTHRecords();
                this.characterEncoding = this.mobiHeader.getCharacterEncoding();
            } catch (IOException e) {
                throw new MobiMetaException("Could not parse mobi file " + f.getAbsolutePath() + ": " + e.getMessage());
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException var5_6) {
                }
            }
        }
    }

    public void saveToNewFile(File outputFile) throws MobiMetaException {
        this.saveToNewFile(outputFile, true);
    }

    public void saveToNewFile(File outputFile, boolean packHeader) throws MobiMetaException {
        long readOffset = this.pdbHeader.getOffsetAfterMobiHeader();
        if (!MobiCommon.safeMode && packHeader) {
            this.mobiHeader.pack();
            this.pdbHeader.adjustOffsetsAfterMobiHeader(this.mobiHeader.size());
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            try {
                int bytesRead;
                out = new FileOutputStream(outputFile);
                this.pdbHeader.write(out);
                this.mobiHeader.write(out);
                byte[] buffer = new byte[4096];
                in = new FileInputStream(this.inputFile);
                in.skip(readOffset);
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new MobiMetaException("Problems encountered while writing to " + outputFile.getAbsolutePath() + ": " + e.getMessage());
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException var10_12) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException var10_13) {
                }
            }
        }
    }

    public String getCharacterEncoding() {
        return this.mobiHeader.getCharacterEncoding();
    }

    public String getFullName() {
        return this.mobiHeader.getFullName();
    }

    public void setFullName(String s) {
        this.mobiHeader.setFullName(s);
    }

    public List<EXTHRecord> getEXTHRecords() {
        return this.exthRecords;
    }

    public void setEXTHRecords() {
        this.mobiHeader.setEXTHRecords(this.exthRecords);
    }

    public int getLocale() {
        return this.mobiHeader.getLocale();
    }

    public int getDictInput() {
        return this.mobiHeader.getInputLanguage();
    }

    public int getDictOutput() {
        return this.mobiHeader.getOutputLanguage();
    }

    public void setLanguages(int locale, int dictInput, int dictOutput) {
        this.mobiHeader.setLocale(locale);
        this.mobiHeader.setInputLanguage(dictInput);
        this.mobiHeader.setOutputLanguage(dictOutput);
    }

    public String getMetaInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("PDB Header\r\n");
        sb.append("----------\r\n");
        sb.append("Name: ");
        sb.append(this.pdbHeader.getName());
        sb.append("\r\n");
        String[] attributes = this.getPDBHeaderAttributes();
        if (attributes.length > 0) {
            sb.append("Attributes: ");
            int i = 0;
            while (i < attributes.length) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(attributes[i]);
                ++i;
            }
            sb.append("\r\n");
        }
        sb.append("Version: ");
        sb.append(this.pdbHeader.getVersion());
        sb.append("\r\n");
        sb.append("Creation Date: ");
        sb.append(this.pdbHeader.getCreationDate());
        sb.append("\r\n");
        sb.append("Modification Date: ");
        sb.append(this.pdbHeader.getModificationDate());
        sb.append("\r\n");
        sb.append("Last Backup Date: ");
        sb.append(this.pdbHeader.getLastBackupDate());
        sb.append("\r\n");
        sb.append("Modification Number: ");
        sb.append(this.pdbHeader.getModificationNumber());
        sb.append("\r\n");
        sb.append("App Info ID: ");
        sb.append(this.pdbHeader.getAppInfoID());
        sb.append("\r\n");
        sb.append("Sort Info ID: ");
        sb.append(this.pdbHeader.getSortInfoID());
        sb.append("\r\n");
        sb.append("Type: ");
        sb.append(this.pdbHeader.getType());
        sb.append("\r\n");
        sb.append("Creator: ");
        sb.append(this.pdbHeader.getCreator());
        sb.append("\r\n");
        sb.append("Unique ID Seed: ");
        sb.append(this.pdbHeader.getUniqueIDSeed());
        sb.append("\r\n\r\n");
        sb.append("PalmDOC Header\r\n");
        sb.append("--------------\r\n");
        sb.append("Compression: ");
        sb.append(this.mobiHeader.getCompression());
        sb.append("\r\n");
        sb.append("Text Length: ");
        sb.append(this.mobiHeader.getTextLength());
        sb.append("\r\n");
        sb.append("Record Count: ");
        sb.append(this.mobiHeader.getRecordCount());
        sb.append("\r\n");
        sb.append("Record Size: ");
        sb.append(this.mobiHeader.getRecordSize());
        sb.append("\r\n");
        sb.append("Encryption Type: ");
        sb.append(this.mobiHeader.getEncryptionType());
        sb.append("\r\n\r\n");
        sb.append("MOBI Header\r\n");
        sb.append("-----------\r\n");
        sb.append("Header Length: ");
        sb.append(this.mobiHeader.getHeaderLength());
        sb.append("\r\n");
        sb.append("Mobi Type: ");
        sb.append(this.mobiHeader.getMobiType());
        sb.append("\r\n");
        sb.append("Unique ID: ");
        sb.append(this.mobiHeader.getUniqueID());
        sb.append("\r\n");
        sb.append("FileTreeNode Version: ");
        sb.append(this.mobiHeader.getFileVersion());
        sb.append("\r\n");
        sb.append("Orthographic Index: ");
        sb.append(this.mobiHeader.getOrthographicIndex());
        sb.append("\r\n");
        sb.append("Inflection Index: ");
        sb.append(this.mobiHeader.getInflectionIndex());
        sb.append("\r\n");
        sb.append("Index Names: ");
        sb.append(this.mobiHeader.getIndexNames());
        sb.append("\r\n");
        sb.append("Index Keys: ");
        sb.append(this.mobiHeader.getIndexKeys());
        sb.append("\r\n");
        sb.append("Extra Index 0: ");
        sb.append(this.mobiHeader.getExtraIndex0());
        sb.append("\r\n");
        sb.append("Extra Index 1: ");
        sb.append(this.mobiHeader.getExtraIndex1());
        sb.append("\r\n");
        sb.append("Extra Index 2: ");
        sb.append(this.mobiHeader.getExtraIndex2());
        sb.append("\r\n");
        sb.append("Extra Index 3: ");
        sb.append(this.mobiHeader.getExtraIndex3());
        sb.append("\r\n");
        sb.append("Extra Index 4: ");
        sb.append(this.mobiHeader.getExtraIndex4());
        sb.append("\r\n");
        sb.append("Extra Index 5: ");
        sb.append(this.mobiHeader.getExtraIndex5());
        sb.append("\r\n");
        sb.append("First Non-Book Index: ");
        sb.append(this.mobiHeader.getFirstNonBookIndex());
        sb.append("\r\n");
        sb.append("Full Name Offset: ");
        sb.append(this.mobiHeader.getFullNameOffset());
        sb.append("\r\n");
        sb.append("Full Name Length: ");
        sb.append(this.mobiHeader.getFullNameLength());
        sb.append("\r\n");
        sb.append("Min Version: ");
        sb.append(this.mobiHeader.getMinVersion());
        sb.append("\r\n");
        sb.append("Huffman Record Offset: ");
        sb.append(this.mobiHeader.getHuffmanRecordOffset());
        sb.append("\r\n");
        sb.append("Huffman Record Count: ");
        sb.append(this.mobiHeader.getHuffmanRecordCount());
        sb.append("\r\n");
        sb.append("Huffman Table Offset: ");
        sb.append(this.mobiHeader.getHuffmanTableOffset());
        sb.append("\r\n");
        sb.append("Huffman Table Length: ");
        sb.append(this.mobiHeader.getHuffmanTableLength());
        sb.append("\r\n");
        return sb.toString();
    }

    private String[] getPDBHeaderAttributes() {
        LinkedList<String> list = new LinkedList<String>();
        int attr = this.pdbHeader.getAttributes();
        if ((attr & 2) != 0) {
            list.add("Read-Only");
        }
        if ((attr & 4) != 0) {
            list.add("Dirty AppInfoArea");
        }
        if ((attr & 8) != 0) {
            list.add("Backup This Database");
        }
        if ((attr & 16) != 0) {
            list.add("OK To Install Newer Over Existing Copy");
        }
        if ((attr & 32) != 0) {
            list.add("Force The PalmPilot To Reset After This Database Is Installed");
        }
        if ((attr & 64) != 0) {
            list.add("Don't Allow Copy Of FileTreeNode To Be Beamed To Other Pilot");
        }
        String[] ret = new String[list.size()];
        int index = 0;
        for (String s : list) {
            ret[index++] = s;
        }
        return ret;
    }
}

