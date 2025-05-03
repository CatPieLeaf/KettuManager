package cocobo1.pupu.manager.installer

import java.io.File

interface Installer {
    suspend fun installApks(silent: Boolean = false, vararg apks: File)
}