# KMP/CMP Pitfalls Research

> Research compiled: January 2026
> Target: Kotlin Multiplatform + Compose Multiplatform production template for solo developers
> Platforms: Android API 24+, iOS 14+

---

## Critical Pitfalls

### 1. Building All iOS Architectures Unnecessarily

**Description**: The Kotlin/Native compiler builds separate binaries for each iOS architecture (arm64 device, x64 simulator, arm64 simulator). Building all three takes roughly 3x as long as building one.

**Warning Signs**:
- iOS CI builds taking 15-30+ minutes
- Local iOS builds significantly slower than Android
- Xcode building when running iOS simulator tests

**Impact**: HIGH - Build times can exceed 20 minutes unnecessarily

**Prevention**:
- Use `embedAndSignAppleFrameworkForXcode` for local development (auto-selects correct architecture)
- In CI, run only the architecture you need: `iosSimulatorArm64Test` OR `iosX64Test`
- Never run global `build` task in CI when only testing iOS

**Phase**: Phase 1 (Project Setup)

**Confidence**: HIGH

**Sources**:
- [Touchlab: Beware of Build Time Bloat](https://touchlab.co/touchlab-build-only-what-you-need)
- [How I Fixed My KMP iOS Build](https://medium.com/@houssembababendermel/how-i-fixed-my-kmp-ios-build-from-20-minute-builds-to-lightning-fast-c4f0f5c102b0)

---

### 2. Using CocoaPods with SPM Dependencies (Transitive Conflicts)

**Description**: When a KMP CocoaPods dependency uses the same Google/Firebase dependency as an SPM dependency in your iOS app, you get mysterious linker errors and framework conflicts.

**Warning Signs**:
- "Framework not found" errors after adding new iOS dependencies
- Linker errors mentioning duplicate symbols
- Strange runtime crashes after CocoaPods update
- Privacy manifest conflicts (especially with Google SDKs)

**Impact**: HIGH - Can block iOS builds entirely

**Prevention**:
- Prefer direct framework linking over CocoaPods for KMP
- Use `embedAndSignAppleFrameworkForXcode` instead of SPM for local builds
- If using CocoaPods: always open `.xcworkspace`, never `.xcodeproj`
- Audit transitive dependencies before adding new SDKs

**Phase**: Phase 1 (Project Setup)

**Confidence**: HIGH

**Sources**:
- [Touchlab: Local or Remote Framework Integration](https://touchlab.co/ios-framework-local-or-remote)
- [Taming the KMP Beast: Firebase & CocoaPods](https://medium.com/@hgarcia.alberto/taming-the-kmp-beast-my-firebase-cocoapods-saga-on-ios-and-how-you-can-win-too-313a6a52d382)

---

### 3. Gradle Version Incompatibilities with KMP

**Description**: KMP has specific Gradle version requirements. Using incompatible versions causes deprecation warnings, build failures, or subtle runtime issues.

**Warning Signs**:
- Deprecation warnings about `withJava()` function
- Build failures after Gradle upgrade
- "Incompatible ABI version" errors
- Android Gradle Plugin compatibility warnings

**Impact**: HIGH - Can break builds completely

**Prevention**:
- Check [KMP Compatibility Guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html) before upgrading
- Kotlin 2.0.20-2.1.10 works with Gradle 8.0-8.6 (8.7+ has caveats)
- Remove `withJava()` for Gradle 8.7+ (Java source sets created by default in Kotlin 2.1.20+)
- Don't use Gradle Application plugin with KMP on Gradle 8.7+
- Plan for AGP 9.0 (Q4 2025) deprecation of current KMP APIs

**Phase**: Phase 1 (Project Setup)

**Confidence**: HIGH

**Sources**:
- [Kotlin Multiplatform Compatibility Guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Android Gradle Library Plugin for KMP](https://developer.android.com/kotlin/multiplatform/plugin)

---

### 4. Navigation State Loss in Compose Multiplatform

**Description**: Navigation state (including ViewModel instances) can be unexpectedly reset when navigating between screens, especially with bottom navigation on iOS.

**Warning Signs**:
- ViewModels recreated when returning to a screen
- Bottom navigation tabs losing scroll position
- Form data disappearing when switching tabs
- iOS behaving differently than Android for navigation

**Impact**: HIGH - Poor UX, data loss, user complaints

**Prevention**:
- Test navigation thoroughly on BOTH platforms early
- Consider Decompose or PreCompose for complex navigation
- For nested NavHost, be aware that iOS doesn't save state automatically (as of April 2024)
- Use SavedStateHandle for critical state preservation
- Implement manual state persistence for complex screens

**Phase**: Phase 3 (Core Features)

**Confidence**: HIGH

**Sources**:
- [JetBrains Issue #5072: ViewModel and navigation](https://github.com/JetBrains/compose-multiplatform/issues/5072)
- [JetBrains Issue #4735: Nested NavHostController state](https://github.com/JetBrains/compose-multiplatform/issues/4735)
- [PreCompose Library](https://github.com/Tlaster/PreCompose)

---

### 5. Firebase Integration Without Platform-Specific Setup

**Description**: Firebase doesn't initialize automatically in KMP projects. The `google-services.json` isn't picked up in the new project structure, and iOS requires separate dSYM upload configuration.

**Warning Signs**:
- "Firebase(app) not initialized correctly" crashes
- iOS crashes not appearing in Crashlytics dashboard
- "Missing UUID" in Kotlin crash stack traces
- Analytics events not recording

**Impact**: HIGH - No crash reporting or analytics in production

**Prevention**:
- Manually call `Firebase.initialize(context)` on Android
- Set up dSYM upload build phase for iOS (especially for dynamic frameworks)
- Use expect/actual pattern for Firebase interfaces
- Test crash reporting on BOTH platforms before release
- Consider GitLive firebase-kotlin-sdk for multiplatform support

**Phase**: Phase 2 (Infrastructure)

**Confidence**: HIGH

**Sources**:
- [GitLive Firebase SDK Issue #560](https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/560)
- [Firebase Crashlytics KMP Setup](https://slack-chats.kotlinlang.org/t/22739156/to-make-firebase-crashlytics-work-in-a-up-to-date-kmp-projec)
- [KMP Firebase Setup Guide](https://funkymuse.dev/posts/kmp-firebase/)

---

### 6. iOS Privacy Manifest Missing/Incomplete

**Description**: Since Spring 2024, Apple requires privacy manifests explaining why your app uses certain APIs. KMP apps are particularly vulnerable because the Kotlin runtime uses APIs that require disclosure.

**Warning Signs**:
- App Store Connect warnings about privacy manifest
- Rejection during App Store review
- No privacy manifest file in your iOS bundle

**Impact**: CRITICAL - App Store rejection

**Prevention**:
- Add privacy manifest to your iOS target before first submission
- Follow [JetBrains Privacy Manifest Guide](https://kotlinlang.org/docs/multiplatform/multiplatform-privacy-manifest.html)
- Audit third-party SDKs for their required reason API usage
- Test with Xcode's privacy report feature before submission

**Phase**: Phase 4 (Release)

**Confidence**: HIGH

**Sources**:
- [Kotlin Multiplatform Privacy Manifest](https://kotlinlang.org/docs/multiplatform/multiplatform-privacy-manifest.html)
- [App Store 2024 Privacy Changes](https://www.marcogomiero.com/posts/2024/kmp-ci-ios/)

---

### 7. MockK Not Working on iOS/Native Targets

**Description**: MockK only supports JVM and Android targets. Attempting to use it in commonTest for iOS tests causes build failures or silent test skipping.

**Warning Signs**:
- "Unresolved reference" errors when running iOS tests
- Tests passing on Android but failing/skipping on iOS
- "Test event was not received" for iOS targets
- MockK dependency causing multiplatform build to fail

**Impact**: MEDIUM - Inadequate test coverage on iOS

**Prevention**:
- Use Mockative or MocKMP for true multiplatform mocking
- Write tests in JVM source set for common code when possible
- Consider fake objects instead of mocks for critical paths
- Clearly separate platform-specific tests from common tests

**Phase**: Phase 2 (Infrastructure)

**Confidence**: HIGH

**Sources**:
- [MockK Issue #1019](https://github.com/mockk/mockk/issues/1019)
- [Mockative Library](https://github.com/mockative/mockative)
- [KMM Testing Architecture](https://medium.com/@maruchin/kmm-architecture-7-testing-93efd01f3952)

---

### 8. Compose Multiplatform UI Performance on iOS

**Description**: Compose Multiplatform on iOS can have significant performance overhead: +5ms per frame average, +25-50MB memory baseline, frame drops during animations.

**Warning Signs**:
- Visible jank during scrolling or animations on iOS
- High CPU usage causing device to warm up
- Memory warnings or app crashes on iOS
- User reviews mentioning iOS being "slower" than Android version

**Impact**: HIGH - Poor user experience, negative reviews

**Prevention**:
- Profile early and often on real iOS devices
- Avoid deeply nested Composables
- Use lazy layouts (LazyColumn/LazyRow) for lists
- Minimize custom Canvas drawing
- Test on older iOS devices (not just latest)
- Consider native UI for performance-critical screens
- Use static frameworks with `-dead_strip` linker option

**Phase**: Phase 3 (Core Features)

**Confidence**: HIGH

**Sources**:
- [Compose Multiplatform iOS Performance Issue #4912](https://github.com/JetBrains/compose-multiplatform/issues/4912)
- [Should You Trust CMP with Your Established App?](https://medium.com/@naufalprakoso24/should-you-trust-compose-multiplatform-with-your-established-app-9f921a9a47aa)
- [KotlinConf 2024: CMP Performance on iOS](https://kotlinconf.com/2024/talks/578918/)

---

## Technical Debt Patterns

| Pattern | Description | Early Warning | Prevention | Phase |
|---------|-------------|--------------|------------|-------|
| Monolithic shared module | All code in single `shared` module | Build times > 2 min, hard to navigate | Split into feature modules early | Phase 1 |
| Platform code in common | Using expect/actual when abstraction unnecessary | Many empty `actual` implementations | Only use expect/actual for true platform differences | Phase 2 |
| Skipping compilation checks | Not running `compileDebugKotlinAndroid` after changes | Errors accumulate, debugging becomes hard | Compile after every 5 file changes | All phases |
| Hardcoded strings in Composables | `Text("Hello")` instead of `stringResource()` | No localization capability | Use Compose Resources from day one | Phase 2 |
| Missing content descriptions | Icons without accessibility text | App fails accessibility audits | Add contentDescription to all interactive elements | Phase 3 |
| Ignoring Detekt/lint warnings | Suppressing warnings instead of fixing | Technical debt accumulates | Fix warnings as part of PR process | All phases |
| Manual dependency versions | Not using version catalog | Version conflicts, inconsistent updates | Use `libs.versions.toml` exclusively | Phase 1 |
| No offline-first design | Always fetching from network | App unusable without internet | Design repository layer for offline from start | Phase 2 |

---

## Integration Gotchas

| Integration | Gotcha | Solution | Confidence |
|-------------|--------|----------|------------|
| Firebase Crashlytics (iOS) | dSYMs not uploaded, Kotlin crashes show "Missing UUID" | Add build phase to upload Kotlin framework dSYM separately | HIGH |
| Firebase Analytics | Platform-specific initialization required | Use expect/actual with platform initializers | HIGH |
| Ktor Client | Different engines per platform have different timeout/SSL options | Configure engine-specific options in platform source sets | HIGH |
| SQLDelight | FreezingException with new memory model + old coroutines | Upgrade to SQLDelight 2.x, use new memory model | HIGH |
| Room | KMP support requires specific Kotlin/KSP versions | Check Room KMP compatibility matrix | MEDIUM |
| Stripe/Payment SDKs | No KMP support, requires native UI interop | Use UIKitViewController/AndroidView for payment screens | HIGH |
| Google Sign-In | No official KMP library | Use expect/actual with native SDKs | HIGH |
| Push Notifications | Platform-specific token handling | Abstract behind common interface, implement per platform | HIGH |
| Deep Linking | Navigation library differences | Test deep links on both platforms thoroughly | MEDIUM |
| Biometric Auth | Different APIs (BiometricPrompt vs LocalAuthentication) | Use expect/actual, handle fallbacks consistently | HIGH |

---

## Performance Traps

| Trap | Impact | Detection | Mitigation | Phase |
|------|--------|-----------|------------|-------|
| Building all iOS architectures | 3x slower builds | CI taking 15-30+ minutes | Build only needed architecture | Phase 1 |
| Large framework size | Slow app startup, large download | IPA > 100MB | Use static framework, enable dead stripping | Phase 4 |
| Recomposition storms | UI jank, high CPU | Profiler shows many recompositions | Use `remember`, `derivedStateOf`, stable types | Phase 3 |
| Missing Gradle caching | Slow CI, repeated work | Same tasks rebuilding | Enable configuration cache, dependency caching | Phase 1 |
| Synchronous IO on main thread | ANRs, frozen UI | StrictMode violations, user reports | Use `withContext(Dispatchers.IO)` | Phase 2 |
| Large images in memory | OOM crashes, especially iOS | Memory profiler warnings | Use Coil/Kamel with proper sizing | Phase 3 |
| Excessive logging in production | Performance degradation | Large log outputs | Set `minLogLevel` to INFO in release builds | Phase 4 |
| Network calls without timeout | Hanging requests, poor UX | No timeout configured in Ktor | Configure request/connect/socket timeouts | Phase 2 |

---

## Security Mistakes

| Mistake | Risk | Detection | Prevention | Phase |
|---------|------|-----------|------------|-------|
| API keys in source code | Key exposure via reverse engineering | Git history, APK/IPA analysis | Use BuildConfig/environment variables | Phase 1 |
| Storing secrets in SharedPreferences/UserDefaults | Plaintext secrets on device | File system inspection | Use Android Keystore/iOS Keychain | Phase 2 |
| Missing certificate pinning | MITM attacks | Proxy interception works | Configure SSL pinning in Ktor engine | Phase 2 |
| Ignoring supply chain vulnerabilities | Compromised dependencies | No scanning in place | Enable Dependabot/Snyk/OSV scanning | Phase 1 |
| Logging sensitive data | PII/credentials in logs | Log inspection | Never log tokens, passwords, PII | All phases |
| Missing ProGuard rules | Serialization models stripped | Runtime crashes | Add keep rules for serialization, Ktor, Koin | Phase 4 |
| Insecure JWT storage | Token theft | Security audit | Store tokens in secure storage only | Phase 2 |
| Missing input validation | Injection attacks, crashes | Fuzzing, security testing | Validate all user input and API responses | Phase 2 |

---

## UX Pitfalls

| Pitfall | User Impact | Detection | Fix | Phase |
|---------|-------------|-----------|-----|-------|
| Material Design on iOS | App feels "foreign" to iOS users | User feedback, low ratings | Use platform-specific styling or native UI | Phase 3 |
| iOS navigation not native | Transitions feel wrong | Side-by-side comparison with native iOS | Consider native navigation for complex flows | Phase 3 |
| Missing iOS-specific gestures | Swipe-to-go-back doesn't work | User testing | Handle touch interop properly | Phase 3 |
| Inconsistent loading states | Jarring experience | UX review | Use skeleton loaders, consistent patterns | Phase 3 |
| Poor error messages | Users don't know what went wrong | Support tickets | Provide actionable, user-friendly error messages | Phase 3 |
| Missing offline indicators | Users confused when offline | User testing | Show network status, queue actions | Phase 3 |
| Accessibility not tested | App unusable for some users | Accessibility scanner | Test with TalkBack/VoiceOver regularly | Phase 3 |
| Text fields behave differently | iOS battery-saving mode causes jumping | iOS device testing | Test on iOS in battery-saving mode | Phase 3 |

---

## "Looks Done But Isn't" Checklist

Before marking a feature complete, verify:

### Build & CI
- [ ] Builds successfully on clean checkout (no local caches)
- [ ] CI passes for both Android and iOS
- [ ] No new Detekt/lint warnings introduced
- [ ] Build time hasn't increased significantly

### Platform Parity
- [ ] Tested on real Android device (not just emulator)
- [ ] Tested on real iOS device (not just simulator)
- [ ] Tested on iOS in battery-saving mode
- [ ] Navigation works identically on both platforms
- [ ] Keyboard handling works on both platforms

### State Management
- [ ] State survives configuration change (Android rotation)
- [ ] State survives process death (Android background kill)
- [ ] Back navigation preserves expected state
- [ ] Tab switching preserves state (if applicable)

### Error Handling
- [ ] Network errors handled gracefully
- [ ] Empty states displayed appropriately
- [ ] Loading states shown during async operations
- [ ] Errors logged to crash reporting (non-fatal)

### Security
- [ ] No secrets in source code or logs
- [ ] Sensitive data uses secure storage
- [ ] API calls use HTTPS only

### Accessibility
- [ ] All interactive elements have content descriptions
- [ ] Contrast ratios meet WCAG guidelines
- [ ] Touch targets are at least 48dp

### Release Readiness
- [ ] String resources used (no hardcoded text)
- [ ] Debug logging removed or filtered
- [ ] ProGuard rules tested with release build
- [ ] Privacy manifest updated (iOS)

---

## Recovery Strategies

| Problem | Symptoms | Immediate Fix | Long-term Solution |
|---------|----------|---------------|-------------------|
| iOS build broken after Kotlin upgrade | "Incompatible ABI version" errors | Revert Kotlin version, clean build | Align all KMP library versions with Kotlin version |
| Firebase not reporting iOS crashes | Crashes visible in Xcode but not Firebase | Manually upload dSYMs | Add dSYM upload to CI pipeline |
| CI taking 30+ minutes | GitHub Actions timing out | Cancel redundant jobs, reduce matrix | Optimize architecture builds, add caching |
| Navigation state lost | Users complaining about lost data | Add SavedStateHandle usage | Audit all navigation for state preservation |
| App rejected for privacy manifest | App Store rejection email | Add privacy manifest immediately | Audit all SDKs for required reason APIs |
| Memory crashes on iOS | App terminating, high memory warnings | Reduce image sizes, check for leaks | Profile memory, implement proper cleanup |
| Tests pass locally, fail in CI | Green local, red CI | Check environment differences | Standardize CI environment to match local |
| Ktor timeout on Android | Works initially, then times out | Increase timeout values | Investigate connection pooling, retry logic |

---

## Pitfall-to-Phase Mapping

### Phase 1: Project Setup
| Pitfall | Priority | Effort |
|---------|----------|--------|
| Building all iOS architectures | HIGH | LOW |
| CocoaPods/SPM conflicts | HIGH | MEDIUM |
| Gradle version incompatibilities | HIGH | MEDIUM |
| Missing version catalog | MEDIUM | LOW |
| No supply chain scanning | MEDIUM | LOW |

### Phase 2: Infrastructure
| Pitfall | Priority | Effort |
|---------|----------|--------|
| Firebase integration issues | HIGH | MEDIUM |
| MockK not working on iOS | MEDIUM | MEDIUM |
| Ktor client misconfiguration | MEDIUM | LOW |
| Insecure secret storage | HIGH | MEDIUM |
| Missing offline-first design | MEDIUM | HIGH |

### Phase 3: Core Features
| Pitfall | Priority | Effort |
|---------|----------|--------|
| Navigation state loss | HIGH | HIGH |
| Compose performance on iOS | HIGH | HIGH |
| Material Design on iOS | MEDIUM | HIGH |
| Missing accessibility | MEDIUM | MEDIUM |
| Recomposition storms | MEDIUM | MEDIUM |

### Phase 4: Release
| Pitfall | Priority | Effort |
|---------|----------|--------|
| iOS privacy manifest | CRITICAL | LOW |
| Missing ProGuard rules | HIGH | MEDIUM |
| Large framework size | MEDIUM | MEDIUM |
| Debug logging in production | HIGH | LOW |
| dSYM upload for iOS | HIGH | LOW |

---

## Sources Summary

### High Confidence Sources (Post-mortems, Official Docs, Experienced Developers)
- [JetBrains KMP Compatibility Guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [JetBrains Privacy Manifest Guide](https://kotlinlang.org/docs/multiplatform/multiplatform-privacy-manifest.html)
- [Touchlab Build Time Optimization](https://touchlab.co/touchlab-build-only-what-you-need)
- [Touchlab Framework Integration](https://touchlab.co/ios-framework-local-or-remote)
- [ProAndroidDev: KMP Scalability Challenges](https://proandroiddev.com/kotlin-multiplatform-scalability-challenges-on-a-large-project-b3140e12da9d)
- [GitLive Firebase SDK Issues](https://github.com/GitLiveApp/firebase-kotlin-sdk/issues)
- [JetBrains Compose Multiplatform Issues](https://github.com/JetBrains/compose-multiplatform/issues)

### Medium Confidence Sources (Community Discussions, Blog Posts)
- [Kotlin Slack #multiplatform Channel](https://slack-chats.kotlinlang.org/)
- [KMP Production Ready 2025 Analysis](https://volpis.com/blog/is-kotlin-multiplatform-production-ready/)
- [CI/CD for KMP 2025 Guide](https://www.kmpship.app/blog/ci-cd-kotlin-multiplatform-2025)
- [Medium: CMP with Established Apps](https://medium.com/@naufalprakoso24/should-you-trust-compose-multiplatform-with-your-established-app-9f921a9a47aa)

### Low Confidence Sources (General Advice, Unverified Claims)
- General blog posts without specific project context
- Advice for older Kotlin versions (pre-2.0)
- Recommendations without production validation

---

## Version Information

This research is based on:
- Kotlin 2.0.x - 2.1.x
- Compose Multiplatform 1.6.x - 1.8.x
- Gradle 8.x
- Android Gradle Plugin 8.x
- Xcode 15.x - 16.x

Check for updates to this research when major versions change.

---

## Related Documentation

- **[CONCERNS.md](../codebase/CONCERNS.md)** - Project-specific technical debt and issues
