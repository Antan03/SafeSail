package no.uio.ifi.in2000.sofiaalo.team44.data.fileStorage

import java.io.File

// her henter vi filene som ligger lagret
// er kanskje litt redundant men er grei
interface FileStorage {
    val filesDir: File
}