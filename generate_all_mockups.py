import os
import zlib
import struct

os.makedirs("docs/screenshots", exist_ok=True)

# 1. screen_main.svg
svg_main = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1440" width="720" height="1440">
  <defs>
    <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0F111A"/>
      <stop offset="100%" stop-color="#141724"/>
    </linearGradient>
    <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#00E5FF"/>
      <stop offset="100%" stop-color="#00B0FF"/>
    </linearGradient>
    <linearGradient id="cardGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#1B1E2C"/>
      <stop offset="100%" stop-color="#161824"/>
    </linearGradient>
  </defs>

  <rect width="720" height="1440" fill="url(#bgGrad)"/>

  <!-- Status Bar -->
  <rect width="720" height="56" fill="#0F111A"/>
  <text x="40" y="38" fill="#FFFFFF" font-family="-apple-system, sans-serif" font-size="20" font-weight="600">09:41</text>
  <circle cx="640" cy="32" r="6" fill="#00E676"/>
  <rect x="655" y="24" width="24" height="14" rx="3" fill="none" stroke="#FFFFFF" stroke-width="2"/>
  <rect x="658" y="27" width="16" height="8" rx="1.5" fill="#00E676"/>

  <!-- Top App Bar -->
  <rect y="56" width="720" height="100" fill="#141724"/>
  <text x="36" y="122" fill="#FFFFFF" font-family="-apple-system, sans-serif" font-size="32" font-weight="bold" letter-spacing="-0.5">3x manager</text>

  <!-- Open Tabs Button -->
  <g transform="translate(365, 82)">
    <rect width="48" height="48" rx="12" fill="#1E2234"/>
    <path d="M14,16 L34,16 M14,24 L34,24 M14,32 L34,32" stroke="#00E5FF" stroke-width="2.5" stroke-linecap="round"/>
    <circle cx="40" cy="8" r="10" fill="#00E5FF"/>
    <text x="40" y="13" fill="#000000" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">2</text>
  </g>

  <!-- Tunnel Stop Button -->
  <g transform="translate(425, 82)">
    <rect width="90" height="48" rx="12" fill="#FF5252" fill-opacity="0.15" stroke="#FF5252" stroke-opacity="0.5" stroke-width="1.5"/>
    <circle cx="440" cy="106" r="4" fill="#00E676"/>
    <text x="475" y="112" fill="#FF5252" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">СТОП</text>
  </g>

  <!-- Update Badge (Left of Eye) -->
  <g transform="translate(525, 82)">
    <rect width="90" height="48" rx="12" fill="#00E5FF" fill-opacity="0.18" stroke="#00E5FF" stroke-opacity="0.6" stroke-width="1.5"/>
    <text x="570" y="112" fill="#00E5FF" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">☁ v1.1.0</text>
  </g>

  <!-- Eye Mask Icon -->
  <g transform="translate(625, 82)">
    <circle cx="24" cy="24" r="24" fill="#1E2234"/>
    <path d="M14,24 C17,18 31,18 34,24 C31,30 17,30 14,24 Z" fill="none" stroke="#00E5FF" stroke-width="2"/>
    <circle cx="24" cy="24" r="3.5" fill="#00E5FF"/>
  </g>

  <!-- Settings Gear Icon -->
  <g transform="translate(675, 82)">
    <circle cx="20" cy="24" r="20" fill="#1E2234"/>
    <circle cx="20" cy="24" r="7" fill="none" stroke="#9CA3AF" stroke-width="2.5"/>
  </g>

  <!-- Active Tunnel Banner -->
  <g transform="translate(36, 175)">
    <rect width="648" height="88" rx="18" fill="#16232F" stroke="#00E676" stroke-opacity="0.45" stroke-width="1.5"/>
    <circle cx="40" cy="44" r="6" fill="#00E676"/>
    <text x="68" y="38" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="bold">Туннель активен ➔ Frankfurt Node-01</text>
    <text x="68" y="66" fill="#00E676" font-family="monospace" font-size="16">127.0.0.1:2053 ➔ SSH: 203.0.113.***:22</text>
  </g>

  <!-- Search & Sort -->
  <g transform="translate(36, 280)">
    <rect width="520" height="56" rx="14" fill="#181B26" stroke="#262A3B" stroke-width="1"/>
    <text x="56" y="316" fill="#6B7280" font-family="sans-serif" font-size="18">Поиск по названию или IP...</text>
  </g>
  <g transform="translate(568, 280)">
    <rect width="116" height="56" rx="14" fill="#181B26" stroke="#262A3B" stroke-width="1"/>
    <text x="626" y="316" fill="#00E5FF" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">Сортировка ▾</text>
  </g>

  <!-- Server Card 1 (Active) -->
  <g transform="translate(36, 360)">
    <rect width="648" height="210" rx="22" fill="url(#cardGrad)" stroke="#00E5FF" stroke-opacity="0.6" stroke-width="1.8"/>
    <circle cx="36" cy="40" r="6" fill="#00E676"/>
    <text x="56" y="46" fill="#FFFFFF" font-family="sans-serif" font-size="24" font-weight="bold">🇩🇪 Frankfurt Node-01 (Core)</text>
    
    <rect x="56" y="64" width="125" height="30" rx="8" fill="#00E5FF" fill-opacity="0.15"/>
    <text x="118" y="84" fill="#00E5FF" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">SSH Ключ RSA</text>

    <rect x="190" y="64" width="100" height="30" rx="8" fill="#00E676" fill-opacity="0.15"/>
    <text x="240" y="84" fill="#00E676" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">24 ms</text>

    <text x="36" y="132" fill="#9CA3AF" font-family="monospace" font-size="17">Хост: <tspan fill="#FFFFFF">203.0.113.***:22</tspan></text>
    <text x="36" y="162" fill="#9CA3AF" font-family="monospace" font-size="17">Панель: <tspan fill="#00E5FF">https://127.0.0.1:2053/xui/</tspan></text>

    <g transform="translate(470, 125)">
      <rect width="145" height="56" rx="14" fill="url(#cyanGrad)"/>
      <text x="542" y="160" fill="#000000" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">ОТКРЫТЬ ➔</text>
    </g>
  </g>

  <!-- Server Card 2 -->
  <g transform="translate(36, 595)">
    <rect width="648" height="210" rx="22" fill="url(#cardGrad)" stroke="#262A3B" stroke-width="1.2"/>
    <circle cx="36" cy="40" r="5" fill="#6B7280"/>
    <text x="56" y="46" fill="#FFFFFF" font-family="sans-serif" font-size="24" font-weight="bold">🇳🇱 Amsterdam Edge-02</text>
    
    <rect x="56" y="64" width="115" height="30" rx="8" fill="#9CA3AF" fill-opacity="0.15"/>
    <text x="113" y="84" fill="#9CA3AF" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">Пароль SSH</text>

    <rect x="180" y="64" width="100" height="30" rx="8" fill="#00E676" fill-opacity="0.15"/>
    <text x="230" y="84" fill="#00E676" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">31 ms</text>

    <text x="36" y="132" fill="#9CA3AF" font-family="monospace" font-size="17">Хост: <tspan fill="#FFFFFF">198.51.100.***:2222</tspan></text>
    <text x="36" y="162" fill="#9CA3AF" font-family="monospace" font-size="17">Панель: <tspan fill="#00E5FF">https://127.0.0.1:2087/panel/</tspan></text>

    <g transform="translate(470, 125)">
      <rect width="145" height="56" rx="14" fill="#222738" stroke="#00E5FF" stroke-width="1.5"/>
      <text x="542" y="160" fill="#00E5FF" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">Открыть</text>
    </g>
  </g>

  <!-- Server Card 3 -->
  <g transform="translate(36, 830)">
    <rect width="648" height="210" rx="22" fill="url(#cardGrad)" stroke="#262A3B" stroke-width="1.2"/>
    <circle cx="36" cy="40" r="5" fill="#6B7280"/>
    <text x="56" y="46" fill="#FFFFFF" font-family="sans-serif" font-size="24" font-weight="bold">🇯🇵 Tokyo HighSpeed Relay</text>
    
    <rect x="56" y="64" width="135" height="30" rx="8" fill="#00E5FF" fill-opacity="0.15"/>
    <text x="123" y="84" fill="#00E5FF" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">Ключ ED25519</text>

    <rect x="200" y="64" width="100" height="30" rx="8" fill="#00E676" fill-opacity="0.15"/>
    <text x="250" y="84" fill="#00E676" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">115 ms</text>

    <text x="36" y="132" fill="#9CA3AF" font-family="monospace" font-size="17">Хост: <tspan fill="#FFFFFF">192.0.2.***:22</tspan></text>
    <text x="36" y="162" fill="#9CA3AF" font-family="monospace" font-size="17">Панель: <tspan fill="#00E5FF">https://127.0.0.1:2096/admin/</tspan></text>

    <g transform="translate(470, 125)">
      <rect width="145" height="56" rx="14" fill="#222738" stroke="#00E5FF" stroke-width="1.5"/>
      <text x="542" y="160" fill="#00E5FF" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">Открыть</text>
    </g>
  </g>

  <!-- FAB -->
  <g transform="translate(530, 1280)">
    <circle cx="65" cy="65" r="55" fill="url(#cyanGrad)"/>
    <path d="M45,65 L85,65 M65,45 L65,85" stroke="#000000" stroke-width="5.5" stroke-linecap="round"/>
  </g>
