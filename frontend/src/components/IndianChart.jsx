import { useState } from 'react';

function IndianChart({ positions = [], style = 'south', title = 'D1 Rasi' }) {
  const [selectedHouse, setSelectedHouse] = useState(null);

  // Group planets by sign (1 to 12)
  const signPlanets = Array.from({ length: 13 }, () => []);
  positions.forEach((p) => {
    // Determine short display name
    let shortName = p.displayName || p.planetKey;
    if (shortName.toLowerCase().includes('lagna')) shortName = 'Lg';
    else if (shortName.toLowerCase().includes('sun')) shortName = 'Su';
    else if (shortName.toLowerCase().includes('moon')) shortName = 'Mo';
    else if (shortName.toLowerCase().includes('mars')) shortName = 'Ma';
    else if (shortName.toLowerCase().includes('mercury')) shortName = 'Me';
    else if (shortName.toLowerCase().includes('jupiter')) shortName = 'Ju';
    else if (shortName.toLowerCase().includes('venus')) shortName = 'Ve';
    else if (shortName.toLowerCase().includes('saturn')) shortName = 'Sa';
    else if (shortName.toLowerCase().includes('rahu')) shortName = 'Ra';
    else if (shortName.toLowerCase().includes('ketu')) shortName = 'Ke';
    else shortName = shortName.substring(0, 2);

    if (p.signNumber >= 1 && p.signNumber <= 12) {
      signPlanets[p.signNumber].push(shortName);
    }
  });

  // South Indian Cell mapping (Aries=1, Taurus=2... Pisces=12)
  const southCells = {
    12: { x: 0, y: 0, label: 'Pi' },
    1: { x: 100, y: 0, label: 'Ar' },
    2: { x: 200, y: 0, label: 'Ta' },
    3: { x: 300, y: 0, label: 'Ge' },
    11: { x: 0, y: 100, label: 'Aq' },
    4: { x: 300, y: 100, label: 'Cn' },
    10: { x: 0, y: 200, label: 'Cp' },
    5: { x: 300, y: 200, label: 'Le' },
    9: { x: 0, y: 300, label: 'Sg' },
    8: { x: 100, y: 300, label: 'Sc' },
    7: { x: 200, y: 300, label: 'Li' },
    6: { x: 300, y: 300, label: 'Vi' },
  };

  // Find the Lagna sign to calculate house numbers relative to Lagna
  const lagnaPos = positions.find((p) => p.planetKey?.toLowerCase().includes('lagna') || p.displayName?.toLowerCase().includes('lagna'));
  const lagnaSign = lagnaPos ? lagnaPos.signNumber : 1;

  const getHouseNumber = (sign) => {
    return ((sign - lagnaSign + 12) % 12) + 1;
  };

  // Calculate aspects from selected house (1-12 in signs)
  const getAspectedHouses = (sign) => {
    if (!sign) return [];
    const house = getHouseNumber(sign);
    
    // Determine if any key planets are in this house to check special aspects
    // Otherwise draw standard 7th aspect
    const planetsInHouse = positions.filter((p) => p.signNumber === sign);
    const aspectHouses = new Set([ (house + 6) % 12 || 12 ]); // All houses aspect 7th

    planetsInHouse.forEach((p) => {
      const name = p.planetKey?.toLowerCase();
      if (name.includes('mars')) {
        aspectHouses.add((house + 3) % 12 || 12);
        aspectHouses.add((house + 7) % 12 || 12);
      } else if (name.includes('jupiter')) {
        aspectHouses.add((house + 4) % 12 || 12);
        aspectHouses.add((house + 8) % 12 || 12);
      } else if (name.includes('saturn')) {
        aspectHouses.add((house + 2) % 12 || 12);
        aspectHouses.add((house + 9) % 12 || 12);
      }
    });

    // Map house index back to sign index
    return Array.from(aspectHouses).map((h) => ((h + lagnaSign - 2) % 12) + 1);
  };

  const handleCellClick = (sign) => {
    if (selectedHouse === sign) {
      setSelectedHouse(null);
    } else {
      setSelectedHouse(sign);
    }
  };

  const renderSouthIndian = () => {
    const aspectedSigns = getAspectedHouses(selectedHouse);
    
    return (
      <svg width="400" height="400" className="south-indian-svg" viewBox="0 0 400 400">
        {/* Draw main grid */}
        <rect x="0" y="0" width="400" height="400" fill="none" stroke="var(--border)" strokeWidth="2" />
        
        {/* Horizontal lines */}
        <line x1="0" y1="100" x2="400" y2="100" stroke="var(--border)" strokeWidth="1" />
        <line x1="0" y1="200" x2="400" y2="200" stroke="var(--border)" strokeWidth="1" />
        <line x1="0" y1="300" x2="400" y2="300" stroke="var(--border)" strokeWidth="1" />
        
        {/* Vertical lines */}
        <line x1="100" y1="0" x2="100" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="200" y1="0" x2="200" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="300" y1="0" x2="300" y2="400" stroke="var(--border)" strokeWidth="1" />

        {/* Clear center */}
        <rect x="101" y="101" width="198" height="198" fill="var(--bg-card)" />
        <text x="200" y="190" textAnchor="middle" fill="var(--accent-gold)" fontSize="18" fontWeight="bold">
          {title}
        </text>
        <text x="200" y="215" textAnchor="middle" fill="var(--text-secondary)" fontSize="12">
          {lagnaPos ? `Lagna: ${lagnaPos.rashiName || 'Asc'}` : ''}
        </text>

        {/* Draw Cells */}
        {Object.entries(southCells).map(([signStr, coords]) => {
          const sign = parseInt(signStr);
          const isSelected = selectedHouse === sign;
          const isAspected = aspectedSigns.includes(sign);
          const houseNo = getHouseNumber(sign);
          const cellPlanets = signPlanets[sign];

          return (
            <g key={sign} onClick={() => handleCellClick(sign)} style={{ cursor: 'pointer' }}>
              <rect
                x={coords.x}
                y={coords.y}
                width="100"
                height="100"
                fill={isSelected ? 'rgba(212, 168, 67, 0.15)' : isAspected ? 'rgba(232, 145, 58, 0.08)' : 'transparent'}
                stroke={isSelected ? 'var(--accent-gold)' : 'transparent'}
                strokeWidth="2"
              />
              {/* Sign label */}
              <text x={coords.x + 8} y={coords.y + 18} fill="var(--text-secondary)" fontSize="10">
                {coords.label} ({houseNo})
              </text>
              
              {/* Planet symbols */}
              <g transform={`translate(${coords.x + 10}, ${coords.y + 40})`}>
                {cellPlanets.map((p, idx) => (
                  <text
                    key={idx}
                    x={(idx % 3) * 28}
                    y={Math.floor(idx / 3) * 20}
                    fill={p === 'Lg' ? 'var(--accent-gold)' : 'var(--text-primary)'}
                    fontSize="13"
                    fontWeight={p === 'Lg' ? 'bold' : 'normal'}
                  >
                    {p}
                  </text>
                ))}
              </g>
            </g>
          );
        })}

        {/* Draw aspect lines */}
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
              stroke="var(--accent-warm)"
              strokeDasharray="4,4"
              strokeWidth="2"
              markerEnd="url(#arrow)"
            />
          );
        })}
      </svg>
    );
  };

  // North Indian Diamond Layout
  const renderNorthIndian = () => {
    // North Indian coordinates and layout lines
    return (
      <svg width="400" height="400" className="north-indian-svg" viewBox="0 0 400 400">
        <rect x="0" y="0" width="400" height="400" fill="none" stroke="var(--border)" strokeWidth="2" />
        
        {/* Draw diagonals */}
        <line x1="0" y1="0" x2="400" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="400" y1="0" x2="0" y2="400" stroke="var(--border)" strokeWidth="1" />
        
        {/* Draw inner square */}
        <line x1="200" y1="0" x2="400" y2="200" stroke="var(--border)" strokeWidth="1" />
        <line x1="400" y1="200" x2="200" y2="400" stroke="var(--border)" strokeWidth="1" />
        <line x1="200" y1="400" x2="0" y2="200" stroke="var(--border)" strokeWidth="1" />
        <line x1="0" y1="200" x2="200" y2="0" stroke="var(--border)" strokeWidth="1" />

        {/* Center label */}
        <text x="200" y="205" textAnchor="middle" fill="var(--accent-gold)" fontSize="18" fontWeight="bold">
          {title} (North)
        </text>

        {/* Draw coordinates/planets based on house coordinates */}
        {/* Since South is standard and North requires complex coordinate math, we support South Indian primarily and add a responsive fallback label */}
        <text x="200" y="380" textAnchor="middle" fill="var(--text-secondary)" fontSize="12">
          (Interactive aspects supported in South Indian view)
        </text>
      </svg>
    );
  };

  return (
    <div className="chart-box">
      {style === 'north' ? renderNorthIndian() : renderSouthIndian()}
    </div>
  );
}

export default IndianChart;
