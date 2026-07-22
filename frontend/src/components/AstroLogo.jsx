function AstroLogo({ size = 28, className = "" }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`astro-logo-svg ${className}`}
    >
      <defs>
        <linearGradient id="astroGradient" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
          <stop offset="0%" stopColor="#ff6b00" />
          <stop offset="100%" stopColor="#e85d04" />
        </linearGradient>
        <linearGradient id="goldGradient" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
          <stop offset="0%" stopColor="#ffb703" />
          <stop offset="100%" stopColor="#fb8500" />
        </linearGradient>
      </defs>

      {/* Outer Diamond Kundali Framework */}
      <rect x="8" y="8" width="32" height="32" rx="6" fill="#fff4e5" stroke="url(#astroGradient)" strokeWidth="2.5" />
      <path d="M 24 8 L 40 24 L 24 40 L 8 24 Z" stroke="url(#goldGradient)" strokeWidth="1.5" strokeDasharray="3 3" fill="none" opacity="0.6" />

      {/* Center Celestial Sun & Moon Symbol */}
      <circle cx="24" cy="24" r="7" fill="url(#astroGradient)" />
      
      {/* Crescent Moon overlay */}
      <path
        d="M 27 18 C 23.5 19 21.5 22.5 22.5 26.5 C 23.5 30.5 27 32 30 31 C 25 32 20 28 21 23 C 21.8 19.5 24.5 18.2 27 18 Z"
        fill="#ffffff"
        opacity="0.95"
      />

      {/* Radiant Sun Rays */}
      <line x1="24" y1="11" x2="24" y2="13" stroke="url(#astroGradient)" strokeWidth="2" strokeLinecap="round" />
      <line x1="24" y1="35" x2="24" y2="37" stroke="url(#astroGradient)" strokeWidth="2" strokeLinecap="round" />
      <line x1="11" y1="24" x2="13" y2="24" stroke="url(#astroGradient)" strokeWidth="2" strokeLinecap="round" />
      <line x1="35" y1="24" x2="37" y2="24" stroke="url(#astroGradient)" strokeWidth="2" strokeLinecap="round" />
      <line x1="15" y1="15" x2="16.5" y2="16.5" stroke="url(#astroGradient)" strokeWidth="1.8" strokeLinecap="round" />
      <line x1="31.5" y1="31.5" x2="33" y2="33" stroke="url(#astroGradient)" strokeWidth="1.8" strokeLinecap="round" />
      <line x1="15" y1="33" x2="16.5" y2="31.5" stroke="url(#astroGradient)" strokeWidth="1.8" strokeLinecap="round" />
      <line x1="31.5" y1="16.5" x2="33" y2="15" stroke="url(#astroGradient)" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

export default AstroLogo;