</svg>"""

# 2. screen_webpanel.svg
svg_webpanel = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1440" width="720" height="1440">
  <defs>
    <linearGradient id="panelBg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0E1017"/>
      <stop offset="100%" stop-color="#141724"/>
    </linearGradient>
    <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#00E5FF"/>
      <stop offset="100%" stop-color="#00B0FF"/>
    </linearGradient>
  </defs>

  <rect width="720" height="1440" fill="url(#panelBg)"/>

  <!-- Status Bar -->
  <rect width="720" height="56" fill="#0E1017"/>
  <text x="40" y="38" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="600">09:41</text>
  <rect x="655" y="24" width="24" height="14" rx="3" fill="none" stroke="#FFFFFF" stroke-width="2"/>
  <rect x="658" y="27" width="16" height="8" rx="1.5" fill="#00E676"/>

  <!-- WebPanel Header -->
  <rect y="56" width="720" height="80" fill="#181B28"/>
  <path d="M40,96 L58,78 M40,96 L58,114" stroke="#FFFFFF" stroke-width="3" stroke-linecap="round"/>
  <text x="80" y="94" fill="#FFFFFF" font-family="sans-serif" font-size="22" font-weight="bold">🇩🇪 Frankfurt Node-01</text>
  <circle cx="85" cy="114" r="4" fill="#00E676"/>
  <text x="96" y="118" fill="#00E676" font-family="monospace" font-size="14">Туннель ➔ Frankfurt</text>

  <!-- Right Actions: Tabs, PC Mode, Reload -->
  <g transform="translate(480, 72)">
    <rect width="44" height="44" rx="10" fill="#222638"/>
    <path d="M12,14 L32,14 M12,22 L32,22 M12,30 L32,30" stroke="#00E5FF" stroke-width="2"/>
    <circle cx="36" cy="8" r="8" fill="#00E5FF"/>
    <text x="36" y="12" fill="#000000" font-family="sans-serif" font-size="10" font-weight="bold" text-anchor="middle">2</text>
  </g>
  <g transform="translate(540, 72)">
    <rect width="95" height="44" rx="10" fill="#00E5FF" fill-opacity="0.2" stroke="#00E5FF" stroke-width="1.5"/>
    <text x="587" y="99" fill="#00E5FF" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">🖥 ПК 1280</text>
  </g>
  <g transform="translate(648, 72)">
    <rect width="44" height="44" rx="10" fill="#222638"/>
    <path d="M22,14 A10,10 0 1,1 14,22" fill="none" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
  </g>

  <!-- 3x-ui Embedded Desktop Interface -->
  <!-- Top 3x-ui navbar -->
  <g transform="translate(0, 136)">
    <rect width="720" height="50" fill="#001529"/>
    <text x="24" y="168" fill="#00E5FF" font-family="sans-serif" font-size="20" font-weight="bold">3X-UI DASHBOARD</text>
    <text x="600" y="168" fill="#FFFFFF" font-family="sans-serif" font-size="16">👤 admin ▾</text>
  </g>

  <!-- Metrics Grid -->
  <g transform="translate(24, 210)">
    <!-- CPU Card -->
    <rect width="325" height="150" rx="16" fill="#1C1F2E" stroke="#2B2F44" stroke-width="1"/>
    <text x="20" y="38" fill="#9CA3AF" font-family="sans-serif" font-size="16">Нагрузка ЦП</text>
    <text x="20" y="80" fill="#00E676" font-family="sans-serif" font-size="36" font-weight="bold">14.2%</text>
    <rect x="20" y="105" width="285" height="12" rx="6" fill="#2B2F44"/>
    <rect x="20" y="105" width="42" height="12" rx="6" fill="#00E676"/>

    <!-- RAM Card -->
    <rect x="345" width="325" height="150" rx="16" fill="#1C1F2E" stroke="#2B2F44" stroke-width="1"/>
    <text x="365" y="38" fill="#9CA3AF" font-family="sans-serif" font-size="16">ОЗУ (RAM)</text>
    <text x="365" y="80" fill="#00E5FF" font-family="sans-serif" font-size="34" font-weight="bold">1.2 / 4 GB</text>
    <rect x="365" y="105" width="285" height="12" rx="6" fill="#2B2F44"/>
    <rect x="365" y="105" width="85" height="12" rx="6" fill="#00E5FF"/>
  </g>

  <!-- Traffic Stats -->
  <g transform="translate(24, 385)">
    <!-- Download -->
    <rect width="325" height="130" rx="16" fill="#1C1F2E" stroke="#2B2F44" stroke-width="1"/>
    <text x="20" y="36" fill="#9CA3AF" font-family="sans-serif" font-size="16">Входящий трафик (↓)</text>
    <text x="20" y="78" fill="#FFFFFF" font-family="sans-serif" font-size="30" font-weight="bold">1.48 TB</text>
    <text x="20" y="108" fill="#00E676" font-family="sans-serif" font-size="14">Скорость: 12.4 MB/s</text>

    <!-- Upload -->
    <rect x="345" width="325" height="130" rx="16" fill="#1C1F2E" stroke="#2B2F44" stroke-width="1"/>
    <text x="365" y="36" fill="#9CA3AF" font-family="sans-serif" font-size="16">Исходящий трафик (↑)</text>
    <text x="365" y="78" fill="#FFFFFF" font-family="sans-serif" font-size="30" font-weight="bold">2.12 TB</text>
    <text x="365" y="108" fill="#00E5FF" font-family="sans-serif" font-size="14">Скорость: 18.1 MB/s</text>
  </g>

  <!-- Inbounds Table Section -->
  <g transform="translate(24, 540)">
    <rect width="670" height="580" rx="18" fill="#181B28" stroke="#2B2F44" stroke-width="1.2"/>
    <text x="24" y="44" fill="#FFFFFF" font-family="sans-serif" font-size="22" font-weight="bold">Список подключений (Inbounds)</text>
    <rect x="520" y="18" width="125" height="38" rx="10" fill="#00E5FF"/>
    <text x="582" y="42" fill="#000000" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">+ Добавить</text>

    <!-- Table Header -->
    <rect y="70" width="670" height="42" fill="#202436"/>
    <text x="24" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="15">Протокол</text>
    <text x="170" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="15">Порт</text>
    <text x="270" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="15">Трафик</text>
    <text x="440" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="15">Статус</text>
    <text x="560" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="15">Действия</text>

    <!-- Row 1: VLESS Reality -->
    <g transform="translate(0, 120)">
      <text x="24" y="30" fill="#00E5FF" font-family="sans-serif" font-size="17" font-weight="bold">VLESS Reality</text>
      <text x="24" y="50" fill="#6B7280" font-family="sans-serif" font-size="13">Vision TCP</text>
      <text x="170" y="40" fill="#FFFFFF" font-family="monospace" font-size="16">443</text>
      <text x="270" y="40" fill="#FFFFFF" font-family="sans-serif" font-size="16">842 GB</text>
      <rect x="435" y="20" width="80" height="28" rx="6" fill="#00E676" fill-opacity="0.2"/>
      <text x="475" y="38" fill="#00E676" font-family="sans-serif" font-size="13" font-weight="bold" text-anchor="middle">Активен</text>
      <text x="560" y="40" fill="#00E5FF" font-family="sans-serif" font-size="16">QR • ⚙️</text>
      <line x1="0" y1="70" x2="670" y2="70" stroke="#252A3D" stroke-width="1"/>
    </g>

    <!-- Row 2: Shadowsocks 2022 -->
    <g transform="translate(0, 200)">
      <text x="24" y="30" fill="#00E5FF" font-family="sans-serif" font-size="17" font-weight="bold">Shadowsocks</text>
      <text x="24" y="50" fill="#6B7280" font-family="sans-serif" font-size="13">2022-blake3</text>
      <text x="170" y="40" fill="#FFFFFF" font-family="monospace" font-size="16">2053</text>
      <text x="270" y="40" fill="#FFFFFF" font-family="sans-serif" font-size="16">120 GB</text>
      <rect x="435" y="20" width="80" height="28" rx="6" fill="#00E676" fill-opacity="0.2"/>
      <text x="475" y="38" fill="#00E676" font-family="sans-serif" font-size="13" font-weight="bold" text-anchor="middle">Активен</text>
      <text x="560" y="40" fill="#00E5FF" font-family="sans-serif" font-size="16">QR • ⚙️</text>
      <line x1="0" y1="70" x2="670" y2="70" stroke="#252A3D" stroke-width="1"/>
    </g>

    <!-- Row 3: VMess WebSocket -->
    <g transform="translate(0, 280)">
      <text x="24" y="30" fill="#00E5FF" font-family="sans-serif" font-size="17" font-weight="bold">VMess WS</text>
      <text x="24" y="50" fill="#6B7280" font-family="sans-serif" font-size="13">Cloudflare CDN</text>
      <text x="170" y="40" fill="#FFFFFF" font-family="monospace" font-size="16">2083</text>
      <text x="270" y="40" fill="#FFFFFF" font-family="sans-serif" font-size="16">315 GB</text>
      <rect x="435" y="20" width="80" height="28" rx="6" fill="#00E676" fill-opacity="0.2"/>
      <text x="475" y="38" fill="#00E676" font-family="sans-serif" font-size="13" font-weight="bold" text-anchor="middle">Активен</text>
      <text x="560" y="40" fill="#00E5FF" font-family="sans-serif" font-size="16">QR • ⚙️</text>
      <line x1="0" y1="70" x2="670" y2="70" stroke="#252A3D" stroke-width="1"/>
    </g>
  </g>
</svg>"""

