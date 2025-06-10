# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform project, targeting Android and iOS platforms. 

## Build Commands

### Building and Running

1. **Build the entire project**:
   ```bash
   ./gradlew build
   ```

2. **Run on Android**:
   ```bash
   ./gradlew composeApp:installDebug
   ```

3. **Build for iOS**:
   ```bash
   ./gradlew :composeApp:assembleReleaseXCFramework
   ```
   Then open the Xcode project in `iosApp` directory and run it from there.

4. **Clean build**:
   ```bash
   ./gradlew clean
   ```

### Code Quality & Testing

5. **Run tests**:
   ```bash
   ./gradlew test
   ```

6. **Run code quality analysis**:
   ```bash
   ./gradlew detekt
   ```

7. **Combined quality check**:
   ```bash
   ./gradlew test detekt
   ```

## Architecture Overview

### Project Structure
- `/composeApp` - Contains code shared across platforms:
  - `commonMain` - Common code for all platforms
  - `androidMain` - Android-specific code
  - `iosMain` - iOS-specific code
- `/iosApp` - iOS application entry point

### Key Architectural Components

1. **Dependency Injection**
   - Uses Koin for dependency injection
   - ViewModels and services are registered in `ViewModelModule.kt`

2. **UI Architecture**
   - Uses Compose Multiplatform for UI

## Important Details

1. **Serialization**
   - Uses Kotlinx Serialization with lenient parsing
   - JSON model classes use `@SerialName` annotations to map fields

2. **Code Quality & CI/CD**
   - Automated test execution on all pull requests
   - Detekt static analysis with mobile-specific rules
   - Comprehensive reporting in PR comments
   - Artifact generation for detailed test/quality reports
   - 7-day retention for downloadable reports

**What is this project?**
A Compose Multiplatform mobile app (iOS + Android) for AfrikaBurn, the South African regional Burning Man event. The app helps participants navigate the event, discover artworks and theme camps, plan their experience, and survive in the harsh Tankwa Karoo desert environment.

## 🏗️ Architecture Decisions

### Technology Stack
- **Frontend**: Compose Multiplatform with shared UI
- **Backend**: Ktor Server (Kotlin) with PostgreSQL + PostGIS
- **Local Database**: SQLDelight for cross-platform local storage
- **Networking**: Ktor Client with robust offline caching
- **DI**: Koin for dependency injection
- **State Management**: ViewModel + StateFlow/Compose State

### Architecture Pattern
```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│  (Compose UI + ViewModels/State)    │
├─────────────────────────────────────┤
│            Domain Layer             │
│    (Use Cases + Entities)           │
├─────────────────────────────────────┤
│             Data Layer              │
│  (Repositories + Data Sources)      │
└─────────────────────────────────────┘
```

**Why Clean Architecture?**
- Clear separation of concerns for testability
- Platform-agnostic business logic
- Easy to add new features without breaking existing code
- Supports offline-first approach with repository pattern

## 🔧 Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use Compose best practices
- Prefer immutable data structures
- Write comprehensive tests

### Material Design 3 Implementation Rules
**MANDATORY for all AI assistants working on this mobile project:**

1. **Theme Structure**:
   ```kotlin
   // AppTheme is ONLY declared at the app level in App.kt
   @Composable
   fun App() {
       AppTheme {
           // Your UI content here - screens inherit theme automatically
       }
   }
   
   // ❌ WRONG - Do NOT wrap individual screens in AppTheme
   @Composable
   fun MyScreen() {
       AppTheme {  // <- This is INCORRECT
           // content
       }
   }
   
   // ✅ CORRECT - Screens inherit theme from App.kt
   @Composable
   fun MyScreen() {
       Column {  // <- Start directly with layout
           // content uses MaterialTheme.* automatically
       }
   }
   ```

