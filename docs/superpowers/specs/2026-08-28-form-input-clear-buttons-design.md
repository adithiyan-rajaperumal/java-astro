# Form Input Clear (✕) Buttons - Design Spec

## Overview
Add inline clear (✕) buttons to form input fields across Horoscope and Matching forms so users can easily erase typed text or selected locations when making corrections.

## Component Design

### 1. `ClearableInput` (`frontend/src/components/ClearableInput.jsx`)
A lightweight wrapper around standard `<input>` elements.
- **Props**:
  - `type`: string (default `'text'`)
  - `value`: string | number
  - `onChange`: function
  - `onClear`: function (optional, defaults to `onChange({ target: { value: '' } })`)
  - `placeholder`: string
  - `required`: boolean
  - `maxLength`: string | number
  - `className`: string
  - `style`: object
  - Additional standard HTML input attributes passed through.
- **Behavior**:
  - Contains an internal `useRef` for the underlying `<input>`.
  - When `value` is non-empty (`value !== ''` and `value != null`), renders `<button type="button" className="input-clear-btn" onClick={handleClear} tabIndex={-1} aria-label="Clear">✕</button>`.
  - `handleClear`: Calls `onClear()`, resets the field, and maintains focus on the input.

### 2. `LocationSearch` Built-in Clear (`frontend/src/components/LocationSearch.jsx`)
- In `.autocomplete-container`, render a clear button when `query` is non-empty.
- **Clear Action**:
  - Sets `query = ''`
  - Sets `suggestions = []`
  - Sets `showDropdown = false`
  - Calls `onChange(null)`
  - Refocuses the search input.

### 3. Styling (`frontend/src/index.css`)
- **`.input-clearable-wrapper`**:
  ```css
  .input-clearable-wrapper {
    position: relative;
    width: 100%;
    display: flex;
    align-items: center;
  }
  .input-clearable-wrapper input {
    padding-right: 32px;
    width: 100%;
  }
  ```
- **`.input-clear-btn`**:
  ```css
  .input-clear-btn {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    font-size: 13px;
    color: var(--text-secondary);
    cursor: pointer;
    padding: 2px 6px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    transition: all 0.15s ease;
  }
  .input-clear-btn:hover {
    color: var(--accent-saffron);
    background-color: rgba(255, 107, 0, 0.1);
  }
  ```

## Integration Scope
- **`frontend/src/components/BirthForm.jsx`**:
  - Name, Birth Date (DD/MM/YYYY), and Birth Time fields use `ClearableInput`.
- **`frontend/src/pages/MatchingPage.jsx`**:
  - Boy Name, Boy Date, Boy Time fields use `ClearableInput`.
  - Girl Name, Girl Date, Girl Time fields use `ClearableInput`.
- **`frontend/src/components/LocationSearch.jsx`**:
  - Integrated clear button for all location autocomplete inputs (Horoscope, Matching, Settings).

## Verification Plan
1. Test clearing Name, Date, and Time inputs in `BirthForm.jsx` (Horoscope page).
2. Test clearing Boy and Girl Name, Date, Time, and Location inputs in `MatchingPage.jsx`.
3. Test clearing Location in `SettingsPage.jsx`.
4. Verify keyboard accessibility: Tab navigation skips the clear icon directly to the next input.
5. Run `npm run build` to ensure clean compilation.
