package cocobo1.pupu.manager.installer.step.patching

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.github.diamondminer88.zip.ZipWriter
import cocobo1.pupu.manager.R
import cocobo1.pupu.manager.domain.manager.PreferenceManager
import cocobo1.pupu.manager.installer.step.Step
import cocobo1.pupu.manager.installer.step.StepGroup
import cocobo1.pupu.manager.installer.step.StepRunner
import cocobo1.pupu.manager.installer.step.download.DownloadBaseStep
import cocobo1.pupu.manager.installer.util.ArscUtil
import cocobo1.pupu.manager.installer.util.ArscUtil.addColorResource
import cocobo1.pupu.manager.installer.util.ArscUtil.getMainArscChunk
import cocobo1.pupu.manager.installer.util.ArscUtil.getPackageChunk
import cocobo1.pupu.manager.installer.util.ArscUtil.getResourceFileName
import cocobo1.pupu.manager.installer.util.AxmlUtil
import cocobo1.pupu.manager.utils.DiscordVersion
import org.koin.core.component.inject

/**
 * Replaces the existing app icons with Pupu tinted ones
 */
class ReplaceIconStep : Step() {

    private val preferences: PreferenceManager by inject()

    val context: Context by inject()

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_change_icon

    override suspend fun run(runner: StepRunner) {
        val baseApk = runner.getCompletedStep<DownloadBaseStep>().workingCopy

        runner.logger.i("Reading resources.arsc")
        val arsc = ArscUtil.readArsc(baseApk)
        
        val iconRscIds = AxmlUtil.readManifestIconInfo(baseApk)
        val squareIconFile = arsc.getMainArscChunk().getResourceFileName(iconRscIds.squareIcon, "anydpi-v26")
        val roundIconFile = arsc.getMainArscChunk().getResourceFileName(iconRscIds.roundIcon, "anydpi-v26")

        runner.logger.i("Patching icon assets (squareIcon=$squareIconFile, roundIcon=$roundIconFile)")

        val backgroundColor = arsc.getPackageChunk().addColorResource("bunny_color", Color(0xFF48488B))

        val postfix = when (preferences.channel) {
            DiscordVersion.Type.BETA -> "beta"
            DiscordVersion.Type.ALPHA -> "canary"
            else -> null
        }
        
        for (rscFile in setOf(squareIconFile, roundIconFile)) { // setOf to not possibly patch same file twice
            val referencePath = if (postfix == null) rscFile else {
                rscFile.replace("_$postfix.xml", ".xml")
            }

            runner.logger.i("Patching adaptive icon ($rscFile <- $referencePath)")

            AxmlUtil.patchAdaptiveIcon(
                apk = baseApk,
                resourcePath = rscFile,
                referencePath = referencePath,
                backgroundColor = backgroundColor,
            )
        }

        runner.logger.i("Writing and compiling resources.arsc")
        ZipWriter(baseApk, /* append = */ true).use {
            it.deleteEntry("resources.arsc")
            it.writeEntry("resources.arsc", arsc.toByteArray())
        }
    }

}
