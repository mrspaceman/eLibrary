package uk.co.droidinactu.ebooklib.files;

import java.util.List;

public class DirectoryContents {
    public List<FileHolder> listDir;
    public List<FileHolder> listFile;
    public List<FileHolder> listSdCard;

    // If true, there's a ".nomedia" file in this directory.
    public boolean noMedia;
    // if true, there was a problem accessing the directory
    public boolean noAccess;
}
