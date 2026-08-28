# Form Input Clear Buttons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add inline clear (✕) buttons to form input fields (Name, Date, Time, and Location) across Horoscope and Matching forms to allow users to quickly erase typed text or reset values.

**Architecture:** Create a reusable `ClearableInput` component for text/date/time inputs, add built-in clearing to `LocationSearch`, and style the clear buttons with subtle hover effects and `tabIndex={-1}` for seamless keyboard navigation.

**Tech Stack:** React (JSX), Vanilla CSS

## Global Constraints
- Strictly isolate changes to `frontend/src/components/ClearableInput.jsx` (new), `frontend/src/components/LocationSearch.jsx`, `frontend/src/components/BirthForm.jsx`, `frontend/src/pages/MatchingPage.jsx`, and `frontend/src/index.css`.
- Preserve existing validation, form submission logic, and accessibility.

---

### Task 1: Add Clearable Input Styling to `frontend/src/index.css`

**Files:**
- Modify: `frontend/src/index.css`

**Interfaces:**
- Produces: `.input-clearable-wrapper`, `.input-clear-btn`, and clear button positioning inside `.autocomplete-container`

- [ ] **Step 1: Add CSS rules for clearable input wrappers and clear buttons**

Add to `frontend/src/index.css`:
```css
/* Clearable Input Styling */
.input-clearable-wrapper {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.input-clearable-wrapper input {
  padding-right: 32px !important;
  width: 100%;
  margin-bottom: 0 !important;
}
.input-clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: all 0.15s ease;
  z-index: 2;
}
.input-clear-btn:hover {
  color: var(--accent-saffron);
  background-color: rgba(255, 107, 0, 0.12);
}
.autocomplete-container input {
  padding-right: 32px !important;
}
```

- [ ] **Step 2: Commit CSS changes**

```bash
git add frontend/src/index.css
git commit -m "feat(css): add styles for clearable input wrappers and clear buttons"
```

---

### Task 2: Create `ClearableInput` Component

**Files:**
- Create: `frontend/src/components/ClearableInput.jsx`

**Interfaces:**
- Produces: `ClearableInput` default export component

- [ ] **Step 1: Create `ClearableInput.jsx`**

```jsx
import { useRef } from 'react';

function ClearableInput({
  type = 'text',
  value = '',
  onChange,
  onClear,
  placeholder,
  required = false,
  maxLength,
  style = {},
  className = '',
  ...rest
}) {
  const inputRef = useRef(null);

  const handleClear = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (onClear) {
      onClear();
    } else if (onChange) {
      onChange({ target: { value: '' } });
    }
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  const showClear = value !== '' && value !== null && value !== undefined;

  return (
    <div className="input-clearable-wrapper" style={style}>
      <input
        ref={inputRef}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        maxLength={maxLength}
        className={className}
        {...rest}
      />
      {showClear && (
        <button
          type="button"
          className="input-clear-btn"
          onClick={handleClear}
          tabIndex={-1}
          aria-label="Clear field"
          title="Clear"
        >
          ✕
        </button>
      )}
    </div>
  );
}

export default ClearableInput;
```

- [ ] **Step 2: Commit `ClearableInput.jsx`**

```bash
git add frontend/src/components/ClearableInput.jsx
git commit -m "feat(components): create reusable ClearableInput component"
```

---

### Task 3: Add Clear Button to `LocationSearch.jsx`

**Files:**
- Modify: `frontend/src/components/LocationSearch.jsx`

**Interfaces:**
- Consumes: internal query state and `onChange` callback

- [ ] **Step 1: Update `LocationSearch.jsx` with clear action and button**

Add an input ref and `handleClear` handler:
```jsx
  const inputRef = useRef(null);

  const handleClear = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setQuery('');
    setSuggestions([]);
    setShowDropdown(false);
    if (onChange) {
      onChange(null);
    }
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };
```
Render clear button inside `.autocomplete-container` when `query` is non-empty:
```jsx
      {query && (
        <button
          type="button"
          className="input-clear-btn"
          onClick={handleClear}
          tabIndex={-1}
          aria-label="Clear location"
          title="Clear location"
        >
          ✕
        </button>
      )}
```

- [ ] **Step 2: Commit `LocationSearch.jsx`**

```bash
git add frontend/src/components/LocationSearch.jsx
git commit -m "feat(location): add inline clear button to LocationSearch component"
```

---

### Task 4: Integrate `ClearableInput` in `BirthForm.jsx`

**Files:**
- Modify: `frontend/src/components/BirthForm.jsx`

**Interfaces:**
- Consumes: `ClearableInput` component

- [ ] **Step 1: Replace raw inputs with `ClearableInput` in `BirthForm.jsx`**

Import `ClearableInput` and replace Name, Date, and Time inputs:
- Name input:
  ```jsx
  <ClearableInput
    type="text"
    value={name}
    onChange={(e) => setName(e.target.value)}
    onClear={() => setName('')}
    placeholder="Enter name"
    required
  />
  ```
- Date input:
  ```jsx
  <ClearableInput
    type="text"
    value={dateText}
    onChange={(e) => handleDateChange(e.target.value)}
    onClear={() => setDateText('')}
    placeholder="DD/MM/YYYY"
    maxLength="10"
    required
  />
  ```
- Time input:
  ```jsx
  <ClearableInput
    type="time"
    value={time}
    onChange={(e) => setTime(e.target.value)}
    onClear={() => setTime('')}
    required
  />
  ```

- [ ] **Step 2: Commit `BirthForm.jsx`**

```bash
git add frontend/src/components/BirthForm.jsx
git commit -m "feat(horoscope): integrate ClearableInput into BirthForm"
```

---

### Task 5: Integrate `ClearableInput` in `MatchingPage.jsx`

**Files:**
- Modify: `frontend/src/pages/MatchingPage.jsx`

**Interfaces:**
- Consumes: `ClearableInput` component

- [ ] **Step 1: Replace Boy and Girl inputs with `ClearableInput` in `MatchingPage.jsx`**

Import `ClearableInput` and replace:
- Boy Name: `value={boyName}`, `onChange={(e) => setBoyName(e.target.value)}`, `onClear={() => setBoyName('')}`
- Boy Date: `value={boyDate}`, `onChange={(e) => handleDateChange(e.target.value, setBoyDate)}`, `onClear={() => setBoyDate('')}`
- Boy Time: `value={boyTime}`, `onChange={(e) => setBoyTime(e.target.value)}`, `onClear={() => setBoyTime('')}`
- Girl Name: `value={girlName}`, `onChange={(e) => setGirlName(e.target.value)}`, `onClear={() => setGirlName('')}`
- Girl Date: `value={girlDate}`, `onChange={(e) => handleDateChange(e.target.value, setGirlDate)}`, `onClear={() => setGirlDate('')}`
- Girl Time: `value={girlTime}`, `onChange={(e) => setGirlTime(e.target.value)}`, `onClear={() => setGirlTime('')}`

- [ ] **Step 2: Commit `MatchingPage.jsx`**

```bash
git add frontend/src/pages/MatchingPage.jsx
git commit -m "feat(matching): integrate ClearableInput into Boy and Girl cards"
```

---

### Task 6: Build Verification & Regression Testing

**Files:**
- Test: Build frontend using `npm run build`

- [ ] **Step 1: Run frontend build**

Run: `cd frontend; npm run build`
Expected: Build passes with 0 errors.

- [ ] **Step 2: Check git diff**

Run: `git diff HEAD~5 --stat`
Expected: Only specified files modified/created.
