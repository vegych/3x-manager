const fs = require('fs');
const path = require('path');
const { Resvg } = require('@resvg/resvg-js');

const outDir = path.join(__dirname, 'docs', 'screenshots');
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

// Common Android Phone Mockup Frame Helpers (800 x 1680, 20:9 ratio)
function createPhoneBase(content) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 1680" width="800" height="1680">
  <defs>
    <style>
      @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600&amp;family=Plus+Jakarta+Sans:wght@400;500;600;700;800&amp;display=swap');
      * {
        box-sizing: border-box;
      }
      .font-sans {
        font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      }
      .font-mono {
        font-family: 'JetBrains Mono', 'SF Mono', Menlo, Consolas, Monaco, monospace;
      }
    </style>
    <filter id="shadow-card" x="-10%" y="-10%" width="120%" height="130%">
      <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="#000000" flood-opacity="0.35"/>
    </filter>
    <filter id="shadow-dialog" x="-20%" y="-20%" width="140%" height="150%">
      <feDropShadow dx="0" dy="16" stdDeviation="24" flood-color="#000000" flood-opacity="0.6"/>
    </filter>
    <filter id="shadow-fab" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="6" stdDeviation="10" flood-color="#1677FF" flood-opacity="0.4"/>
    </filter>
  </defs>

  <!-- Deep Slate Background -->
  <rect width="800" height="1680" fill="#0E141E"/>

  <!-- Status Bar (48px) -->
  <g class="font-sans">
    <text x="44" y="34" fill="#FFFFFF" font-size="18" font-weight="700">09:41</text>
    <!-- Wi-Fi & Cellular -->
    <g transform="translate(680, 18)" fill="#FFFFFF">
      <path d="M0,18 L4,18 L4,22 L0,22 Z M6,14 L10,14 L10,22 L6,22 Z M12,9 L16,9 L16,22 L12,22 Z M18,4 L22,4 L22,22 L18,22 Z"/>
    </g>
    <!-- Battery 100% -->
    <g transform="translate(718, 19)">
      <rect x="0" y="3" width="28" height="15" rx="3.5" fill="none" stroke="#FFFFFF" stroke-width="2"/>
      <rect x="3" y="6" width="18" height="9" rx="2" fill="#52C41A"/>
      <rect x="29" y="8" width="2.5" height="5" rx="1" fill="#FFFFFF"/>
    </g>
  </g>

  <!-- Body Content -->
  ${content}

  <!-- Android Bottom Gesture Navigation Pill -->
  <rect x="290" y="1656" width="220" height="5" rx="2.5" fill="#FFFFFF" fill-opacity="0.45"/>