# 3. screen_tabs_drawer.svg
svg_drawer = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1440" width="720" height="1440">
  <defs>
    <linearGradient id="bgDim" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#08090E"/>
      <stop offset="100%" stop-color="#0B0D14"/>
    </linearGradient>
    <linearGradient id="drawerGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#161926"/>
      <stop offset="100%" stop-color="#1B1F2E"/>
    </linearGradient>
    <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#00E5FF"/>
      <stop offset="100%" stop-color="#00B0FF"/>
    </linearGradient>
  </defs>

  <!-- Dimmed Background -->
  <rect width="720" height="1440" fill="url(#bgDim)" opacity="0.95"/>

  <!-- Right Slide-Out Drawer (Width 540) -->
  <g transform="translate(180, 0)">
    <rect width="540" height="1440" fill="url(#drawerGrad)" stroke="#2B3045" stroke-width="1.5"/>

    <!-- Drawer Header -->
    <g transform="translate(30, 80)">
      <path d="M10,14 L30,14 M10,22 L30,22 M10,30 L30,30" stroke="#00E5FF" stroke-width="3" stroke-linecap="round"/>
      <text x="45" y="26" fill="#FFFFFF" font-family="sans-serif" font-size="26" font-weight="bold">Открытые серверы</text>
      <rect x="360" y="4" width="36" height="30" rx="8" fill="#00E5FF"/>
      <text x="378" y="24" fill="#000000" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">2</text>

      <!-- Close Drawer (X) -->
      <g transform="translate(435, 4)">
        <circle cx="16" cy="16" r="16" fill="#252A3C"/>
        <path d="M10,10 L22,22 M22,10 L10,22" stroke="#FFFFFF" stroke-width="2"/>
      </g>
    </g>

    <!-- Subtitle -->
    <text x="30" y="140" fill="#9CA3AF" font-family="sans-serif" font-size="16">Быстрое переключение между открытыми веб-панелями</text>

    <!-- Tab 1: Frankfurt (Active) -->
    <g transform="translate(24, 180)">
      <rect width="490" height="160" rx="20" fill="#1C2433" stroke="#00E5FF" stroke-width="2"/>
      <circle cx="30" cy="35" r="6" fill="#00E676"/>
      <text x="48" y="40" fill="#FFFFFF" font-family="sans-serif" font-size="22" font-weight="bold">🇩🇪 Frankfurt Node-01</text>
      
      <!-- Active Badge -->
      <rect x="48" y="60" width="90" height="28" rx="6" fill="#00E676" fill-opacity="0.2"/>
      <text x="93" y="78" fill="#00E676" font-family="sans-serif" font-size="13" font-weight="bold" text-anchor="middle">● Активна</text>

      <text x="30" y="120" fill="#9CA3AF" font-family="monospace" font-size="16">127.0.0.1:2053 ➔ 203.0.113.***</text>

      <!-- Close Tab (✕) -->
      <g transform="translate(430, 20)">
        <circle cx="18" cy="18" r="18" fill="#FF5252" fill-opacity="0.2"/>
        <path d="M12,12 L24,24 M24,12 L12,24" stroke="#FF5252" stroke-width="2.5"/>
      </g>
    </g>

    <!-- Tab 2: Amsterdam (In Background) -->
    <g transform="translate(24, 360)">
      <rect width="490" height="160" rx="20" fill="#1A1E2B" stroke="#2B3045" stroke-width="1.2"/>
      <circle cx="30" cy="35" r="5" fill="#FFB300"/>
      <text x="48" y="40" fill="#FFFFFF" font-family="sans-serif" font-size="22" font-weight="bold">🇳🇱 Amsterdam Edge-02</text>
      
      <!-- Background Badge -->
      <rect x="48" y="60" width="85" height="28" rx="6" fill="#FFB300" fill-opacity="0.2"/>
      <text x="90" y="78" fill="#FFB300" font-family="sans-serif" font-size="13" font-weight="bold" text-anchor="middle">В фоне</text>

      <text x="30" y="120" fill="#9CA3AF" font-family="monospace" font-size="16">127.0.0.1:2087 ➔ 198.51.100.***</text>

      <!-- Close Tab (✕) -->
      <g transform="translate(430, 20)">
        <circle cx="18" cy="18" r="18" fill="#2A2F42"/>
        <path d="M12,12 L24,24 M24,12 L12,24" stroke="#9CA3AF" stroke-width="2"/>
      </g>
    </g>

    <!-- Bottom Actions -->
    <g transform="translate(24, 1250)">
      <!-- Close All -->
      <rect width="490" height="60" rx="16" fill="#FF5252" fill-opacity="0.15" stroke="#FF5252" stroke-width="1.5"/>
      <text x="245" y="1288" fill="#FF5252" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">✕ Закрыть все вкладки</text>

      <!-- Go to Servers List -->
      <rect y="75" width="490" height="60" rx="16" fill="url(#cyanGrad)"/>
      <text x="245" y="1362" fill="#000000" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">⌂ Все серверы (Главная)</text>
    </g>
  </g>
