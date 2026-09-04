package no.uio.ifi.in2000.sofiaalo.team44.data.fileStorage

import android.content.Context
import java.io.File

class FileStorageRepositoryImp(
    private val context: Context
) : FileStorage {

    override val filesDir: File
        get() = context.filesDir
}