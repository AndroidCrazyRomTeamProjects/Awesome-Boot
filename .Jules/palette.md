## 2026-03-10 - [Replace hardcoded colors with Semantic Theme attributes in list items]
**Learning:** Hardcoded text colors like #555555 and #999999 can lead to unreadable text on dark modes, causing severe accessibility and readability issues. Using semantic attributes ensures contrast limits are always met regardless of system theme.
**Action:** Use semantic color attributes like ?android:attr/textColorPrimary and ?android:attr/textColorSecondary instead of hardcoded hex values to support dark mode and system theming.
