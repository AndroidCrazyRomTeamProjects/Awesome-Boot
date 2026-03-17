## 2026-03-12 - Empty State Improvements and Accessibility Headings
**Learning:** Using native Android drawables (`@android:drawable/ic_dialog_alert`) combined with semantic tint attributes (`?android:attr/textColorSecondary`) is an effective way to improve empty/error states without adding new assets, while maintaining light/dark theme compatibility. Adding `android:contentDescription="@null"` is crucial for marking these as decorative for screen readers. Furthermore, adding `android:accessibilityHeading="true"` to section titles (like 'Recent Themes') helps screen reader users quickly navigate lists.
**Action:** Use native drawables and theme attributes for simple empty states where appropriate to keep app size down. Always mark decorative images with `@null` content description and ensure section headings are explicitly marked as accessibility headings.
## 2026-03-13 - Adding standard Back Navigation
**Learning:** The application lacked an explicit back mechanism on detail views, relying entirely on the system back button. This can reduce intuitive discoverability for users expecting a visible "up/back" action bar in a detail screen.
**Action:** Applied standard `androidx.appcompat.widget.Toolbar` with `setDisplayHomeAsUpEnabled(true)` to provide a consistent and predictable navigation anchor in detail layouts.
## 2024-05-24 - Layout Cleanups with ViewBinding
**Learning:** The application uses ViewBinding (e.g., `FragmentHomeBinding`). When removing unused views with IDs (like the dummy `@+id/text_dashboard`) from XML layouts to reclaim UI space, it generates a high risk of breaking compilation or causing runtime crashes if the Fragment's Kotlin code still references that view binding property.
**Action:** Always verify that a layout view's ID is not referenced in the corresponding Kotlin Activity/Fragment code before deleting it, even if the view appears to be unused visually.
## 2024-03-24 - Live Regions for Error States
**Learning:** Error states that appear dynamically (like network failures replacing a list) are often missed by screen reader users unless focus is explicitly moved to them.
**Action:** Added `android:accessibilityLiveRegion="polite"` to the error container to ensure screen readers announce the error message automatically when it appears.

## 2025-02-27 - Explicit Empty States for Lists
**Learning:** When a list view (like `themesRecyclerView`) is empty, simply hiding it can lead to a confusing blank screen. Providing an explicit empty state with an icon and clear message (e.g., "No themes found") improves user understanding and confidence that the app is functioning correctly, even when there's no data. Using `android:accessibilityLiveRegion="polite"` on the empty state container ensures screen readers announce its appearance.
**Action:** Always implement explicit empty states for list views, manually toggling them visible when the data list is empty and loading/error states are false. Use native Android drawables and semantic theme attributes for the empty state UI, and ensure accessibility live regions are used for screen reader announcements.
