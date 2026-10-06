import subprocess
import os

os.makedirs("docs/screenshots", exist_ok=True)

svg_main = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1440" width="720" height="1440">
  <defs>
    <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0E1017"/>
      <stop offset="100%" stop-color="#151722"/>
    </linearGradient>
    <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#00E5FF"/>
      <stop offset="100%" stop-color="#00B0FF"/>
    </linearGradient>
    <linearGradient id="cardGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#1C1F2C"/>
      <stop offset="100%" stop-color="#181A24"/>
    </linearGradient>
    <linearGradient id="activeCardGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#1C2836"/>
      <stop offset="100%" stop-color="#16202C"/>
    </linearGradient>
    <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="6" result="blur"/>
      <feComposite in="SourceGraphic" in2="blur" operator="over"/>
    </filter>
  </defs>

  <!-- Background -->
  <rect width="720" height="1440" fill="url(#bgGrad)"/>

  <!-- Status Bar -->
  <rect width="720" height="60" fill="#0E1017"/>
  <text x="40" y="42" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="22" font-weight="600">09:41</text>
  <g transform="translate(620, 26)">
    <path d="M0,16 L4,16 L4,20 L0,20 Z M6,12 L10,12 L10,20 L6,20 Z M12,8 L16,8 L16,20 L12,20 Z M18,4 L22,4 L22,20 L18,20 Z" fill="#FFFFFF"/>
    <path d="M32,4 C38,4 43,8 43,14 C43,20 38,24 32,24 C26,24 21,20 21,14 C21,8 26,4 32,4 Z" fill="#FFFFFF" opacity="0.3"/>
    <rect x="48" y="6" width="30" height="15" rx="3" fill="none" stroke="#FFFFFF" stroke-width="2"/>
    <rect x="51" y="9" width="20" height="9" rx="1.5" fill="#00E676"/>
  </g>

  <!-- Top App Bar -->
  <rect y="60" width="720" height="110" fill="#141722"/>
  <text x="40" y="132" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="34" font-weight="800" letter-spacing="-0.5">3x manager</text>

  <!-- Top Bar Actions (Right side) -->
  <!-- 1. Open Tabs Button with Badge -->
  <g transform="translate(370, 92)">
    <rect width="52" height="52" rx="14" fill="#1E2230"/>
    <path d="M16,18 L36,18 L36,22 L16,22 Z M16,26 L36,26 L36,30 L16,30 Z M16,34 L36,34 L36,38 L16,38 Z" fill="#00E5FF"/>
    <!-- Badge -->
    <circle cx="44" cy="10" r="12" fill="#00E5FF"/>
    <text x="44" y="15" fill="#000000" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">2</text>
  </g>

  <!-- 2. Tunnel Stop Button -->
  <g transform="translate(435, 92)">
    <rect width="105" height="52" rx="14" fill="#FF5252" fill-opacity="0.15" stroke="#FF5252" stroke-opacity="0.4" stroke-width="1.5"/>
    <circle cx="452" cy="118" r="4" fill="#00E676"/>
    <text x="495" y="124" fill="#FF5252" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">СТОП</text>
  </g>

  <!-- 3. Update Badge (Left of Eye) -->
  <g transform="translate(552, 92)">
    <rect width="96" height="52" rx="14" fill="#00E5FF" fill-opacity="0.18" stroke="#00E5FF" stroke-opacity="0.6" stroke-width="1.5"/>
    <text x="600" y="124" fill="#00E5FF" font-family="sans-serif" font-size="17" font-weight="bold" text-anchor="middle">☁ v1.1.0</text>
  </g>

  <!-- 4. Eye Mask Icon -->
  <g transform="translate(658, 92)">
    <circle cx="26" cy="26" r="24" fill="#1E2230"/>
    <path d="M16,26 C19,20 33,20 36,26 C33,32 19,32 16,26 Z" fill="none" stroke="#00E5FF" stroke-width="2.5"/>
    <circle cx="26" cy="26" r="4" fill="#00E5FF"/>
  </g>

  <!-- Active Tunnel Banner -->
  <g transform="translate(40, 195)">
    <rect width="640" height="96" rx="20" fill="url(#activeCardGrad)" stroke="#00E676" stroke-opacity="0.4" stroke-width="1.5"/>
    <circle cx="45" cy="48" r="14" fill="#00E676" fill-opacity="0.2"/>
    <circle cx="45" cy="48" r="6" fill="#00E676" filter="url(#glow)"/>
    <text x="75" y="42" fill="#FFFFFF" font-family="sans-serif" font-size="22" font-weight="bold">Туннель активен ➔ Frankfurt Node-01</text>
    <text x="75" y="72" fill="#00E676" font-family="monospace" font-size="18">127.0.0.1:2053 ➔ SSH: 203.0.113.***:22</text>
  </g>

  <!-- Search & Filter Bar -->
  <g transform="translate(40, 315)">
    <rect width="520" height="64" rx="16" fill="#181B26" stroke="#262A3B" stroke-width="1"/>
    <text x="65" y="356" fill="#6B7280" font-family="sans-serif" font-size="20">Поиск серверов по имени или IP...</text>
    <circle cx="40" cy="32" r="8" fill="none" stroke="#6B7280" stroke-width="2"/>
    <line x1="46" y1="38" x2="52" y2="44" stroke="#6B7280" stroke-width="2"/>
  </g>
  <g transform="translate(580, 315)">
    <rect width="100" height="64" rx="16" fill="#181B26" stroke="#262A3B" stroke-width="1"/>
    <text x="630" y="355" fill="#00E5FF" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">Сорт. ▾</text>
  </g>

  <!-- Server Card 1 (Active) -->
  <g transform="translate(40, 405)">
    <rect width="640" height="210" rx="24" fill="url(#cardGrad)" stroke="#00E5FF" stroke-opacity="0.5" stroke-width="1.8"/>
    <!-- Status Dot -->
    <circle cx="36" cy="42" r="10" fill="#00E676" fill-opacity="0.25"/>
    <circle cx="36" cy="42" r="5" fill="#00E676" filter="url(#glow)"/>
    <text x="58" y="48" fill="#FFFFFF" font-family="sans-serif" font-size="26" font-weight="bold">🇩🇪 Frankfurt Node-01 (Core)</text>
    
    <!-- Badges -->
    <rect x="58" y="68" width="130" height="32" rx="8" fill="#00E5FF" fill-opacity="0.15"/>
    <text x="123" y="90" fill="#00E5FF" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">SSH Ключ RSA</text>

    <rect x="200" y="68" width="110" height="32" rx="8" fill="#00E676" fill-opacity="0.15"/>
    <text x="255" y="90" fill="#00E676" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">Пинг 24 ms</text>

    <!-- Details -->
    <text x="36" y="136" fill="#9CA3AF" font-family="monospace" font-size="18">Хост: <tspan fill="#FFFFFF">203.0.113.***:22</tspan></text>
    <text x="36" y="166" fill="#9CA3AF" font-family="monospace" font-size="18">Панель: <tspan fill="#00E5FF">https://127.0.0.1:2053/xui/</tspan></text>

    <!-- Action Buttons -->
    <g transform="translate(460, 125)">
      <rect width="150" height="60" rx="16" fill="url(#cyanGrad)"/>
      <text x="535" y="162" fill="#000000" font-family="sans-serif" font-size="20" font-weight="800" text-anchor="middle">ОТКРЫТЬ ➔</text>
    </g>
  </g>

  <!-- Server Card 2 -->
  <g transform="translate(40, 640)">
    <rect width="640" height="210" rx="24" fill="url(#cardGrad)" stroke="#262A3B" stroke-width="1.2"/>
    <circle cx="36" cy="42" r="5" fill="#6B7280"/>
    <text x="58" y="48" fill="#FFFFFF" font-family="sans-serif" font-size="26" font-weight="bold">🇳🇱 Amsterdam Edge-02</text>
    
    <rect x="58" y="68" width="120" height="32" rx="8" fill="#9CA3AF" fill-opacity="0.15"/>
    <text x="118" y="90" fill="#9CA3AF" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">Пароль SSH</text>

    <rect x="190" y="68" width="110" height="32" rx="8" fill="#00E676" fill-opacity="0.15"/>
    <text x="245" y="90" fill="#00E676" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">Пинг 31 ms</text>

    <text x="36" y="136" fill="#9CA3AF" font-family="monospace" font-size="18">Хост: <tspan fill="#FFFFFF">198.51.100.***:2222</tspan></text>
    <text x="36" y="166" fill="#9CA3AF" font-family="monospace" font-size="18">Панель: <tspan fill="#00E5FF">https://127.0.0.1:2087/panel/</tspan></text>

    <g transform="translate(460, 125)">
      <rect width="150" height="60" rx="16" fill="#222738" stroke="#00E5FF" stroke-width="1.5"/>
      <text x="535" y="162" fill="#00E5FF" font-family="sans-serif" font-size="20" font-weight="bold" text-anchor="middle">Открыть</text>
    </g>
  </g>

  <!-- Server Card 3 -->
  <g transform="translate(40, 875)">
    <rect width="640" height="210" rx="24" fill="url(#cardGrad)" stroke="#262A3B" stroke-width="1.2"/>
    <circle cx="36" cy="42" r="5" fill="#6B7280"/>
    <text x="58" y="48" fill="#FFFFFF" font-family="sans-serif" font-size="26" font-weight="bold">🇯🇵 Tokyo HighSpeed Relay</text>
    
    <rect x="58" y="68" width="145" height="32" rx="8" fill="#00E5FF" fill-opacity="0.15"/>
    <text x="130" y="90" fill="#00E5FF" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">Ключ ED25519</text>

    <rect x="215" y="68" width="110" height="32" rx="8" fill="#00E676" fill-opacity="0.15"/>
    <text x="270" y="90" fill="#00E676" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">Пинг 115 ms</text>

    <text x="36" y="136" fill="#9CA3AF" font-family="monospace" font-size="18">Хост: <tspan fill="#FFFFFF">192.0.2.***:22</tspan></text>
    <text x="36" y="166" fill="#9CA3AF" font-family="monospace" font-size="18">Панель: <tspan fill="#00E5FF">https://127.0.0.1:2096/admin/</tspan></text>

    <g transform="translate(460, 125)">
      <rect width="150" height="60" rx="16" fill="#222738" stroke="#00E5FF" stroke-width="1.5"/>
      <text x="535" y="162" fill="#00E5FF" font-family="sans-serif" font-size="20" font-weight="bold" text-anchor="middle">Открыть</text>
    </g>
  </g>

  <!-- FAB: Add Server -->
  <g transform="translate(520, 1260)">
    <circle cx="70" cy="70" r="60" fill="url(#cyanGrad)" filter="url(#glow)"/>
    <path d="M50,70 L90,70 M70,50 L70,90" stroke="#000000" stroke-width="6" stroke-linecap="round"/>
  </g>
</svg>"""

with open("docs/screenshots/screen_main.svg", "w") as f:
    f.write(svg_main)

subprocess.run(["convert", "-density", "150", "docs/screenshots/screen_main.svg", "docs/screenshots/screen_main.png"])
print("Generated screen_main.png")
