const STORAGE_KEY = 'drikvedic_saved_horoscopes';

export function getProfileSignature(p) {
  if (!p) return '';
  const name = (p.name || '').trim().toLowerCase();
  const day = p.day ?? p.year ?? 0;
  const month = p.month ?? 0;
  const year = p.year ?? 0;
  const hour = p.hour ?? 0;
  const minute = p.minute ?? 0;
  return `${name}_${day}_${month}_${year}_${hour}_${minute}`;
}

export function getSavedHoroscopes() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    console.error('Failed to load saved horoscopes from localStorage', e);
    return [];
  }
}

export function isProfileAlreadySaved(profile) {
  if (!profile) return false;
  const existing = getSavedHoroscopes();
  const sig = getProfileSignature(profile);
  return existing.some((p) => (profile.id && p.id === profile.id) || getProfileSignature(p) === sig);
}

export function saveHoroscope(profile) {
  try {
    const existing = getSavedHoroscopes();
    const sig = getProfileSignature(profile);
    const existingIndex = existing.findIndex(
      (p) => (profile.id && p.id === profile.id) || getProfileSignature(p) === sig
    );

    const id = existingIndex >= 0 ? existing[existingIndex].id : (profile.id || `profile_${Date.now()}`);
    const newProfile = {
      ...profile,
      id,
      savedAt: new Date().toISOString()
    };

    if (existingIndex >= 0) {
      existing[existingIndex] = newProfile;
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