2. **Color System** (NEVER hardcode colors):
   ```kotlin
   // ✅ CORRECT - Use Material colorScheme
   MaterialTheme.colorScheme.primary
   MaterialTheme.colorScheme.secondary
   MaterialTheme.colorScheme.surface
   MaterialTheme.colorScheme.background
   MaterialTheme.colorScheme.onPrimary
   MaterialTheme.colorScheme.onSurface
   
   // ❌ WRONG - Never hardcode colors
   Color.Blue
   Color(0xFF123456)
   ```

3. **Typography System** (NEVER hardcode text styles):
   ```kotlin
   // ✅ CORRECT - Use Material typography
   MaterialTheme.typography.displayLarge     // 57sp - Large display text
   MaterialTheme.typography.displayMedium    // 45sp - Medium display text
   MaterialTheme.typography.displaySmall     // 36sp - Small display text
   MaterialTheme.typography.headlineLarge    // 32sp - Large headlines
   MaterialTheme.typography.headlineMedium   // 28sp - Medium headlines
   MaterialTheme.typography.headlineSmall    // 24sp - Small headlines
   MaterialTheme.typography.titleLarge       // 22sp - Large titles
   MaterialTheme.typography.titleMedium      // 16sp - Medium titles
   MaterialTheme.typography.titleSmall       // 14sp - Small titles
   MaterialTheme.typography.bodyLarge        // 16sp - Large body text
   MaterialTheme.typography.bodyMedium       // 14sp - Medium body text
   MaterialTheme.typography.bodySmall        // 12sp - Small body text
   MaterialTheme.typography.labelLarge       // 14sp - Large labels
   MaterialTheme.typography.labelMedium      // 12sp - Medium labels
   MaterialTheme.typography.labelSmall       // 11sp - Small labels
   
   // ❌ WRONG - Never hardcode text styles
   fontSize = 16.sp
   fontWeight = FontWeight.Bold
   ```

4. **Shape System** (Use for all components):
   ```kotlin
   // ✅ CORRECT - Use Material shapes
   MaterialTheme.shapes.small    // 4.dp - Small components
   MaterialTheme.shapes.medium   // 8.dp - Medium components  
   MaterialTheme.shapes.large    // 16.dp - Large components
   
   // ❌ WRONG - Never hardcode shapes
   RoundedCornerShape(8.dp)
   ```

5. **Spacing and Dimensions**:
   ```kotlin
   // ✅ CORRECT - Use standard Material spacing
   .padding(16.dp)        // Standard content padding
   .padding(8.dp)         // Small spacing
   .padding(24.dp)        // Large spacing
   Spacer(modifier = Modifier.height(16.dp))
   
   // For custom dimensions, create a Dimens object:
   object Dimens {
       val cardElevation = 4.dp
       val iconSize = 24.dp
       val avatarSize = 40.dp
   }
   ```

6. **Component Usage Examples**:
   ```kotlin
   // ✅ CORRECT - Proper Material 3 components
   Button(
       onClick = { },
       colors = ButtonDefaults.buttonColors(
           containerColor = MaterialTheme.colorScheme.primary
       )
   ) {
       Text(
           text = "Button Text",
           style = MaterialTheme.typography.labelLarge,
           color = MaterialTheme.colorScheme.onPrimary
       )
   }
   
   Card(
       modifier = Modifier.fillMaxWidth(),
       colors = CardDefaults.cardColors(
           containerColor = MaterialTheme.colorScheme.surface
       ),
       shape = MaterialTheme.shapes.medium,
       elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevation)
   ) {
       Column(
           modifier = Modifier.padding(Dimens.paddingMedium)
       ) {
           Text(
               text = "Card Title",
               style = MaterialTheme.typography.titleMedium,
               color = MaterialTheme.colorScheme.onSurface
           )
       }
   }
   ```

**ENFORCEMENT**: Any PR with hardcoded colors, typography, or shapes will be rejected. Always use the Material Design 3 theme system.