</svg>`;
}

// -------------------------------------------------------------
// 1. SCREEN MAIN: Server List & Termius Cards
// -------------------------------------------------------------
const screenMainContent = `
  <!-- Top App Bar (height 96px, from y=48 to y=144) -->
  <rect y="48" width="800" height="96" fill="#161E2C"/>
  <line x1="0" y1="144" x2="800" y2="144" stroke="#253247" stroke-width="1.5"/>

  <!-- App Title -->
  <text x="32" y="108" class="font-sans" fill="#FFFFFF" font-size="26" font-weight="800" letter-spacing="-0.5">3x manager</text>

  <!-- Top App Bar Actions (Right-aligned) -->
  <!-- 1. Open Tabs Button with Badge "2" -->
  <g transform="translate(324, 72)">
    <rect width="48" height="48" rx="14" fill="#1F293B"/>
    <!-- Layers Icon -->
    <path d="M24,14 L12,20 L24,26 L36,20 Z M12,25 L24,31 L36,25 M12,30 L24,36 L36,30" fill="none" stroke="#1677FF" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
    <!-- Cyan Badge "2" -->
    <rect x="30" y="-4" width="22" height="20" rx="10" fill="#1677FF"/>
    <text x="41" y="10" class="font-sans" fill="#FFFFFF" font-size="11" font-weight="800" text-anchor="middle">2</text>
  </g>

  <!-- 2. Disconnect Tunnel Button "СТОП" -->
  <g transform="translate(380, 72)">
    <rect width="78" height="48" rx="14" fill="#FF4D4F" fill-opacity="0.15" stroke="#FF4D4F" stroke-opacity="0.45" stroke-width="1.2"/>
    <circle cx="14" cy="24" r="4" fill="#52C41A"/>
    <!-- Power Icon -->
    <path d="M28,18 L28,24 M24,20 A5.5,5.5 0 1,0 32,20" fill="none" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round"/>
    <text x="54" y="30" class="font-sans" fill="#FF4D4F" font-size="12" font-weight="800" text-anchor="middle">СТОП</text>
  </g>

  <!-- 3. 3x-ui Panel Update Badge (Placed to the LEFT of the App update badge!) -->
  <g transform="translate(466, 72)">
    <rect width="118" height="48" rx="14" fill="#FAAD14" fill-opacity="0.18" stroke="#FAAD14" stroke-opacity="0.65" stroke-width="1.2"/>
    <rect x="12" y="18" width="14" height="12" rx="2" fill="none" stroke="#FAAD14" stroke-width="1.8"/>
    <circle cx="15" cy="24" r="1.5" fill="#FAAD14"/>
    <text x="68" y="29" class="font-sans" fill="#FAAD14" font-size="12" font-weight="800" text-anchor="middle">3x-ui v3.9.0</text>
  </g>

  <!-- 4. App Update Badge "☁ v1.1.0" (Placed to the LEFT of the eye icon!) -->
  <g transform="translate(592, 72)">
    <rect width="88" height="48" rx="14" fill="#1677FF" fill-opacity="0.18" stroke="#1677FF" stroke-opacity="0.65" stroke-width="1.2"/>
    <text x="16" y="30" fill="#1677FF" font-size="15">☁</text>
    <text x="53" y="29" class="font-sans" fill="#1677FF" font-size="12" font-weight="800" text-anchor="middle">v1.1.0</text>
  </g>

  <!-- 5. Eye Mask Icon (Slashed eye for masked IP) -->
  <g transform="translate(688, 72)">
    <circle cx="24" cy="24" r="22" fill="#1F293B"/>
    <path d="M14,24 C17,17 31,17 34,24 C31,31 17,31 14,24 Z" fill="none" stroke="#1677FF" stroke-width="2.2"/>
    <circle cx="24" cy="24" r="3.5" fill="#1677FF"/>
    <line x1="13" y1="13" x2="35" y2="35" stroke="#1677FF" stroke-width="2.2" stroke-linecap="round"/>
  </g>

  <!-- 6. Settings Gear Icon -->
  <g transform="translate(740, 72)">
    <circle cx="24" cy="24" r="22" fill="#1F293B"/>
    <circle cx="24" cy="24" r="7" fill="none" stroke="#8C9BA5" stroke-width="2.2"/>
    <path d="M24,13 L24,15 M24,33 L24,35 M13,24 L15,24 M33,24 L35,24" stroke="#8C9BA5" stroke-width="3" stroke-linecap="round"/>
  </g>

  <!-- Server Count & Sort Header (y=164) -->
  <g transform="translate(32, 164)" class="font-sans">
    <text x="0" y="24" fill="#8C9BA5" font-size="15" font-weight="600">Всего серверов: 3</text>
    
    <!-- Sort Pills -->
    <!-- [По частоте] (Selected) -->
    <rect x="360" y="0" width="120" height="36" rx="10" fill="#1677FF" fill-opacity="0.18" stroke="#1677FF" stroke-width="1.2"/>
    <text x="420" y="23" fill="#1677FF" font-size="13" font-weight="700" text-anchor="middle">По частоте</text>

    <!-- [По алфавиту] -->
    <rect x="490" y="0" width="124" height="36" rx="10" fill="#1F293B" fill-opacity="0.5"/>
    <text x="552" y="23" fill="#8C9BA5" font-size="13" font-weight="500" text-anchor="middle">По алфавиту</text>

    <!-- [По дате] -->
    <rect x="624" y="0" width="112" height="36" rx="10" fill="#1F293B" fill-opacity="0.5"/>
    <text x="680" y="23" fill="#8C9BA5" font-size="13" font-weight="500" text-anchor="middle">По дате</text>
  </g>

  <!-- Active Tunnel Banner (y=216, height 58) -->
  <g transform="translate(32, 216)">
    <rect width="736" height="58" rx="14" fill="#FF4D4F" fill-opacity="0.08" stroke="#FF4D4F" stroke-opacity="0.35" stroke-width="1.2"/>
    <circle cx="28" cy="29" r="5" fill="#52C41A"/>
    <text x="48" y="35" class="font-sans" fill="#FFFFFF" font-size="15" font-weight="600">Туннель: Frankfurt Node-01</text>
    
    <!-- Button "Отключить" inside banner -->
    <g transform="translate(594, 12)">
      <rect width="130" height="34" rx="8" fill="#FF4D4F"/>
      <!-- Power Icon -->
      <path d="M22,12 L22,17 M18,14 A5,5 0 1,0 26,14" fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round"/>
      <text x="74" y="23" class="font-sans" fill="#FFFFFF" font-size="13" font-weight="700" text-anchor="middle">Отключить</text>
    </g>
  </g>

  <!-- ================= SERVER CARD 1 (Selected & Active) ================= -->
  <g transform="translate(32, 294)" filter="url(#shadow-card)">
    <!-- Container -->
    <rect width="736" height="248" rx="20" fill="#161E2C" stroke="#1677FF" stroke-width="2"/>
    
    <!-- Header: Termius Badge L, Name, Status Badge -->
    <rect x="22" y="22" width="40" height="40" rx="10" fill="#3B3E54"/>
    <text x="42" y="49" class="font-sans" fill="#FFFFFF" font-size="22" font-weight="900" text-anchor="middle">L</text>

    <text x="76" y="40" class="font-sans" fill="#FFFFFF" font-size="19" font-weight="700">Frankfurt Node-01</text>
    <text x="76" y="60" class="font-mono" fill="#8C9BA5" font-size="13">SSH: Frankfurt Node-01 (localhost:2053)</text>

    <!-- Status Badge (Top Right) -->
    <rect x="562" y="22" width="152" height="32" rx="8" fill="#52C41A" fill-opacity="0.15" stroke="#52C41A" stroke-opacity="0.3" stroke-width="1"/>
    <circle cx="578" cy="38" r="4" fill="#52C41A"/>
    <text x="644" y="44" class="font-sans" fill="#52C41A" font-size="13" font-weight="700" text-anchor="middle">Туннель активен</text>

    <!-- Route summary surface -->
    <rect x="22" y="80" width="692" height="44" rx="10" fill="#1F293B" fill-opacity="0.55"/>
    <text x="38" y="108" class="font-sans" fill="#1677FF" font-size="13" font-weight="800">SSH ТУННЕЛЬ</text>
    <text x="146" y="108" class="font-sans" fill="#8C9BA5" font-size="14">➔</text>
    <text x="174" y="108" class="font-mono" fill="#8C9BA5" font-size="14">root@203.0.113.***</text>

    <!-- Action Buttons Row -->
    <!-- Primary Button: [ ➔ Открыть ] -->
    <rect x="22" y="148" width="156" height="52" rx="12" fill="#1677FF"/>
    <!-- Login Icon -->
    <path d="M44,174 L58,174 M52,168 L58,174 L52,180" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
    <rect x="36" y="165" width="5" height="18" rx="2" fill="#FFFFFF"/>
    <text x="106" y="180" class="font-sans" fill="#FFFFFF" font-size="16" font-weight="700">Открыть</text>

    <!-- Secondary Action Icons (Edit & Delete) -->
    <g transform="translate(614, 152)">
      <circle cx="24" cy="24" r="22" fill="#1F293B"/>
      <path d="M16,30 L27,19 L30,22 L19,33 L14,34 Z" fill="none" stroke="#8C9BA5" stroke-width="1.8"/>

      <circle cx="78" cy="24" r="22" fill="#1F293B"/>
      <path d="M71,18 L85,18 M73,18 L73,31 M83,18 L83,31 M70,15 L86,15" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round"/>
    </g>
  </g>

  <!-- ================= SERVER CARD 2 (Amsterdam Edge-02) ================= -->
  <g transform="translate(32, 566)" filter="url(#shadow-card)">
    <rect width="736" height="248" rx="20" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    
    <rect x="22" y="22" width="40" height="40" rx="10" fill="#3B3E54"/>
    <text x="42" y="49" class="font-sans" fill="#FFFFFF" font-size="22" font-weight="900" text-anchor="middle">L</text>

    <text x="76" y="40" class="font-sans" fill="#FFFFFF" font-size="19" font-weight="700">Amsterdam Edge-02</text>
    <text x="76" y="60" class="font-mono" fill="#8C9BA5" font-size="13">SSH: Amsterdam Edge-02 (localhost:2087)</text>

    <!-- Status Badge -->
    <rect x="580" y="22" width="134" height="32" rx="8" fill="#FAAD14" fill-opacity="0.15" stroke="#FAAD14" stroke-opacity="0.3" stroke-width="1"/>
    <circle cx="596" cy="38" r="4" fill="#FAAD14"/>
    <text x="654" y="44" class="font-sans" fill="#FAAD14" font-size="13" font-weight="700" text-anchor="middle">Авто-проброс</text>

    <rect x="22" y="80" width="692" height="44" rx="10" fill="#1F293B" fill-opacity="0.55"/>
    <text x="38" y="108" class="font-sans" fill="#1677FF" font-size="13" font-weight="800">SSH ТУННЕЛЬ</text>
    <text x="146" y="108" class="font-sans" fill="#8C9BA5" font-size="14">➔</text>
    <text x="174" y="108" class="font-mono" fill="#8C9BA5" font-size="14">admin@198.51.100.***</text>

    <rect x="22" y="148" width="156" height="52" rx="12" fill="#1677FF"/>
    <path d="M44,174 L58,174 M52,168 L58,174 L52,180" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
    <rect x="36" y="165" width="5" height="18" rx="2" fill="#FFFFFF"/>
    <text x="106" y="180" class="font-sans" fill="#FFFFFF" font-size="16" font-weight="700">Открыть</text>

    <g transform="translate(614, 152)">
      <circle cx="24" cy="24" r="22" fill="#1F293B"/>
      <path d="M16,30 L27,19 L30,22 L19,33 L14,34 Z" fill="none" stroke="#8C9BA5" stroke-width="1.8"/>

      <circle cx="78" cy="24" r="22" fill="#1F293B"/>
      <path d="M71,18 L85,18 M73,18 L73,31 M83,18 L83,31 M70,15 L86,15" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round"/>
    </g>
  </g>

  <!-- ================= SERVER CARD 3 (Tokyo Relay-03) ================= -->
  <g transform="translate(32, 838)" filter="url(#shadow-card)">
    <rect width="736" height="248" rx="20" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    
    <rect x="22" y="22" width="40" height="40" rx="10" fill="#3B3E54"/>
    <text x="42" y="49" class="font-sans" fill="#FFFFFF" font-size="22" font-weight="900" text-anchor="middle">L</text>

    <text x="76" y="40" class="font-sans" fill="#FFFFFF" font-size="19" font-weight="700">Tokyo Relay-03</text>
    <text x="76" y="60" class="font-mono" fill="#8C9BA5" font-size="13">SSH: Tokyo Relay-03 (localhost:2096)</text>

    <rect x="580" y="22" width="134" height="32" rx="8" fill="#FAAD14" fill-opacity="0.15" stroke="#FAAD14" stroke-opacity="0.3" stroke-width="1"/>
    <circle cx="596" cy="38" r="4" fill="#FAAD14"/>
    <text x="654" y="44" class="font-sans" fill="#FAAD14" font-size="13" font-weight="700" text-anchor="middle">Авто-проброс</text>

    <rect x="22" y="80" width="692" height="44" rx="10" fill="#1F293B" fill-opacity="0.55"/>
    <text x="38" y="108" class="font-sans" fill="#1677FF" font-size="13" font-weight="800">SSH ТУННЕЛЬ</text>
    <text x="146" y="108" class="font-sans" fill="#8C9BA5" font-size="14">➔</text>
    <text x="174" y="108" class="font-mono" fill="#8C9BA5" font-size="14">deploy@192.0.2.***</text>

    <rect x="22" y="148" width="156" height="52" rx="12" fill="#1677FF"/>
    <path d="M44,174 L58,174 M52,168 L58,174 L52,180" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
    <rect x="36" y="165" width="5" height="18" rx="2" fill="#FFFFFF"/>
    <text x="106" y="180" class="font-sans" fill="#FFFFFF" font-size="16" font-weight="700">Открыть</text>

    <g transform="translate(614, 152)">
      <circle cx="24" cy="24" r="22" fill="#1F293B"/>
      <path d="M16,30 L27,19 L30,22 L19,33 L14,34 Z" fill="none" stroke="#8C9BA5" stroke-width="1.8"/>

      <circle cx="78" cy="24" r="22" fill="#1F293B"/>
      <path d="M71,18 L85,18 M73,18 L73,31 M83,18 L83,31 M70,15 L86,15" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round"/>
    </g>
  </g>

  <!-- Floating Action Button (FAB "+") at bottom right -->
  <g transform="translate(684, 1520)" filter="url(#shadow-fab)">
    <rect width="72" height="72" rx="22" fill="#1677FF"/>
    <path d="M36,22 L36,50 M22,36 L50,36" stroke="#FFFFFF" stroke-width="3.5" stroke-linecap="round"/>
  </g>
