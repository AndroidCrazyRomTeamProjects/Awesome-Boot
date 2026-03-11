## 2026-03-10 - [Replace hardcoded colors with Semantic Theme attributes in list items]
**Learning:** Hardcoded text colors like #555555 and #999999 can lead to unreadable text on dark modes, causing severe accessibility and readability issues. Using semantic attributes ensures contrast limits are always met regardless of system theme.
**Action:** Use semantic color attributes like ?android:attr/textColorPrimary and ?android:attr/textColorSecondary instead of hardcoded hex values to support dark mode and system theming.

## 2026-03-11 - [Use AppCompatButton and Theme Styles for Buttons]
**Learning:** Standard unstyled `Button` views look inconsistent and unpolished, especially in apps with custom design systems like OneUI. Smaller default buttons also provide poor touch targets.
**Action:** Use `androidx.appcompat.widget.AppCompatButton` with theme-appropriate styles (like `@style/OneUI.ButtonStyleColored`). Add padding to containers and adjust button widths to `match_parent` to improve aesthetics and accessibility via larger touch targets.
