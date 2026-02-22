import XCTest

final class SnapshotUITests: XCTestCase {

    private let app = XCUIApplication()

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
        setupSnapshot(app)
        app.launch()
    }

    func testScreenshots() {
        snapshot("home")

        // Navigate to Explore
        app.tabBars.buttons["Explore"].tap()
        snapshot("explore")

        // Map
        app.tabBars.buttons["Map"].tap()
        snapshot("map")

        // More
        app.tabBars.buttons["More"].tap()
        snapshot("more")
    }
}