`;

// -------------------------------------------------------------
// 2. SCREEN WEBPANEL: Embedded 3x-ui Web Panel & Dual View
// -------------------------------------------------------------
const screenWebpanelContent = `
  <!-- Android Top Bar in WebPanelScreen (height 72px, y=48 to 120) -->
  <rect y="48" width="800" height="72" fill="#161E2C"/>
  <line x1="0" y1="120" x2="800" y2="120" stroke="#253247" stroke-width="1.5"/>

  <!-- Back to Server List Arrow -->
  <g transform="translate(20, 60)">
    <circle cx="24" cy="24" r="22" fill="#1F293B"/>
    <path d="M29,24 L17,24 M23,18 L17,24 L23,30" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
  </g>

  <!-- Server Title & Live Tunnel Status -->
  <g transform="translate(80, 68)">
    <circle cx="6" cy="14" r="5" fill="#52C41A"/>
    <text x="22" y="19" class="font-sans" fill="#FFFFFF" font-size="18" font-weight="700">Frankfurt Node-01</text>
    <text x="22" y="38" class="font-mono" fill="#52C41A" font-size="12">Туннель ➔ Frankfurt Node-01 (127.0.0.1:2053)</text>
  </g>

  <!-- Top Right Actions: Tabs [2], PC Mode [🖥 ПК 1280px], Refresh [⟳] -->
  <g transform="translate(520, 60)">
    <!-- Active Tabs Badge -->
    <rect width="48" height="48" rx="14" fill="#1F293B"/>
    <path d="M24,14 L12,20 L24,26 L36,20 Z M12,25 L24,31 L36,25 M12,30 L24,36 L36,30" fill="none" stroke="#1677FF" stroke-width="2.2" stroke-linecap="round"/>
    <rect x="30" y="-4" width="22" height="20" rx="10" fill="#1677FF"/>
    <text x="41" y="10" class="font-sans" fill="#FFFFFF" font-size="11" font-weight="800" text-anchor="middle">2</text>
  </g>

  <!-- Mode Toggle [🖥 ПК 1280px] -->
  <g transform="translate(580, 60)">
    <rect width="134" height="48" rx="14" fill="#1677FF" fill-opacity="0.18" stroke="#1677FF" stroke-width="1.2"/>
    <text x="20" y="30" font-size="16">🖥</text>
    <text x="76" y="30" class="font-sans" fill="#1677FF" font-size="14" font-weight="800" text-anchor="middle">ПК 1280px</text>
  </g>

  <!-- Refresh Button -->
  <g transform="translate(724, 60)">
    <circle cx="24" cy="24" r="22" fill="#1F293B"/>
    <path d="M24,14 A10,10 0 1,1 16,21 M16,15 L16,21 L22,21" fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round"/>
  </g>

  <!-- ================= EMBEDDED 3X-UI WEB PANEL ================= -->
  <!-- 3x-ui Dark Ant Design Header (y=120, height 64) -->
  <rect y="120" width="800" height="64" fill="#001529"/>
  <!-- 3x-ui Logo Cube -->
  <g transform="translate(28, 134)">
    <rect width="36" height="36" rx="8" fill="#1677FF"/>
    <path d="M18,9 L10,14 L18,19 L26,14 Z M10,18 L18,23 L26,18 M10,22 L18,27 L26,22" fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round"/>
    <text x="48" y="26" class="font-sans" fill="#1677FF" font-size="22" font-weight="900" letter-spacing="1">3X-UI</text>
    <rect x="136" y="6" width="60" height="24" rx="6" fill="#1F293B"/>
    <text x="166" y="23" class="font-mono" fill="#8C9BA5" font-size="12" font-weight="700" text-anchor="middle">v2.4.2</text>
  </g>

  <g transform="translate(610, 142)">
    <rect width="84" height="30" rx="6" fill="#1F293B"/>
    <text x="42" y="20" class="font-sans" fill="#FFFFFF" font-size="13" font-weight="600" text-anchor="middle">👤 admin</text>
    <text x="124" y="20" class="font-sans" fill="#FF4D4F" font-size="13" font-weight="600">Выход ↪</text>
  </g>

  <!-- 3x-ui Navigation Tabs Menu (y=184, height 52) -->
  <rect y="184" width="800" height="52" fill="#141414"/>
  <text x="40" y="216" class="font-sans" fill="#1677FF" font-size="15" font-weight="700">📊 Статус системы</text>
  <rect x="36" y="232" width="144" height="3.5" fill="#1677FF"/>

  <text x="228" y="216" class="font-sans" fill="#8C9BA5" font-size="15" font-weight="500">👥 Подключения (Inbounds)</text>
  <text x="488" y="216" class="font-sans" fill="#8C9BA5" font-size="15" font-weight="500">⚙️ Настройки Xray</text>
  <text x="696" y="216" class="font-sans" fill="#8C9BA5" font-size="15" font-weight="500">📜 Логи</text>

  <!-- ================= 3X-UI SYSTEM GAUGES ================= -->
  <!-- 4 Cards: CPU, RAM, Storage, Uptime -->
  <g transform="translate(32, 256)" class="font-sans">
    <!-- Card 1: CPU -->
    <rect x="0" y="0" width="170" height="150" rx="14" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    <text x="20" y="32" fill="#8C9BA5" font-size="14" font-weight="600">ЦП (CPU)</text>
    <text x="20" y="76" fill="#1677FF" font-size="28" font-weight="800">14.2%</text>
    <rect x="20" y="96" width="130" height="8" rx="4" fill="#1F293B"/>
    <rect x="20" y="96" width="22" height="8" rx="4" fill="#1677FF"/>
    <text x="20" y="128" fill="#8C9BA5" font-size="12">AMD EPYC (2 vCPU)</text>

    <!-- Card 2: RAM -->
    <rect x="188" y="0" width="170" height="150" rx="14" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    <text x="208" y="32" fill="#8C9BA5" font-size="14" font-weight="600">Память (RAM)</text>
    <text x="208" y="76" fill="#52C41A" font-size="28" font-weight="800">35.4%</text>
    <rect x="208" y="96" width="130" height="8" rx="4" fill="#1F293B"/>
    <rect x="208" y="96" width="46" height="8" rx="4" fill="#52C41A"/>
    <text x="208" y="128" fill="#8C9BA5" font-size="12">1.42 GB / 4.00 GB</text>

    <!-- Card 3: Storage -->
    <rect x="376" y="0" width="170" height="150" rx="14" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    <text x="396" y="32" fill="#8C9BA5" font-size="14" font-weight="600">Диск (Disk)</text>
    <text x="396" y="76" fill="#FAAD14" font-size="28" font-weight="800">17.2%</text>
    <rect x="396" y="96" width="130" height="8" rx="4" fill="#1F293B"/>
    <rect x="396" y="96" width="26" height="8" rx="4" fill="#FAAD14"/>
    <text x="396" y="128" fill="#8C9BA5" font-size="12">8.6 GB / 50.0 GB</text>

    <!-- Card 4: Uptime -->
    <rect x="566" y="0" width="170" height="150" rx="14" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    <text x="586" y="32" fill="#8C9BA5" font-size="14" font-weight="600">Аптайм</text>
    <text x="586" y="72" fill="#FFFFFF" font-size="22" font-weight="800">32д 14ч</text>
    <text x="586" y="104" class="font-mono" fill="#52C41A" font-size="12" font-weight="600">Xray: v1.8.24</text>
    <text x="586" y="128" fill="#8C9BA5" font-size="12">Статус: Запущен</text>
  </g>

  <!-- Network Traffic Banner -->
  <g transform="translate(32, 424)">
    <rect width="736" height="74" rx="14" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    <text x="24" y="44" class="font-sans" fill="#FFFFFF" font-size="16" font-weight="700">Трафик сервера:</text>
    
    <g transform="translate(200, 26)">
      <circle cx="14" cy="14" r="14" fill="#52C41A" fill-opacity="0.18"/>
      <path d="M14,8 L14,20 M9,15 L14,20 L19,15" stroke="#52C41A" stroke-width="2" stroke-linecap="round"/>
      <text x="36" y="18" class="font-sans" fill="#8C9BA5" font-size="13">Входящий:</text>
      <text x="114" y="18" class="font-mono" fill="#52C41A" font-size="15" font-weight="700">1.24 TB</text>
    </g>

    <g transform="translate(460, 26)">
      <circle cx="14" cy="14" r="14" fill="#1677FF" fill-opacity="0.18"/>
      <path d="M14,20 L14,8 M9,13 L14,8 L19,13" stroke="#1677FF" stroke-width="2" stroke-linecap="round"/>
      <text x="36" y="18" class="font-sans" fill="#8C9BA5" font-size="13">Исходящий:</text>
      <text x="124" y="18" class="font-mono" fill="#1677FF" font-size="15" font-weight="700">389.4 GB</text>
    </g>
  </g>

  <!-- ================= 3X-UI INBOUNDS TABLE ================= -->
  <g transform="translate(32, 516)" class="font-sans">
    <text x="0" y="24" fill="#FFFFFF" font-size="19" font-weight="800">Активные подключения (Inbounds)</text>
    
    <!-- Table Container -->
    <rect x="0" y="40" width="736" height="420" rx="16" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
    
    <!-- Table Header (y=40, height 48) -->
    <rect x="0" y="40" width="736" height="48" rx="16" fill="#1F293B" fill-opacity="0.7"/>
    <text x="24" y="70" fill="#8C9BA5" font-size="13" font-weight="700">ID / ПРОТОКОЛ</text>
    <text x="200" y="70" fill="#8C9BA5" font-size="13" font-weight="700">НАЗВАНИЕ (REMARK) / ПОРТ</text>
    <text x="470" y="70" fill="#8C9BA5" font-size="13" font-weight="700">ТРАФИК</text>
    <text x="640" y="70" fill="#8C9BA5" font-size="13" font-weight="700">СТАТУС</text>

    <!-- Table Row 1: VLESS Reality -->
    <line x1="0" y1="88" x2="736" y2="88" stroke="#253247" stroke-width="1"/>
    <g transform="translate(24, 102)">
      <text x="0" y="28" fill="#8C9BA5" font-size="15" font-weight="600">#1</text>
      <rect x="30" y="8" width="80" height="28" rx="6" fill="#1677FF" fill-opacity="0.18" stroke="#1677FF" stroke-width="1"/>
      <text x="70" y="27" fill="#1677FF" font-size="13" font-weight="800" text-anchor="middle">VLESS</text>

      <text x="176" y="20" fill="#FFFFFF" font-size="15" font-weight="700">VLESS-Reality-Vision</text>
      <text x="176" y="38" class="font-mono" fill="#8C9BA5" font-size="12">Port: 443 • xtls-rprx-vision</text>

      <text x="446" y="20" class="font-mono" fill="#52C41A" font-size="13" font-weight="700">↓ 642.1 GB</text>
      <text x="446" y="38" class="font-mono" fill="#8C9BA5" font-size="12">14 клиентов</text>

      <!-- Toggle Switch ON -->
      <rect x="618" y="12" width="46" height="24" rx="12" fill="#52C41A"/>
      <circle cx="652" cy="24" r="9" fill="#FFFFFF"/>
    </g>

    <!-- Table Row 2: Shadowsocks 2022 -->
    <line x1="0" y1="168" x2="736" y2="168" stroke="#253247" stroke-width="1"/>
    <g transform="translate(24, 182)">
      <text x="0" y="28" fill="#8C9BA5" font-size="15" font-weight="600">#2</text>
      <rect x="30" y="8" width="116" height="28" rx="6" fill="#FAAD14" fill-opacity="0.18" stroke="#FAAD14" stroke-width="1"/>
      <text x="88" y="27" fill="#FAAD14" font-size="12" font-weight="800" text-anchor="middle">Shadowsocks</text>

      <text x="176" y="20" fill="#FFFFFF" font-size="15" font-weight="700">SS-2022-MultiClient</text>
      <text x="176" y="38" class="font-mono" fill="#8C9BA5" font-size="12">Port: 8443 • 2022-blake3</text>

      <text x="446" y="20" class="font-mono" fill="#52C41A" font-size="13" font-weight="700">↓ 184.2 GB</text>
      <text x="446" y="38" class="font-mono" fill="#8C9BA5" font-size="12">6 клиентов</text>

      <rect x="618" y="12" width="46" height="24" rx="12" fill="#52C41A"/>
      <circle cx="652" cy="24" r="9" fill="#FFFFFF"/>
    </g>

    <!-- Table Row 3: Trojan gRPC -->
    <line x1="0" y1="248" x2="736" y2="248" stroke="#253247" stroke-width="1"/>
    <g transform="translate(24, 262)">
      <text x="0" y="28" fill="#8C9BA5" font-size="15" font-weight="600">#3</text>
      <rect x="30" y="8" width="84" height="28" rx="6" fill="#52C41A" fill-opacity="0.18" stroke="#52C41A" stroke-width="1"/>
      <text x="72" y="27" fill="#52C41A" font-size="13" font-weight="800" text-anchor="middle">Trojan</text>

      <text x="176" y="20" fill="#FFFFFF" font-size="15" font-weight="700">Trojan-gRPC-TLS</text>
      <text x="176" y="38" class="font-mono" fill="#8C9BA5" font-size="12">Port: 2083 • gRPC multi</text>

      <text x="446" y="20" class="font-mono" fill="#52C41A" font-size="13" font-weight="700">↓ 94.8 GB</text>
      <text x="446" y="38" class="font-mono" fill="#8C9BA5" font-size="12">2 клиента</text>

      <rect x="618" y="12" width="46" height="24" rx="12" fill="#52C41A"/>
      <circle cx="652" cy="24" r="9" fill="#FFFFFF"/>
    </g>

    <!-- Table Row 4: VMess -->
    <line x1="0" y1="328" x2="736" y2="328" stroke="#253247" stroke-width="1"/>
    <g transform="translate(24, 342)">
      <text x="0" y="28" fill="#8C9BA5" font-size="15" font-weight="600">#4</text>
      <rect x="30" y="8" width="80" height="28" rx="6" fill="#FF4D4F" fill-opacity="0.18" stroke="#FF4D4F" stroke-width="1"/>
      <text x="70" y="27" fill="#FF4D4F" font-size="13" font-weight="800" text-anchor="middle">VMess</text>

      <text x="176" y="20" fill="#FFFFFF" font-size="15" font-weight="700">VMess-WS-CDN</text>
      <text x="176" y="38" class="font-mono" fill="#8C9BA5" font-size="12">Port: 2053 • WebSocket</text>

      <text x="446" y="20" class="font-mono" fill="#8C9BA5" font-size="13" font-weight="700">0.00 B</text>
      <text x="446" y="38" class="font-mono" fill="#8C9BA5" font-size="12">0 клиентов</text>

      <!-- Toggle Switch OFF -->
      <rect x="618" y="12" width="46" height="24" rx="12" fill="#1F293B"/>
      <circle cx="630" cy="24" r="9" fill="#8C9BA5"/>
    </g>
  </g>
