package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.presentation.projects.ProjectsViewModel
import io.asterixorobelix.afrikaburn.presentation.projects.ProjectTabViewModel
import io.asterixorobelix.afrikaburn.presentation.discovery.EventDiscoveryViewModel
import io.asterixorobelix.afrikaburn.presentation.discovery.ThemeCampsViewModel
import io.asterixorobelix.afrikaburn.presentation.weather.WeatherAlertsViewModel
import io.asterixorobelix.afrikaburn.ui.map.OfflineMapViewModel
import io.asterixorobelix.afrikaburn.presentation.schedule.PersonalScheduleViewModel
import io.asterixorobelix.afrikaburn.presentation.sync.SyncViewModel
import io.asterixorobelix.afrikaburn.presentation.community.LocationBasedMessagingViewModel
import io.asterixorobelix.afrikaburn.presentation.community.GiftSharingViewModel
import io.asterixorobelix.afrikaburn.presentation.community.CommunityMessagesViewModel
import io.asterixorobelix.afrikaburn.presentation.safety.SafetyViewModel
import io.asterixorobelix.afrikaburn.models.ProjectType
import org.koin.dsl.module

val presentationModule = module {
    // Existing ViewModels
    factory { ProjectsViewModel(get()) }
    
    // Factory for ProjectTabViewModel with ProjectType parameter
    factory { (projectType: ProjectType) -> 
        ProjectTabViewModel(get(), projectType) 
    }
    
    // New ViewModels
    factory { EventDiscoveryViewModel(get(), get(), get(), get()) }
    factory { ThemeCampsViewModel(get(), get()) }
    factory { WeatherAlertsViewModel(get()) }
    factory { OfflineMapViewModel(get(), get()) }
    factory { PersonalScheduleViewModel(get(), get(), get()) }
    factory { SyncViewModel(get(), get()) }
    factory { LocationBasedMessagingViewModel() }
    factory { GiftSharingViewModel() }
    factory { CommunityMessagesViewModel() }
    factory { SafetyViewModel(get(), get()) }
}