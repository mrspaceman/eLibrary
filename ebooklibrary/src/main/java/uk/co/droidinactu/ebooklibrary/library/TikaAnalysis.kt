package uk.co.droidinactu.ebooklibrary.library

import org.apache.tika.Tika
import org.apache.tika.exception.TikaException
import org.apache.tika.metadata.Metadata

import java.io.IOException
import java.io.InputStream

object TikaAnalysis {

    @Throws(IOException::class)
    fun detectDocType(stream: InputStream): String {
        val tika = Tika()
        return tika.detect(stream)
    }

    @Throws(IOException::class, TikaException::class)
    fun extractContent(stream: InputStream): String {
        val tika = Tika()
        return tika.parseToString(stream)
    }

    @Throws(IOException::class)
    fun extractMetadata(stream: InputStream): Metadata {
        val tika = Tika()
        val metadata = Metadata()
        tika.parse(stream, metadata)
        return metadata
    }

}
