package cocobo1.pupu.manager.installer.step.patching

import cocobo1.pupu.manager.R
import cocobo1.pupu.manager.installer.step.Step
import cocobo1.pupu.manager.installer.step.StepGroup
import cocobo1.pupu.manager.installer.step.StepRunner
import cocobo1.pupu.manager.installer.step.download.DownloadPupuStep
import cocobo1.pupu.manager.installer.util.Patcher
import java.io.File

/**
 * Uses LSPatch to inject the PupuXPosed module into Discord
 *
 * @param signedDir The signed apks to patch
 * @param lspatchedDir Output directory for LSPatch
 */
class AddPupuStep(
    private val signedDir: File,
    private val lspatchedDir: File
) : Step() {

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_add_vd

    override suspend fun run(runner: StepRunner) {
        val pupu = runner.getCompletedStep<DownloadPupuStep>().workingCopy

        runner.logger.i("Adding PupuXposed module with LSPatch")
        val files = signedDir.listFiles()
            ?.takeIf { it.isNotEmpty() }
            ?: throw Error("Missing APKs from signing step")

        Patcher.patch(
            runner.logger,
            outputDir = lspatchedDir,
            apkPaths = files.map { it.absolutePath },
            embeddedModules = listOf(pupu.absolutePath)
        )
    }

}