</svg>"""

# 4. screen_settings.svg
svg_settings = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1440" width="720" height="1440">
  <defs>
    <linearGradient id="bgDim" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#08090E"/>
      <stop offset="100%" stop-color="#0E1017"/>
    </linearGradient>
    <linearGradient id="dialogGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#191C29"/>
      <stop offset="100%" stop-color="#141620"/>
    </linearGradient>
    <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#00E5FF"/>
      <stop offset="100%" stop-color="#00B0FF"/>
    </linearGradient>
  </defs>

  <rect width="720" height="1440" fill="url(#bgDim)" opacity="0.95"/>

  <!-- Modal Settings Dialog -->
  <g transform="translate(36, 120)">
    <rect width="648" height="1200" rx="28" fill="url(#dialogGrad)" stroke="#2B3045" stroke-width="2"/>

    <!-- Dialog Header with Top-Right Close Cross -->
    <g transform="translate(32, 45)">
      <circle cx="16" cy="16" r="16" fill="#00E5FF" fill-opacity="0.2"/>
      <circle cx="16" cy="16" r="6" fill="#00E5FF"/>
      <text x="46" y="24" fill="#FFFFFF" font-family="sans-serif" font-size="28" font-weight="bold">Настройки</text>

      <!-- Top Right (✕) -->
      <g transform="translate(540, 0)">
        <circle cx="18" cy="18" r="18" fill="#262A3C"/>
        <path d="M12,12 L24,24 M24,12 L12,24" stroke="#FFFFFF" stroke-width="2.5"/>
      </g>
    </g>

    <!-- Tabs: [Общие] | [Бэкап] -->
    <g transform="translate(32, 100)">
      <rect width="285" height="50" rx="12" fill="#00E5FF" fill-opacity="0.2" stroke="#00E5FF" stroke-width="1.5"/>
      <text x="142" y="132" fill="#00E5FF" font-family="sans-serif" font-size="18" font-weight="bold" text-anchor="middle">⚙️ Общие</text>

      <rect x="295" width="285" height="50" rx="12" fill="#1E2232"/>
      <text x="437" y="132" fill="#9CA3AF" font-family="sans-serif" font-size="18" text-anchor="middle">☁️ Бэкап и WebDAV</text>
    </g>

    <!-- 1. Tunnel Lifecycle Policy -->
    <g transform="translate(32, 180)">
      <rect width="584" height="160" rx="18" fill="#1C2030" stroke="#262A3C" stroke-width="1"/>
      <text x="24" y="38" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="bold">Режим закрытия туннеля</text>
      <text x="24" y="66" fill="#9CA3AF" font-family="sans-serif" font-size="15">Как туннель завершает работу при навигации</text>

      <rect x="24" y="90" width="536" height="50" rx="10" fill="#252A3E" stroke="#00E5FF" stroke-width="1.2"/>
      <text x="44" y="122" fill="#00E5FF" font-family="sans-serif" font-size="16" font-weight="bold">Закрывать при выходе из приложения ▾</text>
    </g>

    <!-- 2. Language Picker -->
    <g transform="translate(32, 360)">
      <rect width="584" height="140" rx="18" fill="#1C2030" stroke="#262A3C" stroke-width="1"/>
      <text x="24" y="38" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="bold">Язык приложения / Language</text>

      <rect x="24" y="65" width="165" height="50" rx="10" fill="#00E5FF" fill-opacity="0.2" stroke="#00E5FF" stroke-width="1.5"/>
      <text x="106" y="96" fill="#00E5FF" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">🇷🇺 Русский</text>

      <rect x="205" y="65" width="165" height="50" rx="10" fill="#252A3E"/>
      <text x="287" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="16" text-anchor="middle">🇬🇧 English</text>

      <rect x="385" y="65" width="175" height="50" rx="10" fill="#252A3E"/>
      <text x="472" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="16" text-anchor="middle">🌐 Авто (Система)</text>
    </g>

    <!-- 3. Theme Selector -->
    <g transform="translate(32, 520)">
      <rect width="584" height="140" rx="18" fill="#1C2030" stroke="#262A3C" stroke-width="1"/>
      <text x="24" y="38" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="bold">Тема оформления (Material 3)</text>

      <rect x="24" y="65" width="165" height="50" rx="10" fill="#00E5FF" fill-opacity="0.2" stroke="#00E5FF" stroke-width="1.5"/>
      <text x="106" y="96" fill="#00E5FF" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">🌙 Темная</text>

      <rect x="205" y="65" width="165" height="50" rx="10" fill="#252A3E"/>
      <text x="287" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="16" text-anchor="middle">☀️ Светлая</text>

      <rect x="385" y="65" width="175" height="50" rx="10" fill="#252A3E"/>
      <text x="472" y="96" fill="#9CA3AF" font-family="sans-serif" font-size="16" text-anchor="middle">⚙️ Системная</text>
    </g>

    <!-- 4. WebDAV Cloud Sync Card -->
    <g transform="translate(32, 680)">
      <rect width="584" height="230" rx="18" fill="#1C2030" stroke="#00E5FF" stroke-opacity="0.3" stroke-width="1.2"/>
      <text x="24" y="38" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="bold">Синхронизация WebDAV</text>
      <text x="24" y="64" fill="#9CA3AF" font-family="sans-serif" font-size="14">Яндекс Диск, Nextcloud или собственный сервер</text>

      <rect x="24" y="85" width="536" height="50" rx="10" fill="#141722" stroke="#2B3045" stroke-width="1"/>
      <text x="44" y="116" fill="#FFFFFF" font-family="monospace" font-size="15">https://webdav.yandex.ru/backup.json</text>

      <g transform="translate(24, 150)">
        <rect width="255" height="52" rx="12" fill="url(#cyanGrad)"/>
        <text x="127" y="183" fill="#000000" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">☁️ Выгрузить</text>

        <rect x="275" width="260" height="52" rx="12" fill="#252A3E" stroke="#00E5FF" stroke-width="1.2"/>
        <text x="405" y="183" fill="#00E5FF" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">⬇️ Восстановить</text>
      </g>
    </g>

    <!-- 5. App Version & Updates -->
    <g transform="translate(32, 930)">
      <rect width="584" height="150" rx="18" fill="#1C2030" stroke="#262A3C" stroke-width="1"/>
      <text x="24" y="38" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="bold">3x manager • Версия v1.1.0</text>
      <text x="24" y="64" fill="#00E676" font-family="sans-serif" font-size="14">У вас установлена актуальная версия</text>

      <g transform="translate(24, 80)">
        <rect width="255" height="48" rx="10" fill="#252A3E" stroke="#2B3045" stroke-width="1"/>
        <text x="127" y="110" fill="#FFFFFF" font-family="sans-serif" font-size="15" text-anchor="middle">История версий</text>

        <rect x="275" width="260" height="48" rx="10" fill="url(#cyanGrad)"/>
        <text x="405" y="110" fill="#000000" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">⟳ Проверить обновления</text>
      </g>
    </g>
  </g>
</svg>"""

files = {
    "screen_main.svg": svg_main,
    "screen_webpanel.svg": svg_webpanel,
    "screen_tabs_drawer.svg": svg_drawer,
    "screen_settings.svg": svg_settings
}

for name, content in files.items():
    path = os.path.join("docs/screenshots", name)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Written {path}")