**CRITICAL APPTHEME RULE**: `AppTheme` is ONLY declared once in App.kt at the application level. NEVER wrap individual screens or components in `AppTheme` - they inherit theming automatically. Only use `AppTheme` in Preview functions for testing purposes.

### String Resources for Compose Multiplatform
**MANDATORY for all AI assistants working on this mobile project:**

1. **Generated String Resources Pattern**:
   ```kotlin
   // ✅ CORRECT - Use generated string resources
   import org.jetbrains.compose.resources.stringResource
   import afrikaburn.composeapp.generated.resources.Res
   import afrikaburn.composeapp.generated.resources.about_title
   import afrikaburn.composeapp.generated.resources.button_save
   // Import specific resources or use wildcard import
   // import afrikaburn.composeapp.generated.resources.*
   
   @Composable
   fun MyScreen() {
       Text(
           text = stringResource(Res.string.about_title),
           style = MaterialTheme.typography.headlineMedium
       )
   }
   
   // ❌ WRONG - Never hardcode strings
   Text(text = "Welcome to the app")
   ```

2. **String Resource Organization**:
   ```kotlin
   // Define strings in composeResources/values/strings.xml
   <resources>
       <!-- Screen titles -->
       <string name="screen_home_title">Home</string>
       <string name="screen_profile_title">Profile</string>
       
       <!-- Button labels -->
       <string name="button_save">Save</string>
       <string name="button_cancel">Cancel</string>
       <string name="button_delete">Delete</string>
       
       <!-- Messages -->
       <string name="message_loading">Loading...</string>
       <string name="message_error_generic">Something went wrong</string>
       
       <!-- Content descriptions for accessibility -->
       <string name="cd_back_button">Navigate back</string>
       <string name="cd_profile_image">Profile image</string>
   </resources>
   ```

3. **String Resource Usage Examples**:
   ```kotlin
   // ✅ CORRECT - Complete implementation with actual project imports
   import afrikaburn.composeapp.generated.resources.Res
   import afrikaburn.composeapp.generated.resources.screen_profile_title
   import afrikaburn.composeapp.generated.resources.button_save
   import afrikaburn.composeapp.generated.resources.cd_back_button
   
   @Composable
   fun ProfileScreen() {
       AppTheme {
           Column(
               modifier = Modifier
                   .fillMaxSize()
                   .background(MaterialTheme.colorScheme.background)
                   .padding(Dimens.paddingMedium)
           ) {
               Text(
                   text = stringResource(Res.string.screen_profile_title),
                   style = MaterialTheme.typography.headlineLarge,
                   color = MaterialTheme.colorScheme.onBackground
               )
               
               Button(
                   onClick = { /* save action */ },
                   colors = ButtonDefaults.buttonColors(
                       containerColor = MaterialTheme.colorScheme.primary
                   )
               ) {
                   Text(
                       text = stringResource(Res.string.button_save),
                       style = MaterialTheme.typography.labelLarge,
                       color = MaterialTheme.colorScheme.onPrimary
                   )
               }
               
               IconButton(
                   onClick = { /* back action */ }
               ) {
                   Icon(
                       imageVector = Icons.Default.ArrowBack,
                       contentDescription = stringResource(Res.string.cd_back_button),
                       tint = MaterialTheme.colorScheme.onSurface
                   )
               }
           }
       }
   }
   ```

4. **Parameterized Strings**:
   ```kotlin
   // Define in strings.xml
   <string name="welcome_user">Welcome, %1$s!</string>
   <string name="items_count">You have %1$d items</string>
   
   // Use in Compose
   Text(
       text = stringResource(Res.string.welcome_user, userName),
       style = MaterialTheme.typography.bodyLarge
   )
   
   Text(
       text = stringResource(Res.string.items_count, itemCount),
       style = MaterialTheme.typography.bodyMedium
   )
   ```

