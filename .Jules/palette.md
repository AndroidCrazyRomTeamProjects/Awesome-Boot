## 2026-03-10 - [Replace hardcoded colors with Semantic Theme attributes in list items]
**Learning:** Hardcoded text colors like #555555 and #999999 can lead to unreadable text on dark modes, causing severe accessibility and readability issues. Using semantic attributes ensures contrast limits are always met regardless of system theme.
**Action:** Use semantic color attributes like ?android:attr/textColorPrimary and ?android:attr/textColorSecondary instead of hardcoded hex values to support dark mode and system theming.

## 2026-03-11 - [Use Standard Icons for Empty/Error States]
**Learning:** Empty states and error containers without visual indicators (icons) can easily be overlooked by users and lack polish. Leveraging native standard drawables tinted with semantic colors provides quick, accessible improvements without ballooning app size.
**Action:** When creating or improving error/empty views, use `ImageView`s with native drawables like `@android:drawable/ic_dialog_alert` and apply `app:tint="?android:attr/textColorSecondary"` for better visual feedback.
