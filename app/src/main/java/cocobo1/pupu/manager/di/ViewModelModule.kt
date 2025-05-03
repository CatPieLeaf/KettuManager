package cocobo1.pupu.manager.di

import cocobo1.pupu.manager.ui.viewmodel.home.HomeViewModel
import cocobo1.pupu.manager.ui.viewmodel.installer.InstallerViewModel
import cocobo1.pupu.manager.ui.viewmodel.installer.LogViewerViewModel
import cocobo1.pupu.manager.ui.viewmodel.libraries.LibrariesViewModel
import cocobo1.pupu.manager.ui.viewmodel.settings.AdvancedSettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::InstallerViewModel)
    factoryOf(::AdvancedSettingsViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::LogViewerViewModel)
    factoryOf(::LibrariesViewModel)
}