5. **Plurals Support**:
   ```kotlin
   // Define in strings.xml
   <plurals name="notification_count">
       <item quantity="zero">No notifications</item>
       <item quantity="one">1 notification</item>
       <item quantity="other">%1$d notifications</item>
   </plurals>
   
   // Use in Compose
   import org.jetbrains.compose.resources.pluralStringResource
   
   Text(
       text = pluralStringResource(Res.plurals.notification_count, count, count),
       style = MaterialTheme.typography.bodyMedium
   )
   ```

6. **Accessibility Content Descriptions**:
   ```kotlin
   // ✅ CORRECT - Always provide content descriptions
   Image(
       painter = painterResource(Res.drawable.profile_placeholder),
       contentDescription = stringResource(Res.string.cd_profile_image),
       modifier = Modifier.size(Dimens.avatarSize)
   )
   
   FloatingActionButton(
       onClick = { /* add action */ }
   ) {
       Icon(
           imageVector = Icons.Default.Add,
           contentDescription = stringResource(Res.string.cd_add_button)
       )
   }
   ```

7. **String Resource File Structure**:
   ```
   composeApp/src/commonMain/composeResources/
   ├── values/
   │   └── strings.xml           # Default (English)
   ├── values-es/
   │   └── strings.xml           # Spanish
   ├── values-fr/
   │   └── strings.xml           # French
   └── values-de/
       └── strings.xml           # German
   ```

**CRITICAL RULES**:
- **NEVER** hardcode strings in UI components
- **ALWAYS** use `stringResource(Res.string.your_string_key)`
- **ALWAYS** provide content descriptions for accessibility
- **ORGANIZE** strings logically by screens, components, or functionality
- **USE** parameterized strings for dynamic content
- **SUPPORT** plurals when dealing with counts
- **FOLLOW** consistent naming: `screen_name_element`, `button_action`, `message_type`, `cd_description`

**ENFORCEMENT**: Any PR with hardcoded strings will be rejected. All user-facing text must use the Compose Multiplatform string resource system.

### Compose Preview Requirements
**MANDATORY for all AI assistants working on this mobile project:**

#### Always Add Preview Composables - One Preview Per File
Every new Composable function MUST include corresponding `@Preview` functions for development and design validation. **CRITICAL**: Each file can only contain ONE `@Preview` function.

1. **File Structure Requirements**:
   ```kotlin
   // ✅ CORRECT - One Composable, One Preview per file
   
   // File: MyComponent.kt
   import org.jetbrains.compose.ui.tooling.preview.Preview
   import io.asterixorobelix.afrikaburn.AppTheme
   
   @Composable
   fun MyComponent() {
       // Your composable content - inherits theme from App.kt
   }
   
   @Preview
   @Composable
   private fun MyComponentPreview() {
       AppTheme {  // Only use AppTheme in previews for testing
           MyComponent()
       }
   }
   
   // ❌ WRONG - Multiple Previews in same file
   // @Preview fun AnotherPreview() { ... }  // This would be rejected
   ```

2. **Component Separation Pattern**:
   ```kotlin
   // ✅ CORRECT - Separate files for each component
   
   // File: AboutPageContent.kt
   @Composable
   fun AboutPageContent(title: String, content: String) {
       // Component implementation
   }
   
   @Preview
   @Composable
   private fun AboutPageContentPreview() {
       AppTheme {
           AboutPageContent(
               title = "Welcome to AfrikaBurn",
               content = "Your companion app for the AfrikaBurn experience in the Tankwa Karoo."
           )
       }
   }
   
   // File: PageIndicator.kt (separate file)
   @Composable
   fun PageIndicator(currentPage: Int, totalPages: Int) {
       // Component implementation
   }
   
   @Preview
   @Composable
   private fun PageIndicatorPreview() {
       AppTheme {
           // Show multiple states in ONE preview
           Column {
               PageIndicator(currentPage = 0, totalPages = 4)
               PageIndicator(currentPage = 2, totalPages = 4) 
           }
       }
   }
   ```

