package com.romreviewertools.noteitup.di

import com.romreviewertools.noteitup.data.cloud.CloudProvider
import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.data.cloud.CloudSyncManager
import com.romreviewertools.noteitup.data.cloud.DropboxProvider
import com.romreviewertools.noteitup.data.cloud.GoogleDriveProvider
import com.romreviewertools.noteitup.data.database.DiaryDatabase
import com.romreviewertools.noteitup.data.database.DriverFactory
import com.romreviewertools.noteitup.data.encryption.EncryptedBundleService
import com.romreviewertools.noteitup.data.repository.CloudSyncRepositoryImpl
import com.romreviewertools.noteitup.data.repository.DiaryRepositoryImpl
import com.romreviewertools.noteitup.data.repository.PreferencesRepositoryImpl
import com.romreviewertools.noteitup.data.repository.SecurityRepositoryImpl
import com.romreviewertools.noteitup.domain.repository.CloudSyncRepository
import com.romreviewertools.noteitup.domain.repository.DiaryRepository
import com.romreviewertools.noteitup.domain.repository.PreferencesRepository
import com.romreviewertools.noteitup.domain.repository.SecurityRepository
import com.romreviewertools.noteitup.domain.usecase.CreateEntryUseCase
import com.romreviewertools.noteitup.domain.usecase.CreateFolderUseCase
import com.romreviewertools.noteitup.domain.usecase.CreateTagUseCase
import com.romreviewertools.noteitup.domain.usecase.DeleteEntryUseCase
import com.romreviewertools.noteitup.domain.usecase.DeleteFolderUseCase
import com.romreviewertools.noteitup.domain.usecase.DeleteTagUseCase
import com.romreviewertools.noteitup.domain.usecase.ExportEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.ImportEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllFoldersUseCase
import com.romreviewertools.noteitup.domain.usecase.GetAllTagsUseCase
import com.romreviewertools.noteitup.domain.usecase.GetEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.GetEntryByIdUseCase
import com.romreviewertools.noteitup.domain.usecase.GetStatsUseCase
import com.romreviewertools.noteitup.domain.usecase.SearchEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.UpdateEntryUseCase
import com.romreviewertools.noteitup.presentation.screens.allentries.AllEntriesViewModel
import com.romreviewertools.noteitup.presentation.screens.calendar.CalendarViewModel
import com.romreviewertools.noteitup.presentation.screens.cloudsync.CloudSyncViewModel
import com.romreviewertools.noteitup.presentation.screens.editor.EditorViewModel
import com.romreviewertools.noteitup.presentation.screens.export.ExportViewModel
import com.romreviewertools.noteitup.presentation.screens.folders.FoldersViewModel
import com.romreviewertools.noteitup.presentation.screens.home.HomeViewModel
import com.romreviewertools.noteitup.presentation.screens.search.SearchViewModel
import com.romreviewertools.noteitup.presentation.screens.settings.SettingsViewModel
import com.romreviewertools.noteitup.presentation.screens.statistics.StatisticsViewModel
import com.romreviewertools.noteitup.presentation.screens.security.SecurityViewModel
import com.romreviewertools.noteitup.presentation.screens.tags.TagsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    single { get<DriverFactory>().createDriver() }
    single { DiaryDatabase(get()) }
}

val repositoryModule = module {
    singleOf(::DiaryRepositoryImpl) bind DiaryRepository::class
    singleOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class
    singleOf(::SecurityRepositoryImpl) bind SecurityRepository::class
    singleOf(::CloudSyncRepositoryImpl) bind CloudSyncRepository::class
}

val useCaseModule = module {
    factoryOf(::GetEntriesUseCase)
    factoryOf(::GetEntryByIdUseCase)
    factoryOf(::CreateEntryUseCase)
    factoryOf(::UpdateEntryUseCase)
    factoryOf(::DeleteEntryUseCase)
    factoryOf(::GetStatsUseCase)
    factoryOf(::SearchEntriesUseCase)
    factoryOf(::GetAllTagsUseCase)
    factoryOf(::CreateTagUseCase)
    factoryOf(::DeleteTagUseCase)
    factoryOf(::GetAllFoldersUseCase)
    factoryOf(::CreateFolderUseCase)
    factoryOf(::DeleteFolderUseCase)
    factoryOf(::ExportEntriesUseCase)
    factoryOf(::ImportEntriesUseCase)
}

val cloudModule = module {
    // Encrypted bundle service
    single {
        EncryptedBundleService(
            encryptionService = get(),
            exportEntriesUseCase = get()
        )
    }

    // Cloud providers - using placeholder credentials (should be replaced with actual values)
    // In production, these should come from BuildConfig or secure storage
    single<CloudProvider>(named("googleDrive")) {
        GoogleDriveProvider(
            httpClient = get(),
            cloudSyncRepository = get(),
            oAuthHandler = get(),
            clientId = "YOUR_GOOGLE_CLIENT_ID",
            clientSecret = "YOUR_GOOGLE_CLIENT_SECRET"
        )
    }

    single<CloudProvider>(named("dropbox")) {
        DropboxProvider(
            httpClient = get(),
            cloudSyncRepository = get(),
            oAuthHandler = get(),
            appKey = "YOUR_DROPBOX_APP_KEY",
            appSecret = "YOUR_DROPBOX_APP_SECRET"
        )
    }

    // Cloud sync manager
    single {
        CloudSyncManager(
            googleDriveProvider = get(named("googleDrive")),
            dropboxProvider = get(named("dropbox")),
            cloudSyncRepository = get(),
            encryptedBundleService = get(),
            importEntriesUseCase = get()
        )
    }
}

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::EditorViewModel)
    viewModelOf(::AllEntriesViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::TagsViewModel)
    viewModelOf(::FoldersViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::StatisticsViewModel)
    viewModelOf(::ExportViewModel)
    viewModelOf(::SecurityViewModel)
    viewModelOf(::CloudSyncViewModel)
}

val commonModules = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    cloudModule,
    viewModelModule
)
