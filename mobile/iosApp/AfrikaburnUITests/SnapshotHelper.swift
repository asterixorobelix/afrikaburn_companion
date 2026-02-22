import XCTest

// NOTE: This file is required by fastlane snapshot.
// It is copied from fastlane's template and slightly trimmed for CI use.

private let SnapshotTimeout: TimeInterval = 20

func setupSnapshot(_ app: XCUIApplication, waitForAnimations: Bool = true) {
    let args = ProcessInfo.processInfo.arguments
    if args.contains("-FASTLANE_SNAPSHOT") {
        app.launchArguments.append("-FASTLANE_SNAPSHOT")
    }

    // Disable animations for consistent screenshots
    if waitForAnimations {
        app.launchArguments.append("-UIViewAnimationSuppressesAnimations")
        app.launchArguments.append("YES")
    }
}

func snapshot(_ name: String, timeWaitingForIdle: TimeInterval = 1) {
    let expectation = XCTNSNotificationExpectation(name: XCTNSNotification.Name(rawValue: "fastlaneSnapshot"), object: nil)
    let userInfo = ["name": name]
    NotificationCenter.default.post(name: Notification.Name(rawValue: "fastlaneSnapshot"), object: nil, userInfo: userInfo)
    _ = XCTWaiter.wait(for: [expectation], timeout: SnapshotTimeout)
    Thread.sleep(forTimeInterval: timeWaitingForIdle)
}