3. **Preview Best Practices**:
   - **ALWAYS** wrap previews in `AppTheme` for accurate theming
   - **USE** realistic sample data, not Lorem Ipsum
   - **INCLUDE** multiple states within ONE preview when relevant (selected/unselected, different data)
   - **MAKE** previews `private` to keep them internal to the file
   - **PROVIDE** meaningful preview names ending with "Preview"
   - **USE** proper background colors for visibility
   - **SHOW** edge cases like long text, empty states, error states
   - **CREATE** separate files when you need multiple distinct Composables

4. **Multi-State Single Preview Example**:
   ```kotlin
   // File: MyButton.kt
   @Preview
   @Composable
   private fun MyButtonPreview() {
       AppTheme {
           Column(
               modifier = Modifier
                   .background(MaterialTheme.colorScheme.background)
                   .padding(Dimens.paddingMedium),
               verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
           ) {
               // Show ALL states in ONE preview
               MyButton(text = "Normal", enabled = true)
               MyButton(text = "Disabled", enabled = false)
               MyButton(text = "Very Long Button Text That Might Wrap", enabled = true)
           }
       }
   }
   ```

**CRITICAL RULES**:
- **ALWAYS** import `org.jetbrains.compose.ui.tooling.preview.Preview` when creating Composables (Compose Multiplatform)
- **ALWAYS** import `io.asterixorobelix.afrikaburn.AppTheme` for preview theming
- **NEVER** create Composables without corresponding previews
- **NEVER** put multiple `@Preview` functions in the same file
- **ALWAYS** create separate files for each distinct Composable component
- **ALWAYS** use `AppTheme` wrapper in previews
- **INCLUDE** all major component states within ONE preview function
- **USE** realistic data from the AfrikaBurn context
- **MAKE** previews visible in Android Studio design panel

**FILE STRUCTURE REQUIREMENT**:
```
ui/about/
├── AboutScreen.kt          // Main screen + ONE preview
├── AboutPageContent.kt     // Component + ONE preview  
├── PageIndicator.kt        // Component + ONE preview
└── SomeOtherComponent.kt   // Component + ONE preview
```

**ENFORCEMENT**: Any PR with multiple `@Preview` functions in a single file will be rejected. Any PR with new Composables lacking proper `@Preview` functions and required imports will be rejected.

### Data Class Organization
**MANDATORY for all AI assistants working on this mobile project:**

#### Always Place Data Classes in Separate Files in Models Folder
All data classes must be organized in individual files within the `models` package for better maintainability and reusability.

1. **Models Folder Structure**:
   ```
   composeApp/src/commonMain/kotlin/io/asterixorobelix/afrikaburn/models/
   ├── Artist.kt
   ├── ProjectItem.kt
   ├── TabDataSource.kt
   └── [OtherModel].kt
   ```

2. **One Data Class Per File Rule**:
   ```kotlin
   // ✅ CORRECT - Each data class in its own file
   
   // File: models/Artist.kt
   package io.asterixorobelix.afrikaburn.models
   
   import kotlinx.serialization.SerialName
   import kotlinx.serialization.Serializable
   
   @Serializable
   data class Artist(
       @SerialName("s") val name: String = ""
   )
   
   // File: models/ProjectItem.kt
   package io.asterixorobelix.afrikaburn.models
   
   import kotlinx.serialization.SerialName
   import kotlinx.serialization.Serializable
   
   @Serializable
   data class ProjectItem(
       @SerialName("Name") val name: String,
       @SerialName("Description") val description: String,
       @SerialName("Artist") val artist: Artist = Artist(),
       @SerialName("code") val code: String = "",
       @SerialName("status") val status: String = ""
   )
   ```

