package cocobo1.pupu.manager.updatechecker.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cocobo1.pupu.manager.domain.manager.InstallManager
import cocobo1.pupu.manager.domain.manager.PreferenceManager
import cocobo1.pupu.manager.domain.repository.RestRepository
import cocobo1.pupu.manager.network.utils.ApiResponse
import cocobo1.pupu.manager.updatechecker.reciever.UpdateBroadcastReceiver
import cocobo1.pupu.manager.utils.DiscordVersion
import cocobo1.pupu.manager.utils.Intents
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    val api: RestRepository by inject()
    val prefs: PreferenceManager by inject()
    val installManager: InstallManager by inject()

    override suspend fun doWork(): Result {
        if (prefs.discordVersion.isNotBlank()) return Result.success()
        return when (val res = api.getLatestDiscordVersions()) {
            is ApiResponse.Success -> {
                val currentVersion =
                    DiscordVersion.fromVersionCode(installManager.current?.longVersionCode.toString())
                val latestVersion = res.data[prefs.channel]

                if (latestVersion == null || currentVersion == null) return Result.failure()

                if (latestVersion > currentVersion) {
                    context.sendBroadcast(
                        Intent(
                            context,
                            UpdateBroadcastReceiver::class.java
                        ).apply {
                            putExtra(Intents.Extras.VERSION, latestVersion.toVersionCode())
                        })
                }

                Result.success()
            }

            else -> Result.failure()
        }
    }

}