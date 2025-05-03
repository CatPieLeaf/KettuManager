package cocobo1.pupu.manager.di

import cocobo1.pupu.manager.domain.manager.DownloadManager
import cocobo1.pupu.manager.domain.manager.InstallManager
import cocobo1.pupu.manager.domain.manager.PreferenceManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::DownloadManager)
    singleOf(::PreferenceManager)
    singleOf(::InstallManager)
}