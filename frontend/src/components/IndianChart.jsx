import { useState } from 'react';
import { t } from '../i18n/translations';

function IndianChart({ positions = [], style = 'south', title = 'D1 Rasi', lang = 'en' }) {
  const [selectedHouse, setSelectedHouse] = useState(null);

  const abbrevMaps = {
    en: { Lagna: 'Lg', Sun: 'Su', Moon: 'Mo', Mars: 'Ma', Mercury: 'Me', Jupiter: 'Ju', Venus: 'Ve', Saturn: 'Sa', Rahu: 'Ra', Ketu: 'Ke' },
    ta: { Lagna: 'ல', Sun: 'சூ', Moon: 'ச', Mars: 'செ', Mercury: 'பு', Jupiter: 'கு', Venus: 'சு', Saturn: 'சனி', Rahu: 'ரா', Ketu: 'கே' },
    hi: { Lagna: 'ल', Sun: 'सू', Moon: 'च', Mars: 'मं', Mercury: 'बु', Jupiter: 'गु', Venus: 'शु', Saturn: 'श', Rahu: 'रा', Ketu: 'के' },
    te: { Lagna: 'ల', Sun: 'సూ', Moon: 'చ', Mars: 'మం', Mercury: 'బు', Jupiter: 'గు', Venus: 'శు', Saturn: 'శ', Rahu: 'రా', Ketu: 'కే' },
    kn: { Lagna: 'ಲ', Sun: 'ಸೂ', Moon: 'ಚ', Mars: 'ಮಂ', Mercury: 'ಬು', Jupiter: 'ಗು', Venus: 'ಶು', Saturn: 'ಶ', Rahu: 'ರಾ', Ketu: 'ಕೇ' },
    ml: { Lagna: 'ല', Sun: 'സൂ', Moon: 'ച', Mars: 'ചൊ', Mercury: 'ബു', Jupiter: 'ഗു', Venus: 'ശു', Saturn: 'ശ', Rahu: 'രാ', Ketu: 'കേ' }
  };

  const dignityLabels = {
    en: { own: 'Own Sign (Aatchi)', exalted: 'Exalted (Uchcham)', debilitated: 'Debilitated (Neecham)', neutral: 'Neutral' },
    ta: { own: 'ஆட்சி (Own)', exalted: 'உச்சம் (Exalted)', debilitated: 'நீசம் (Debilitated)', neutral: 'சமம் (Neutral)' },
    hi: { own: 'स्वक्षेत्री (Own)', exalted: 'उच्च (Exalted)', debilitated: 'नीच (Debilitated)', neutral: 'सम (Neutral)' },
    te: { own: 'స్వక్షేత్రం', exalted: 'ఉచ్ఛ', debilitated: 'నీచ', neutral: 'సమ' },
    kn: { own: 'ಸ್ವಕ್ಷೇತ್ರ', exalted: 'ಉಚ್ಚ', debilitated: 'ನೀಚ', neutral: 'ಸಮ' },
    ml: { own: 'സ്വക്ഷേത്രം', exalted: 'ഉച്ചം', debilitated: 'നീചം', neutral: 'സമം' }
  };

  const signLords = {
    1: 'Mars', 2: 'Venus', 3: 'Mercury', 4: 'Moon', 5: 'Sun', 6: 'Mercury',
    7: 'Venus', 8: 'Mars', 9: 'Jupiter', 10: 'Saturn', 11: 'Saturn', 12: 'Jupiter'
  };

  const activeDignityMap = dignityLabels[lang] || dignityLabels.en;
  const langMap = abbrevMaps[lang] || abbrevMaps.en;
  const activeChartStyle = lang === 'hi' ? 'north' : style;

  // Group planets by sign (1 to 12)
  const signPlanets = Array.from({ length: 13 }, () => []);
  positions.forEach((p) => {
    let key = p.planetKey || p.displayName || '';
    let shortName = '';

    const matchedKey = Object.keys(langMap).find(k => k.toLowerCase() === key.toLowerCase());
    if (matchedKey) {
      shortName = langMap[matchedKey];
    } else {
      shortName = key.substring(0, 2);
    }

    if (p.signNumber >= 1 && p.signNumber <= 12) {
      signPlanets[p.signNumber].push({ ...p, shortName });
    }
  });

  // South Indian Cell mapping (Aries=1, Taurus=2... Pisces=12)
  const southCells = {
    12: { x: 0, y: 0 },
    1: { x: 100, y: 0 },
    2: { x: 200, y: 0 },
    3: { x: 300, y: 0 },
    11: { x: 0, y: 100 },
    4: { x: 300, y: 100 },
    10: { x: 0, y: 200 },
    5: { x: 300, y: 200 },
    9: { x: 0, y: 300 },
    8: { x: 100, y: 300 },
    7: { x: 200, y: 300 },
    6: { x: 300, y: 300 },
  };

  // Find Lagna sign
  const lagnaPos = positions.find((p) => p.planetKey?.toLowerCase().includes('lagna') || p.displayName?.toLowerCase().includes('lagna'));
  const lagnaSign = lagnaPos ? lagnaPos.signNumber : 1;

  const getHouseNumber = (sign) => {
    return ((sign - lagnaSign + 12) % 12) + 1;
  };

  const getDignity = (planetKey, sign) => {
    const pk = (planetKey || '').toUpperCase();
    if (pk === 'SUN') return sign === 5 ? activeDignityMap.own : (sign === 1 ? activeDignityMap.exalted : (sign === 7 ? activeDignityMap.debilitated : activeDignityMap.neutral));
    if (pk === 'MOON') return sign === 4 ? activeDignityMap.own : (sign === 2 ? activeDignityMap.exalted : (sign === 8 ? activeDignityMap.debilitated : activeDignityMap.neutral));
    if (pk === 'MARS') return (sign === 1 || sign === 8) ? activeDignityMap.own : (sign === 10 ? activeDignityMap.exalted : (sign === 4 ? activeDignityMap.debilitated : activeDignityMap.neutral));
    if (pk === 'MERCURY') return (sign === 3 || sign === 6) ? activeDignityMap.own : (sign === 6 ? activeDignityMap.exalted : (sign === 12 ? activeDignityMap.debilitated : activeDignityMap.neutral));
    if (pk === 'JUPITER') return (sign === 9 || sign === 12) ? activeDignityMap.own : (sign === 4 ? activeDignityMap.exalted : (sign === 10 ? activeDignityMap.debilitated : activeDignityMap.neutral));
    if (pk === 'VENUS') return (sign === 2 || sign === 7) ? activeDignityMap.own : (sign === 12 ? activeDignityMap.exalted : (sign === 6 ? activeDignityMap.debilitated : activeDignityMap.neutral));
    if (pk === 'SATURN') return (sign === 10 || sign === 11) ? activeDignityMap.own : (sign === 7 ? activeDignityMap.exalted : (sign === 1 ? activeDignityMap.debilitated : activeDignityMap.neutral));
    return activeDignityMap.neutral;
  };

  // Calculate aspects from selected house
  const getAspectedHouses = (sign) => {
    if (!sign) return [];
    const house = getHouseNumber(sign);
    const planetsInHouse = positions.filter((p) => p.signNumber === sign);
    const aspectHouses = new Set([ (house + 6) % 12 || 12 ]);

    planetsInHouse.forEach((p) => {
      const name = p.planetKey?.toLowerCase();
      if (name?.includes('mars')) {
        aspectHouses.add((house + 3) % 12 || 12);
        aspectHouses.add((house + 7) % 12 || 12);
      } else if (name?.includes('jupiter')) {
        aspectHouses.add((house + 4) % 12 || 12);
        aspectHouses.add((house + 8) % 12 || 12);
      } else if (name?.includes('saturn')) {
        aspectHouses.add((house + 2) % 12 || 12);
        aspectHouses.add((house + 9) % 12 || 12);
      }
    });

    return Array.from(aspectHouses).map((h) => ((h + lagnaSign - 2) % 12) + 1);
  };

  const handleCellClick = (sign) => {
    setSelectedHouse(selectedHouse === sign ? null : sign);
  };

  const selectedPlanets = selectedHouse ? signPlanets[selectedHouse] : [];

  const renderSouthIndian = () => {
    const aspectedSigns = getAspectedHouses(selectedHouse);
    
    return (
      <svg width="400" height="400" className="south-indian-svg" viewBox="0 0 400 400" style={{ backgroundColor: '#ffffff', borderRadius: '8px' }}>
        <rect x="0" y="0" width="400" height="400" fill="#ffffff" stroke="var(--border)" strokeWidth="2" />
        <line x1="0" y1="100" x2="400" y2="100" stroke="var(--border)" strokeWidth="1" />
        <line x1="0" y1="200" x2="400" y2="200" stroke="var(--border)" strokeWidth="1" />
        <line x1="0" y1="300" x2="400" y2="300" stroke="var(--border)" strokeWidth="1" />
        <line x1="100" y1="0" x2="100" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="200" y1="0" x2="200" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="300" y1="0" x2="300" y2="400" stroke="var(--border)" strokeWidth="1" />

        <rect x="101" y="101" width="198" height="198" fill="var(--bg-card)" />
        <text x="200" y="190" textAnchor="middle" fill="var(--accent-gold)" fontSize="18" fontWeight="bold">
          {title}
        </text>
        <text x="200" y="215" textAnchor="middle" fill="var(--text-secondary)" fontSize="12">
          {lagnaPos ? `Lagna: ${lagnaPos.rashiName || 'Asc'}` : ''}
        </text>

        {Object.entries(southCells).map(([signStr, coords]) => {
          const sign = parseInt(signStr);
          const isSelected = selectedHouse === sign;
          const isAspected = aspectedSigns.includes(sign);
          const cellPlanets = signPlanets[sign];

          return (
            <g key={sign} onClick={() => handleCellClick(sign)} style={{ cursor: 'pointer' }}>
              <rect
                x={coords.x}
                y={coords.y}
                width="100"
                height="100"
                fill={isSelected ? 'rgba(255, 107, 0, 0.18)' : isAspected ? 'rgba(232, 93, 4, 0.08)' : '#ffffff'}
                stroke={isSelected ? 'var(--accent-saffron, #ff6b00)' : 'var(--border, #f0e2d0)'}
                strokeWidth={isSelected ? '2' : '1'}
              />
              
              {/* Clean Planet Symbols Only - No house numbers / rashi labels */}
              <g transform={`translate(${coords.x + 12}, ${coords.y + 35})`}>
                {cellPlanets.map((p, idx) => {
                  const isLagna = p.planetKey?.toUpperCase() === 'LAGNA';
                  return (
                    <text
                      key={idx}
                      x={(idx % 3) * 28}
                      y={Math.floor(idx / 3) * 22}
                      fill={isLagna ? 'var(--accent-gold)' : 'var(--text-primary)'}
                      fontSize="14"
                      fontWeight={isLagna ? 'bold' : '600'}
                    >
                      {p.shortName}
                    </text>
                  );
                })}
              </g>
            </g>
          );
        })}

        {selectedHouse && aspectedSigns.map((targetSign, idx) => {
          const start = southCells[selectedHouse];
          const end = southCells[targetSign];
          if (!start || !end) return null;

          return (
            <line
              key={idx}
              x1={start.x + 50}
              y1={start.y + 50}
              x2={end.x + 50}
              y2={end.y + 50}
              stroke="var(--accent-saffron, #ff6b00)"
              strokeDasharray="4,4"
              strokeWidth="2"
            />
          );
        })}
      </svg>
    );
  };

  const renderNorthIndian = () => {
    return (
      <svg width="400" height="400" className="north-indian-svg" viewBox="0 0 400 400" style={{ backgroundColor: '#ffffff', borderRadius: '8px' }}>
        <rect x="0" y="0" width="400" height="400" fill="#ffffff" stroke="var(--border)" strokeWidth="2" />
        <line x1="0" y1="0" x2="400" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="400" y1="0" x2="0" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="200" y1="0" x2="400" y2="200" stroke="var(--border)" strokeWidth="1" />
        <line x1="400" y1="200" x2="200" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="200" y1="400" x2="0" y2="200" stroke="var(--border)" strokeWidth="1" />
        <line x1="0" y1="200" x2="200" y2="0" stroke="var(--border)" strokeWidth="1" />

        <text x="200" y="205" textAnchor="middle" fill="var(--accent-gold)" fontSize="18" fontWeight="bold">
          {title}
        </text>

        {/* North Indian Houses rendering with sign number in center house */}
        <text x="200" y="140" textAnchor="middle" fill="var(--accent-saffron)" fontSize="14" fontWeight="bold">
          1 ({lagnaSign})
        </text>
      </svg>
    );
  };

  return (
    <div className="chart-box-container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <div className="chart-box">
        {activeChartStyle === 'north' ? renderNorthIndian() : renderSouthIndian()}
      </div>

      {/* House detail panel below chart on house click */}
      {selectedHouse && (
        <div className="card" style={{ marginTop: '15px', width: '100%', maxWidth: '400px', padding: '15px' }}>
          <h4 style={{ margin: '0 0 10px', color: 'var(--accent-gold)' }}>
            {t('houseDetails', lang)} ({t('house', lang)} {getHouseNumber(selectedHouse)} — {t('signLord', lang)}: {t('planet.' + (signLords[selectedHouse] || '').toLowerCase(), lang)})
          </h4>
          {selectedPlanets.length === 0 ? (
            <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-secondary)' }}>{t('noPlanetsInHouse', lang)}</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {selectedPlanets.map((p, idx) => {
                const planetKey = (p.planetKey || p.displayName || '').toLowerCase();
                const localizedPlanetName = t('planet.' + planetKey, lang) !== ('planet.' + planetKey) ? t('planet.' + planetKey, lang) : (p.displayName || p.planetKey);
                return (
                  <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', borderBottom: '1px solid var(--border)', paddingBottom: '4px' }}>
                    <span><strong>{localizedPlanetName}:</strong> {p.formattedDegree || `${p.degreeInSign?.toFixed(2)}°`}</span>
                    <span style={{ color: 'var(--accent-saffron)' }}>{getDignity(p.planetKey, selectedHouse)}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default IndianChart;
