package uk.co.droidinactu.elibrary.library;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

import java.io.IOException;
import java.io.InputStream;

public class TikaAnalysis {

    public static String detectDocType(InputStream stream) throws IOException {
        Tika tika = new Tika();
        return tika.detect(stream);
    }

    public static String extractContent(InputStream stream) throws IOException, TikaException {
        Tika tika = new Tika();
        return tika.parseToString(stream);
    }

    public static Metadata extractMetadata(InputStream stream) throws IOException {
        Tika tika = new Tika();
        Metadata metadata = new Metadata();
        tika.parse(stream, metadata);
        return metadata;
    }

}