`;

// -------------------------------------------------------------
// 3. SCREEN TABS DRAWER: Slide-Out Menu on the RIGHT side
// -------------------------------------------------------------
const screenTabsDrawerContent = `
  <!-- Dimmed Main Screen Background behind Scrim -->
  <g opacity="0.35">
    <rect y="48" width="800" height="96" fill="#161E2C"/>
    <rect x="32" y="180" width="220" height="40" fill="#1F293B" rx="10"/>
    <rect x="32" y="250" width="300" height="220" fill="#161E2C" rx="20"/>
    <rect x="32" y="500" width="300" height="220" fill="#161E2C" rx="20"/>
  </g>

  <!-- Dark Scrim Overlay (Black 60%) -->
  <rect width="800" height="1680" fill="#000000" fill-opacity="0.6"/>

  <!-- ================= RIGHT-SIDE SLIDING PANEL ================= -->
  <!-- Surface starts at x=280 to 800 (width 520px) on the RIGHT edge -->
  <g transform="translate(280, 0)" filter="url(#shadow-dialog)">
    <rect width="520" height="1680" rx="24" fill="#161E2C" stroke="#253247" stroke-width="1.5"/>

    <!-- Drawer Header (y=72) -->
    <g transform="translate(28, 72)">
      <!-- Cyan Layers Icon Box -->
      <rect width="44" height="44" rx="12" fill="#1677FF" fill-opacity="0.18"/>
      <path d="M22,12 L12,17 L22,22 L32,17 Z M12,21 L22,26 L32,21 M12,25 L22,30 L32,25" fill="none" stroke="#1677FF" stroke-width="2.2" stroke-linecap="round"/>

      <text x="58" y="22" class="font-sans" fill="#FFFFFF" font-size="20" font-weight="800">Открытые панели</text>
      <text x="58" y="40" class="font-sans" fill="#8C9BA5" font-size="13" font-weight="600">Открыто вкладок: 2</text>

      <!-- Close Drawer (✕) Cross Button on right -->
      <g transform="translate(420, 4)">
        <circle cx="18" cy="18" r="18" fill="#1F293B"/>
        <path d="M12,12 L24,24 M24,12 L12,24" stroke="#8C9BA5" stroke-width="2.2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- Main Menu / All Servers Shortcut Button (y=144) -->
    <g transform="translate(28, 144)">
      <rect width="464" height="54" rx="14" fill="#1F293B" fill-opacity="0.65"/>
      <!-- Dns Icon -->
      <rect x="18" y="16" width="22" height="10" rx="2" fill="none" stroke="#1677FF" stroke-width="2"/>
      <rect x="18" y="28" width="22" height="10" rx="2" fill="none" stroke="#1677FF" stroke-width="2"/>
      <circle cx="23" cy="21" r="1.5" fill="#1677FF"/>
      <circle cx="23" cy="33" r="1.5" fill="#1677FF"/>
      
      <text x="56" y="34" class="font-sans" fill="#FFFFFF" font-size="16" font-weight="700">Главное меню (Все сервера)</text>
    </g>

    <!-- Divider Line -->
    <line x1="28" y1="220" x2="492" y2="220" stroke="#253247" stroke-width="1.2"/>

    <!-- ================= TAB 1: Selected Server (Active) ================= -->
    <g transform="translate(28, 240)">
      <!-- Container with Cyan 1.5px border and tint -->
      <rect width="464" height="108" rx="16" fill="#1677FF" fill-opacity="0.15" stroke="#1677FF" stroke-width="1.5"/>

      <!-- Termius Badge L -->
      <rect x="18" y="20" width="36" height="36" rx="8" fill="#3B3E54"/>
      <text x="36" y="45" class="font-sans" fill="#FFFFFF" font-size="20" font-weight="900" text-anchor="middle">L</text>

      <text x="66" y="38" class="font-sans" fill="#FFFFFF" font-size="17" font-weight="800">Frankfurt Node-01</text>
      
      <!-- Live Status Row -->
      <circle cx="70" cy="58" r="4.5" fill="#52C41A"/>
      <text x="82" y="62" class="font-sans" fill="#52C41A" font-size="12" font-weight="700">Туннель активен</text>
      <text x="194" y="62" class="font-mono" fill="#8C9BA5" font-size="12">• 127.0.0.1:2053</text>

      <text x="66" y="86" class="font-mono" fill="#8C9BA5" font-size="12">root@203.0.113.***</text>

      <!-- Close Tab (✕) Cross Button -->
      <g transform="translate(414, 20)">
        <circle cx="16" cy="16" r="16" fill="#1F293B"/>
        <path d="M11,11 L21,21 M21,11 L11,21" stroke="#8C9BA5" stroke-width="2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- ================= TAB 2: Amsterdam Edge-02 ================= -->
    <g transform="translate(28, 368)">
      <rect width="464" height="108" rx="16" fill="#1F293B" fill-opacity="0.5" stroke="#253247" stroke-width="1.2"/>

      <rect x="18" y="20" width="36" height="36" rx="8" fill="#3B3E54"/>
      <text x="36" y="45" class="font-sans" fill="#FFFFFF" font-size="20" font-weight="900" text-anchor="middle">L</text>

      <text x="66" y="38" class="font-sans" fill="#FFFFFF" font-size="17" font-weight="700">Amsterdam Edge-02</text>
      
      <circle cx="70" cy="58" r="4.5" fill="#FAAD14"/>
      <text x="82" y="62" class="font-sans" fill="#FAAD14" font-size="12" font-weight="700">Фоновый туннель</text>
      <text x="200" y="62" class="font-mono" fill="#8C9BA5" font-size="12">• 127.0.0.1:2087</text>

      <text x="66" y="86" class="font-mono" fill="#8C9BA5" font-size="12">admin@198.51.100.***</text>

      <!-- Close Tab Button -->
      <g transform="translate(414, 20)">
        <circle cx="16" cy="16" r="16" fill="#1F293B"/>
        <path d="M11,11 L21,21 M21,11 L11,21" stroke="#8C9BA5" stroke-width="2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- Close All Tabs Button at bottom -->
    <g transform="translate(28, 1530)">
      <rect width="464" height="52" rx="14" fill="#FF4D4F" fill-opacity="0.12" stroke="#FF4D4F" stroke-width="1.2"/>
      <path d="M160,26 L174,26 M164,18 L170,34" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round"/>
      <text x="246" y="32" class="font-sans" fill="#FF4D4F" font-size="15" font-weight="800" text-anchor="middle">Закрыть все вкладки</text>
    </g>
  </g>