3. **Import Models in UI Files**:
   ```kotlin
   // ✅ CORRECT - Import models from dedicated package
   import io.asterixorobelix.afrikaburn.models.Artist
   import io.asterixorobelix.afrikaburn.models.ProjectItem
   import io.asterixorobelix.afrikaburn.models.TabDataSource
   
   @Composable
   fun ProjectsScreen() {
       // Use imported models
       var projects by remember { mutableStateOf<List<ProjectItem>?>(null) }
   }
   
   // ❌ WRONG - Data classes defined in UI files
   @Composable
   fun SomeScreen() {
       // UI implementation
   }
   
   data class SomeModel(val name: String) // <- This is INCORRECT
   ```

4. **Serialization Best Practices**:
   ```kotlin
   // ✅ CORRECT - Proper serialization annotations
   @Serializable
   data class Event(
       @SerialName("Name") val name: String,
       @SerialName("Description") val description: String,
       @SerialName("Artist") val artist: Artist = Artist(),
       @SerialName("code") val code: String = "",
       @SerialName("status") val status: String = ""
   )
   
   // ✅ CORRECT - Non-serializable internal models
   data class TabDataSource(
       val fileName: String,
       val displayName: String
   )
   ```

5. **File Naming Conventions**:
   - Use PascalCase for file names matching the data class name
   - File name must exactly match the data class name
   - Examples: `Artist.kt`, `ProjectItem.kt`, `EventDetails.kt`

**CRITICAL RULES**:
- **NEVER** define data classes in UI files (screens, components)
- **ALWAYS** create a separate file for each data class in the `models` package
- **ALWAYS** use proper package declaration: `package io.asterixorobelix.afrikaburn.models`
- **INCLUDE** appropriate serialization annotations when needed
- **IMPORT** models explicitly in files that use them
- **FOLLOW** consistent naming conventions

**ENFORCEMENT**: Any PR with data classes defined outside the `models` package or multiple data classes in a single file will be rejected. All data models must be properly organized for maintainability and reusability.

### Testing Strategy
- Unit tests for business logic (80%+ coverage)
- Integration tests for repositories
- UI tests for critical user flows
- Manual testing on real devices

### Crash Reporting & Logging
**MANDATORY for all AI assistants working on this mobile project:**

#### Crash Logging Service
This project uses **Firebase Crashlytics** for comprehensive crash reporting across Android and iOS platforms.

#### Setup Requirements
1. **Firebase Configuration**:
   ```bash
   # Copy template and configure with your Firebase project
   cp google-services.json.template composeApp/google-services.json
   # Update with your actual Firebase project credentials
   ```

2. **iOS Configuration** (for production):
   - Add `GoogleService-Info.plist` to `iosApp/iosApp/`
   - Configure Firebase iOS SDK in native iOS code

#### Usage Patterns
```kotlin
// ✅ CORRECT - Always inject crash logger via Koin
@Composable
fun MyScreen() {
    val crashLogger: CrashLogger = koinInject()
    
    LaunchedEffect(Unit) {
        crashLogger.setCustomKey("screen", "MyScreen")
    }
    
    AppTheme {
        // Handle errors gracefully
        try {
            // Your UI code
        } catch (e: Exception) {
            crashLogger.logException(e, "Error in MyScreen")
            // Show user-friendly error message
        }
    }
}

// ✅ CORRECT - ViewModel error handling
class MyViewModel(
    private val crashLogger: CrashLogger
) : ViewModel() {
    
    fun performAction() {
        viewModelScope.launch {
            try {
                // Business logic
            } catch (e: Exception) {
                crashLogger.logException(e, "Error in performAction")
                // Handle error state
            }
        }
    }
}
```

#### Required Practices
1. **Error Boundaries**: Always wrap risky operations in try-catch
2. **Context Logging**: Use `setCustomKey()` to add context
3. **User Identification**: Call `setUserId()` when user logs in
4. **Non-Fatal Exceptions**: Log non-fatal errors with `logException()`
5. **Custom Messages**: Use `log()` for important app events

#### Platform-Specific Features
- **Android**: Full Firebase Crashlytics integration with symbolication
- **iOS**: Basic NSLog implementation (expandable to Firebase iOS SDK)

