package cocobo1.pupu.manager.ui.viewmodel.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cocobo1.pupu.manager.BuildConfig
import cocobo1.pupu.manager.domain.manager.DownloadManager
import cocobo1.pupu.manager.domain.manager.InstallManager
import cocobo1.pupu.manager.domain.manager.InstallMethod
import cocobo1.pupu.manager.domain.manager.PreferenceManager
import cocobo1.pupu.manager.domain.repository.RestRepository
import cocobo1.pupu.manager.installer.Installer
import cocobo1.pupu.manager.installer.session.SessionInstaller
import cocobo1.pupu.manager.installer.shizuku.ShizukuInstaller
import cocobo1.pupu.manager.network.dto.Release
import cocobo1.pupu.manager.network.utils.CommitsPagingSource
import cocobo1.pupu.manager.network.utils.dataOrNull
import cocobo1.pupu.manager.network.utils.ifSuccessful
import cocobo1.pupu.manager.utils.DiscordVersion
import cocobo1.pupu.manager.utils.isMiui
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    private val repo: RestRepository,
    val context: Context,
    val prefs: PreferenceManager,
    val installManager: InstallManager,
    private val downloadManager: DownloadManager
) : ScreenModel {

    private val cacheDir = context.externalCacheDir ?: File(
        Environment.getExternalStorageDirectory(),
        Environment.DIRECTORY_DOWNLOADS
    ).resolve("PupuManager").also { it.mkdirs() }

    var discordVersions by mutableStateOf<Map<DiscordVersion.Type, DiscordVersion?>?>(null)
        private set

    var release by mutableStateOf<Release?>(null)
        private set

    var showUpdateDialog by mutableStateOf(false)
    var isUpdating by mutableStateOf(false)
    val commits = Pager(PagingConfig(pageSize = 30)) { CommitsPagingSource(repo) }.flow.cachedIn(screenModelScope)

    init {
        getDiscordVersions()
        checkForUpdate()
    }

    fun getDiscordVersions() {
        screenModelScope.launch {
            discordVersions = repo.getLatestDiscordVersions().dataOrNull
            if (prefs.autoClearCache) autoClearCache()
        }
    }

    fun launchPupu() {
        installManager.current?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun uninstallPupu() {
        installManager.uninstall()
    }

    fun launchPupuInfo() {
        installManager.current?.let {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse("package:${it.packageName}")
                context.startActivity(this)
            }
        }
    }

    private fun autoClearCache() {
        val currentVersion =
            DiscordVersion.fromVersionCode(installManager.current?.longVersionCode.toString()) ?: return
        val latestVersion = when {
            prefs.discordVersion.isBlank() -> discordVersions?.get(prefs.channel)
            else -> DiscordVersion.fromVersionCode(prefs.discordVersion)
        } ?: return

        if (latestVersion > currentVersion) {
            for (file in (context.externalCacheDir ?: context.cacheDir).listFiles()
                ?: emptyArray()) {
                if (file.isDirectory) file.deleteRecursively()
            }
        }
    }

    private fun checkForUpdate() {
        screenModelScope.launch {
            release = repo.getLatestRelease("C0C0B01/PupuManager").dataOrNull
            release?.let {
                showUpdateDialog = it.tagName.toInt() > BuildConfig.VERSION_CODE
            }
            repo.getLatestRelease("C0C0B01/PupuXposed").ifSuccessful {
                if (prefs.moduleVersion != it.tagName) {
                    prefs.moduleVersion = it.tagName
                    val module = File(cacheDir, "xposed.apk")
                    if (module.exists()) module.delete()
                }
            }
        }
    }

    fun downloadAndInstallUpdate() {
        screenModelScope.launch {
            val update = File(cacheDir, "update.apk")
            if (update.exists()) update.delete()
            isUpdating = true
            downloadManager.downloadUpdate(update)
            isUpdating = false

            val installer: Installer = when (prefs.installMethod) {
                InstallMethod.DEFAULT -> SessionInstaller(context)
                InstallMethod.SHIZUKU -> ShizukuInstaller(context)
            }

            installer.installApks(silent = !isMiui, update)
        }
    }

}