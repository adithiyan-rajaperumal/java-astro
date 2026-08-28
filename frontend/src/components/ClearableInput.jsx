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