#### Testing
```kotlin
// ✅ For testing crash reporting (DEBUG ONLY)
if (BuildConfig.DEBUG) {
    crashLogger.testCrash() // This will trigger a test crash
}
```

**ENFORCEMENT**: All error-prone operations must include proper crash logging.

### Dependency Management with Version Catalogs
**MANDATORY for all AI assistants working on this mobile project:**

#### Always Use libs.versions.toml
This project uses Gradle Version Catalogs for centralized dependency management. **NEVER** add dependencies directly to build.gradle.kts files.

#### Required Pattern for Adding Dependencies

1. **Add Version to libs.versions.toml**:
   ```toml
   [versions]
   retrofit = "2.9.0"
   okhttp = "4.12.0"
   ```

2. **Add Library Declaration**:
   ```toml
   [libraries]
   retrofit-core = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
   retrofit-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "retrofit" }
   okhttp-bom = { module = "com.squareup.okhttp3:okhttp-bom", version.ref = "okhttp" }
   okhttp-core = { module = "com.squareup.okhttp3:okhttp" }
   ```

3. **Add Plugin Declaration (if needed)**:
   ```toml
   [plugins]
   kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
   ```

4. **Use in build.gradle.kts**:
   ```kotlin
   // ✅ CORRECT - Use version catalog references
   implementation(libs.retrofit.core)
   implementation(libs.retrofit.gson)
   implementation(platform(libs.okhttp.bom))
   implementation(libs.okhttp.core)
   
   // ❌ WRONG - Never hardcode dependencies
   implementation("com.squareup.retrofit2:retrofit:2.9.0")
   implementation("com.squareup.okhttp3:okhttp:4.12.0")
   ```

#### Version Catalog Benefits
- **Centralized Management**: All versions in one place
- **Type Safety**: IDE autocompletion and compile-time validation
- **Consistency**: Same versions across all modules
- **Easy Updates**: Update version once, affects all modules
- **Build Performance**: Better dependency resolution

#### Existing Dependencies Pattern
The project already follows this pattern. Example from current setup:
```toml
[versions]
koinVersion = "4.0.4"
kotlin = "2.1.21"
androidx-lifecycle = "2.9.0"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koinVersion" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koinCompose" }
androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", version.ref = "androidx-lifecycle" }
```

#### Usage in Build Files
```kotlin
// In composeApp/build.gradle.kts
commonMain.dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
}

androidMain.dependencies {
    // For BOM dependencies, use direct strings with version from catalog
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    
    // For regular dependencies, use version catalog
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)
}
```

#### Special Cases: BOM Dependencies
For Bill of Materials (BOM) dependencies like Firebase, use this pattern:
```kotlin
// ✅ CORRECT - Version catalog BOMs
   implementation(project.dependencies.platform(libs.firebase.bom))
   implementation(libs.firebase.crashlytics)
   implementation(libs.firebase.analytics)

// ❌ AVOID - BOM with hardcoded version 
   implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
   implementation("com.google.firebase:firebase-crashlytics")
   implementation("com.google.firebase:firebase-analytics")
```

**CRITICAL RULES**:
- **NEVER** add dependencies directly to build.gradle.kts
- **ALWAYS** define versions in `[versions]` section first
- **ALWAYS** create library aliases in `[libraries]` section
- **USE** descriptive names with dots (e.g., `retrofit.core`, `okhttp.logging`)
- **GROUP** related dependencies with common prefixes
- **UPDATE** existing dependencies by modifying libs.versions.toml only

**ENFORCEMENT**: Any PR adding dependencies without using the version catalog will be rejected.

### Platform-Specific Notes

#### Android
- Min SDK 24 (Android 7.0)
- Target latest stable SDK
- Use Material 3 design system
- Support for foldable devices

#### iOS
- iOS 14+ minimum
- Support iPhone and iPad
- Follow Apple HIG
- Optimize for different screen sizes

