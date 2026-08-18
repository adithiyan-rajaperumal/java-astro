import { useState, useEffect, useRef } from 'react';

function LocationSearch({ value, onChange, placeholder = 'Search location...' }) {
  const [query, setQuery] = useState(value?.label || '');
  const [suggestions, setSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (value) {
      setQuery(value.label);
    } else {
      setQuery('');
    }
  }, [value]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('touchstart', handleClickOutside, { passive: true });
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('touchstart', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    if (!query.trim()) {
      setSuggestions([]);
      setShowDropdown(false);
      return;
    }

    if (value && query === value.label) {
      return;
    }

    const handler = setTimeout(async () => {
      try {
        const response = await fetch(`/api/v1/locations/autocomplete?query=${encodeURIComponent(query)}`);
        if (response.ok) {
          const data = await response.json();
          setSuggestions(data);
          setShowDropdown(true);
        }
      } catch (error) {
        console.error('Failed to fetch location suggestions', error);
      }
    }, 300);

    return () => clearTimeout(handler);
  }, [query, value]);

  const handleInputChange = (e) => {
    setQuery(e.target.value);
  };

  const handleSelect = (item) => {
    setQuery(item.label);
    setShowDropdown(false);
    if (onChange) {
      onChange(item);
    }
  };

  return (
    <div className="autocomplete-container" ref={dropdownRef}>
      <input
        type="text"
        value={query}
        onChange={handleInputChange}
        onFocus={() => query.trim() && setShowDropdown(true)}
        placeholder={placeholder}
      />
      {showDropdown && (
        <div className="autocomplete-dropdown">
          {suggestions.length > 0 ? (
            suggestions.map((item, idx) => (
              <div
                key={idx}
                className="autocomplete-item"
                onMouseDown={(e) => {
                  e.preventDefault();
                  handleSelect(item);
                }}
                onTouchEnd={(e) => {
                  e.preventDefault();
                  handleSelect(item);
                }}
                onClick={() => handleSelect(item)}
              >
                {item.label}
              </div>
            ))
          ) : (
            <div className="autocomplete-item" style={{ color: 'var(--text-secondary)', cursor: 'default', fontSize: '13px' }}>
              No places found. Try typing city or state name.
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default LocationSearch;
