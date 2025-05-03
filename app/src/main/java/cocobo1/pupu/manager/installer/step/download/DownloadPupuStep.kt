package cocobo1.pupu.manager.installer.step.download

import androidx.compose.runtime.Stable
import cocobo1.pupu.manager.R
import cocobo1.pupu.manager.installer.step.download.base.DownloadStep
import java.io.File

/**
 * Downloads the PupuXPosed module
 *
 * https://github.com/C0C0B01/PupuXposed
 */
@Stable
class DownloadPupuStep(
    workingDir: File
): DownloadStep() {

    override val nameRes = R.string.step_dl_vd

    override val url: String = "https://github.com/C0C0B01/PupuXposed/releases/latest/download/app-release.apk"
    override val destination = preferenceManager.moduleLocation
    override val workingCopy = workingDir.resolve("xposed.apk")

}