## 🔄 CI/CD Pipeline

### Current Automation
- ✅ **Automated testing** on every PR with detailed reporting
- ✅ **Code quality analysis** using Detekt with mobile-specific rules
- ✅ **Comprehensive PR comments** with test results and artifact links
- ✅ **Multi-platform builds** (Android APK + iOS XCFramework)
- ✅ **Artifact uploads** with 7-day retention for detailed analysis
- 🔄 Beta deployments on main branch merges (planned)
- ✅ Production releases via Git tags
- ✅ Security scanning and dependency updates

### Quality Gates
- ✅ **Code quality** (Detekt with custom mobile rules)
- ✅ **Test execution** with pass/fail reporting
- ✅ **Build verification** for both Debug and Release variants
- ✅ **Performance checks** (APK size monitoring)
- 🔄 Test coverage tracking (planned)
- ✅ Security scanning (planned)

### 📊 What You Get in PRs
- **Automated test result comments** with pass/fail breakdowns
- **Code quality analysis** with issue counts and guidance
- **Direct links to detailed reports** via artifacts
- **Clear action items** when issues are found
- **Status indicators** (✅ ❌ ⚠️) for quick assessment

## 🤝 Contribution Guidelines

### For AI Assistants
- Always consider offline-first constraints
- Follow established architecture patterns
- Write comprehensive tests for new features (CI will validate)
- Consider battery and performance impact
- Ensure accessibility compliance
- **CRITICAL: Material Design Theme System Requirements**
  - **ALWAYS** use `AppTheme` as the root theme for all UI components
  - **MANDATORY** use Material 3 design tokens:
    - `MaterialTheme.colorScheme` for all colors (primary, secondary, surface, etc.)
    - `MaterialTheme.typography` for all text styles (displayLarge, headlineMedium, bodyLarge, etc.)
    - `MaterialTheme.shapes` for all component shapes (small, medium, large)
    - **NEVER** hardcode colors, dimensions, or typography values
  - **Required Pattern**:
    ```kotlin
    @Composable
    fun MyScreen() {
        AppTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp) // Use standard Material spacing
            ) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    // Card content
                }
            }
        }
    }
    ```
  - **Use Material Components**: Card, Button, OutlinedButton, TextField, etc. with their default Material 3 styling
  - **Color Usage Rules**:
    - Background: `MaterialTheme.colorScheme.background`
    - Surface: `MaterialTheme.colorScheme.surface`
    - Primary actions: `MaterialTheme.colorScheme.primary`
    - Text on background: `MaterialTheme.colorScheme.onBackground`
    - Text on surface: `MaterialTheme.colorScheme.onSurface`
  - **Typography Rules**:
    - Large headings: `MaterialTheme.typography.displayLarge/Medium/Small`
    - Section headings: `MaterialTheme.typography.headlineLarge/Medium/Small`
    - Body text: `MaterialTheme.typography.bodyLarge/Medium/Small`
    - Labels: `MaterialTheme.typography.labelLarge/Medium/Small`
  - **Spacing Guidelines**:
    - Use standard Material spacing: 4.dp, 8.dp, 12.dp, 16.dp, 20.dp, 24.dp, 32.dp
    - For custom dimensions, define them in a `Dimens` object
- **NEW**: Be aware of automated CI/CD pipeline:
  - Tests run automatically on PR creation
  - Detekt analysis provides code quality feedback
  - Review PR comments for automated feedback
  - Check artifacts for detailed test/quality reports
  - Address CI feedback before merging

### Code Review Focus
- Offline functionality testing
- Battery usage impact
- Security implications
- Accessibility compliance
- Cultural sensitivity
- Performance optimization
- **NEW**: CI/CD Integration:
  - Review automated test results in PR comments
  - Check code quality feedback from Detekt
  - Download artifacts for detailed analysis when needed
  - Ensure all CI checks pass before approval
---