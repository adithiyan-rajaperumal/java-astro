const STORAGE_KEY = 'drikvedic_saved_horoscopes';

export function getSavedHoroscopes() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    console.error('Failed to load saved horoscopes from localStorage', e);
    return [];
  }
}

export function saveHoroscope(profile) {
  try {
    const existing = getSavedHoroscopes();
    const id = profile.id || `profile_${Date.now()}`;
    const newProfile = {
      ...profile,
      id,
      savedAt: new Date().toISOString()
    };
    const index = existing.findIndex((p) => p.id === id);
    if (index >= 0) {
      existing[index] = newProfile;
    } else {
      existing.unshift(newProfile);
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(existing));
    return existing;
  } catch (e) {
    console.error('Failed to save horoscope profile', e);
    return getSavedHoroscopes();
  }
}

export function deleteSavedHoroscope(id) {
  try {
    const existing = getSavedHoroscopes();
    const filtered = existing.filter((p) => p.id !== id);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(filtered));
    return filtered;
  } catch (e) {
    console.error('Failed to delete horoscope profile', e);
    return getSavedHoroscopes();
  }
}