`;

// -------------------------------------------------------------
// 4. SCREEN SETTINGS: Settings Dialog & Cloud Sync
// -------------------------------------------------------------
const screenSettingsContent = `
  <!-- Dimmed Main Screen Background behind Scrim -->
  <g opacity="0.35">
    <rect y="48" width="800" height="96" fill="#161E2C"/>
    <rect x="32" y="180" width="736" height="58" fill="#FF4D4F" fill-opacity="0.1" rx="14"/>
    <rect x="32" y="260" width="736" height="248" fill="#161E2C" rx="20"/>
  </g>

  <!-- Dark Scrim Overlay (Black 70%) -->
  <rect width="800" height="1680" fill="#000000" fill-opacity="0.7"/>

  <!-- ================= CENTERED FLOATING ALERT DIALOG ================= -->
  <g transform="translate(40, 160)" filter="url(#shadow-dialog)">
    <rect width="720" height="1360" rx="24" fill="#161E2C" stroke="#253247" stroke-width="1.5"/>

    <!-- Dialog Header with Top-Right Close Cross (✕) -->
    <g transform="translate(32, 36)">
      <circle cx="18" cy="18" r="16" fill="#1677FF" fill-opacity="0.18"/>
      <circle cx="18" cy="18" r="6" fill="none" stroke="#1677FF" stroke-width="2.2"/>
      <text x="46" y="26" class="font-sans" fill="#FFFFFF" font-size="22" font-weight="800">Настройки</text>

      <!-- Top-Right Close (✕) Cross Button -->
      <g transform="translate(620, 0)">
        <circle cx="18" cy="18" r="18" fill="#1F293B"/>
        <path d="M11,11 L25,25 M25,11 L11,25" stroke="#8C9BA5" stroke-width="2.2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- TabRow: [⚙️ Общие] | [💾 Резервная копия] (y=88) -->
    <g transform="translate(32, 92)">
      <rect width="656" height="52" fill="#161E2C"/>
      
      <!-- Tab 0: Общие (Selected) -->
      <text x="164" y="32" class="font-sans" fill="#1677FF" font-size="16" font-weight="800" text-anchor="middle">⚙️ Общие</text>
      <rect x="32" y="48" width="264" height="3.5" fill="#1677FF"/>

      <!-- Tab 1: Резервная копия (Unselected) -->
      <text x="492" y="32" class="font-sans" fill="#8C9BA5" font-size="16" font-weight="500" text-anchor="middle">💾 Резервная копия</text>
      <line x1="0" y1="50" x2="656" y2="50" stroke="#253247" stroke-width="1"/>
    </g>

    <!-- ================= 1. Language Selection Cards ================= -->
    <g transform="translate(32, 174)" class="font-sans">
      <text x="0" y="16" fill="#8C9BA5" font-size="14" font-weight="700">Язык оформления</text>

      <!-- 3 Cards: Auto, RU (selected), EN -->
      <!-- Auto -->
      <rect x="0" y="30" width="208" height="58" rx="14" fill="#1F293B" fill-opacity="0.45"/>
      <text x="104" y="65" fill="#8C9BA5" font-size="15" font-weight="500" text-anchor="middle">Auto</text>

      <!-- RU (Selected) -->
      <rect x="224" y="30" width="208" height="58" rx="14" fill="#1677FF" fill-opacity="0.18" stroke="#1677FF" stroke-width="1.5"/>
      <text x="328" y="65" fill="#1677FF" font-size="15" font-weight="800" text-anchor="middle">RU (Русский)</text>

      <!-- EN -->
      <rect x="448" y="30" width="208" height="58" rx="14" fill="#1F293B" fill-opacity="0.45"/>
      <text x="552" y="65" fill="#8C9BA5" font-size="15" font-weight="500" text-anchor="middle">EN (English)</text>
    </g>

    <!-- ================= 2. Theme Selection Cards ================= -->
    <g transform="translate(32, 292)" class="font-sans">
      <text x="0" y="16" fill="#8C9BA5" font-size="14" font-weight="700">Тема приложения</text>

      <!-- 3 Cards: Темная (selected), Светлая, Системная -->
      <!-- Темная (Selected) -->
      <rect x="0" y="30" width="208" height="58" rx="14" fill="#1677FF" fill-opacity="0.18" stroke="#1677FF" stroke-width="1.5"/>
      <text x="104" y="65" fill="#1677FF" font-size="15" font-weight="800" text-anchor="middle">🌙 Темная</text>

      <!-- Светлая -->
      <rect x="224" y="30" width="208" height="58" rx="14" fill="#1F293B" fill-opacity="0.45"/>
      <text x="328" y="65" fill="#8C9BA5" font-size="15" font-weight="500" text-anchor="middle">☀️ Светлая</text>

      <!-- Системная -->
      <rect x="448" y="30" width="208" height="58" rx="14" fill="#1F293B" fill-opacity="0.45"/>
      <text x="552" y="65" fill="#8C9BA5" font-size="15" font-weight="500" text-anchor="middle">⚙️ Система</text>
    </g>

    <!-- ================= 3. SSH Tunnel Lifecycle Policy ================= -->
    <g transform="translate(32, 410)" class="font-sans">
      <rect width="656" height="136" rx="16" fill="#1F293B" fill-opacity="0.4" stroke="#253247" stroke-width="1.2"/>
      <text x="20" y="32" fill="#FFFFFF" font-size="15" font-weight="700">Режим закрытия туннеля</text>
      <text x="20" y="52" fill="#8C9BA5" font-size="13">Управляет поведением SSH-соединения при навигации</text>

      <!-- Dropdown Field -->
      <rect x="20" y="68" width="616" height="48" rx="10" fill="#161E2C" stroke="#1677FF" stroke-width="1.2"/>
      <text x="36" y="98" fill="#1677FF" font-size="15" font-weight="700">Закрывать при выходе из приложения</text>
      <text x="604" y="98" fill="#1677FF" font-size="14">▾</text>
    </g>

    <!-- ================= 4. WebDAV Cloud Sync Preview ================= -->
    <g transform="translate(32, 574)" class="font-sans">
      <rect width="656" height="236" rx="16" fill="#1F293B" fill-opacity="0.4" stroke="#253247" stroke-width="1.2"/>
      <text x="20" y="34" fill="#FFFFFF" font-size="15" font-weight="700">Синхронизация с WebDAV (Яндекс Диск / Nextcloud)</text>
      <text x="20" y="54" fill="#8C9BA5" font-size="13">Резервная копия зашифрована и сохраняется в облако</text>

      <rect x="20" y="74" width="616" height="46" rx="10" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
      <text x="36" y="102" class="font-mono" fill="#FFFFFF" font-size="13">https://webdav.yandex.ru/backup.json</text>

      <!-- WebDAV Buttons -->
      <g transform="translate(20, 142)">
        <rect width="300" height="48" rx="12" fill="#1677FF"/>
        <text x="150" y="30" fill="#FFFFFF" font-size="15" font-weight="700" text-anchor="middle">☁️ Выгрузить на WebDAV</text>

        <rect x="316" y="0" width="300" height="48" rx="12" fill="#161E2C" stroke="#1677FF" stroke-width="1.2"/>
        <text x="466" y="30" fill="#1677FF" font-size="15" font-weight="700" text-anchor="middle">⬇️ Восстановить</text>
      </g>
    </g>

    <!-- ================= 5. App Version & Updates Card ================= -->
    <g transform="translate(32, 836)" class="font-sans">
      <rect width="656" height="156" rx="16" fill="#1F293B" fill-opacity="0.4" stroke="#253247" stroke-width="1.2"/>
      
      <circle cx="34" cy="34" r="14" fill="#1677FF" fill-opacity="0.18"/>
      <path d="M28,34 L33,39 L41,27" stroke="#1677FF" stroke-width="2.2" fill="none" stroke-linecap="round"/>
      <text x="58" y="39" fill="#FFFFFF" font-size="18" font-weight="800">3x manager • Версия v1.1.0</text>
      <text x="58" y="60" fill="#52C41A" font-size="13" font-weight="600">У вас установлена актуальная версия (v1.1.0)</text>

      <g transform="translate(20, 84)">
        <!-- Outlined Changelog Button -->
        <rect width="300" height="48" rx="12" fill="#161E2C" stroke="#253247" stroke-width="1.2"/>
        <text x="150" y="30" fill="#FFFFFF" font-size="14" font-weight="600" text-anchor="middle">История изменений</text>

        <!-- Filled Check Updates Button -->
        <rect x="316" y="0" width="300" height="48" rx="12" fill="#1677FF"/>
        <text x="466" y="30" fill="#FFFFFF" font-size="14" font-weight="700" text-anchor="middle">⟳ Проверить обновления</text>
      </g>
    </g>
  </g>
`;

const screens = [
  { name: 'screen_main', content: screenMainContent },
  { name: 'screen_webpanel', content: screenWebpanelContent },
  { name: 'screen_tabs_drawer', content: screenTabsDrawerContent },
  { name: 'screen_settings', content: screenSettingsContent },
];

for (const s of screens) {
  const svgXml = createPhoneBase(s.content);
  const svgPath = path.join(outDir, `${s.name}.svg`);
  const pngPath = path.join(outDir, `${s.name}.png`);

  fs.writeFileSync(svgPath, svgXml, 'utf-8');
  console.log(`Saved SVG: ${svgPath}`);

  // Render to high-resolution PNG using @resvg/resvg-js
  const resvg = new Resvg(svgXml, {
    fitTo: {
      mode: 'width',
      value: 800,
    },
    font: {
      loadSystemFonts: true,
      defaultFontFamily: 'sans-serif',
    },
  });

  const pngData = resvg.render().asPng();
  fs.writeFileSync(pngPath, pngData);
  console.log(`Rendered PNG: ${pngPath} (${pngData.length} bytes)`);
}

console.log('All screenshots generated successfully!');
