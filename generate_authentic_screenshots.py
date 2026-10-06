import os

os.makedirs("docs/screenshots", exist_ok=True)

# Exact colors from Theme.kt / Color.kt
BG_DARK = "#0E141E"
SURFACE_DARK = "#161E2C"
SURFACE_VARIANT = "#1F293B"
BORDER_DARK = "#253247"
PRIMARY_BLUE = "#1677FF"
GREEN_MINT = "#52C41A"
AMBER_WARN = "#FAAD14"
RED_DANGER = "#FF4D4F"
TEXT_PRIMARY = "#FFFFFF"
TEXT_SECONDARY = "#8C9BA5"
BADGE_BG = "#3B3E54"

# 1. screen_main.svg - Authentic Main Servers List
svg_main = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1520" width="720" height="1520">
  <defs>
    <style>
      .sf-title {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 28px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .sf-h2 {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 21px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .sf-sub {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 15px; font-weight: 400; fill: {TEXT_SECONDARY}; }}
      .sf-mono {{ font-family: 'SF Mono', Menlo, Consolas, Monaco, monospace; font-size: 15px; fill: {TEXT_SECONDARY}; }}
      .sf-btn {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 16px; font-weight: 600; fill: {TEXT_PRIMARY}; }}
      .sf-badge {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 13px; font-weight: 700; }}
    </style>
  </defs>

  <!-- Background -->
  <rect width="720" height="1520" fill="{BG_DARK}"/>

  <!-- Status Bar -->
  <rect width="720" height="52" fill="{BG_DARK}"/>
  <text x="40" y="36" fill="{TEXT_PRIMARY}" font-family="-apple-system, sans-serif" font-size="20" font-weight="600">09:41</text>
  <!-- Wi-Fi & Battery -->
  <g transform="translate(620, 18)">
    <path d="M0,16 L4,16 L4,20 L0,20 Z M6,12 L10,12 L10,20 L6,20 Z M12,8 L16,8 L16,20 L12,20 Z M18,4 L22,4 L22,20 L18,20 Z" fill="{TEXT_PRIMARY}"/>
    <rect x="32" y="5" width="28" height="15" rx="3.5" fill="none" stroke="{TEXT_PRIMARY}" stroke-width="2"/>
    <rect x="35" y="8" width="18" height="9" rx="2" fill="{GREEN_MINT}"/>
  </g>

  <!-- TopAppBar (Compose TopAppBar surface) -->
  <rect y="52" width="720" height="96" fill="{SURFACE_DARK}"/>
  <line x1="0" y1="148" x2="720" y2="148" stroke="{BORDER_DARK}" stroke-width="1"/>
  
  <text x="32" y="112" class="sf-title" letter-spacing="-0.5">3x manager</text>

  <!-- TopAppBar Actions -->
  <!-- 1. Open Tabs Button with Badge -->
  <g transform="translate(365, 78)">
    <rect width="48" height="48" rx="14" fill="{SURFACE_VARIANT}"/>
    <!-- Layers Icon -->
    <path d="M24,14 L12,20 L24,26 L36,20 Z M12,25 L24,31 L36,25 M12,30 L24,36 L36,30" fill="none" stroke="{PRIMARY_BLUE}" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
    <!-- Badge -->
    <rect x="32" y="-2" width="22" height="20" rx="10" fill="{PRIMARY_BLUE}"/>
    <text x="43" y="13" fill="#FFFFFF" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">2</text>
  </g>

  <!-- 2. Tunnel Quick Disconnect Pill -->
  <g transform="translate(425, 78)">
    <rect width="92" height="48" rx="14" fill="{RED_DANGER}" fill-opacity="0.15" stroke="{RED_DANGER}" stroke-opacity="0.45" stroke-width="1.2"/>
    <circle cx="442" cy="102" r="4.5" fill="{GREEN_MINT}"/>
    <!-- Power Icon -->
    <path d="M459,96 L459,102 M455,98 A6,6 0 1,0 463,98" fill="none" stroke="{RED_DANGER}" stroke-width="2" stroke-linecap="round"/>
    <text x="488" y="108" fill="{RED_DANGER}" class="sf-badge" text-anchor="middle">СТОП</text>
  </g>

  <!-- 3. Update Badge (Placed LEFT of eye icon) -->
  <g transform="translate(525, 78)">
    <rect width="84" height="48" rx="14" fill="{PRIMARY_BLUE}" fill-opacity="0.18" stroke="{PRIMARY_BLUE}" stroke-opacity="0.65" stroke-width="1.2"/>
    <text x="567" y="108" fill="{PRIMARY_BLUE}" class="sf-badge" text-anchor="middle">☁ v1.1.0</text>
  </g>

  <!-- 4. Eye Mask Icon Button -->
  <g transform="translate(618, 78)">
    <circle cx="24" cy="24" r="22" fill="{SURFACE_VARIANT}"/>
    <path d="M14,24 C17,17 31,17 34,24 C31,31 17,31 14,24 Z" fill="none" stroke="{PRIMARY_BLUE}" stroke-width="2.2"/>
    <circle cx="24" cy="24" r="3.5" fill="{PRIMARY_BLUE}"/>
    <line x1="13" y1="13" x2="35" y2="35" stroke="{PRIMARY_BLUE}" stroke-width="2.2" stroke-linecap="round"/>
  </g>

  <!-- 5. Settings Gear Icon Button -->
  <g transform="translate(668, 78)">
    <circle cx="24" cy="24" r="22" fill="{SURFACE_VARIANT}"/>
    <circle cx="24" cy="24" r="7" fill="none" stroke="{TEXT_SECONDARY}" stroke-width="2.5"/>
    <path d="M24,13 L24,15 M24,33 L24,35 M13,24 L15,24 M33,24 L35,24" stroke="{TEXT_SECONDARY}" stroke-width="3" stroke-linecap="round"/>
  </g>

  <!-- Sort Row (header_sort) -->
  <g transform="translate(32, 172)">
    <text x="0" y="24" class="sf-sub">Всего серверов: 3</text>
    
    <!-- Sort pills on right -->
    <!-- [По частоте] (Selected) -->
    <rect x="280" y="0" width="115" height="34" rx="8" fill="{PRIMARY_BLUE}" fill-opacity="0.18" stroke="{PRIMARY_BLUE}" stroke-width="1"/>
    <text x="337" y="22" fill="{PRIMARY_BLUE}" class="sf-badge" text-anchor="middle">По частоте</text>

    <!-- [По алфавиту] -->
    <rect x="405" y="0" width="125" height="34" rx="8" fill="{SURFACE_VARIANT}" fill-opacity="0.45"/>
    <text x="467" y="22" fill="{TEXT_SECONDARY}" class="sf-sub" font-size="13" text-anchor="middle">По алфавиту</text>

    <!-- [Новые] -->
    <rect x="540" y="0" width="115" height="34" rx="8" fill="{SURFACE_VARIANT}" fill-opacity="0.45"/>
    <text x="597" y="22" fill="{TEXT_SECONDARY}" class="sf-sub" font-size="13" text-anchor="middle">По дате</text>
  </g>

  <!-- Active Tunnel Banner (item active_tunnel_banner) -->
  <g transform="translate(32, 224)">
    <rect width="656" height="58" rx="12" fill="{RED_DANGER}" fill-opacity="0.08" stroke="{RED_DANGER}" stroke-opacity="0.35" stroke-width="1"/>
    <circle cx="26" cy="29" r="4.5" fill="{GREEN_MINT}"/>
    <text x="44" y="35" fill="{TEXT_PRIMARY}" font-family="sans-serif" font-size="16" font-weight="600">Туннель: Frankfurt Node-01</text>
    
    <!-- Disconnect button inside banner -->
    <g transform="translate(524, 12)">
      <rect width="118" height="34" rx="8" fill="{RED_DANGER}"/>
      <text x="583" y="23" fill="#FFFFFF" font-family="sans-serif" font-size="13" font-weight="bold" text-anchor="middle">Отключить</text>
    </g>
  </g>

  <!-- ================= SERVER CARD 1 (Selected & Tunnel Active) ================= -->
  <g transform="translate(32, 302)">
    <!-- Card Container -->
    <rect width="656" height="236" rx="18" fill="{SURFACE_DARK}" stroke="{PRIMARY_BLUE}" stroke-width="2"/>
    
    <!-- Top Row: TermiusBadgeL, Name, Status Badge -->
    <!-- TermiusBadgeL (34x34 in #3B3E54 with white 'L') -->
    <rect x="20" y="20" width="38" height="38" rx="8" fill="{BADGE_BG}"/>
    <text x="39" y="46" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="900" text-anchor="middle">L</text>

    <!-- Name & effective URL -->
    <text x="70" y="38" class="sf-h2">Frankfurt Node-01 (Core)</text>
    <text x="70" y="58" class="sf-mono" fill="{TEXT_SECONDARY}">SSH: Frankfurt Node-01 (localhost:2053)</text>

    <!-- Status Badge (Top Right) -->
    <g transform="translate(480, 20)">
      <rect width="156" height="32" rx="8" fill="{GREEN_MINT}" fill-opacity="0.15"/>
      <text x="558" y="42" fill="{GREEN_MINT}" class="sf-badge" text-anchor="middle">Туннель активен</text>
    </g>

    <!-- Route summary surface -->
    <g transform="translate(20, 78)">
      <rect width="616" height="42" rx="8" fill="{SURFACE_VARIANT}" fill-opacity="0.5"/>
      <text x="16" y="26" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="13" font-weight="700">SSH ТУННЕЛЬ</text>
      <!-- Arrow ➔ -->
      <path d="M125,22 L140,22 M135,17 L140,22 L135,27" stroke="{TEXT_SECONDARY}" stroke-width="2" stroke-linecap="round"/>
      <text x="155" y="26" class="sf-mono" fill="{TEXT_SECONDARY}">root@203.0.113.***</text>
    </g>

    <!-- Bottom Action Row -->
    <!-- Left: Button [ ➔ Открыть ] -->
    <g transform="translate(20, 142)">
      <rect width="140" height="48" rx="10" fill="{PRIMARY_BLUE}"/>
      <!-- Login Icon -->
      <path d="M22,24 L36,24 M30,18 L36,24 L30,30" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
      <rect x="16" y="16" width="6" height="16" rx="2" fill="#FFFFFF"/>
      <text x="78" y="30" fill="#FFFFFF" class="sf-btn">Открыть</text>
    </g>

    <!-- Right: Edit & Delete icon buttons -->
    <g transform="translate(540, 146)">
      <!-- Edit Icon -->
      <circle cx="20" cy="20" r="18" fill="{SURFACE_VARIANT}"/>
      <path d="M13,27 L25,15 L28,18 L16,30 L11,31 Z" fill="none" stroke="{TEXT_SECONDARY}" stroke-width="1.8"/>

      <!-- Delete Icon -->
      <circle cx="68" cy="20" r="18" fill="{SURFACE_VARIANT}"/>
      <path d="M62,15 L74,15 M64,15 L64,27 M72,15 L72,27 M61,12 L75,12" stroke="{RED_DANGER}" stroke-width="2" stroke-linecap="round"/>
    </g>
  </g>

  <!-- ================= SERVER CARD 2 (Amsterdam) ================= -->
  <g transform="translate(32, 558)">
    <rect width="656" height="236" rx="18" fill="{SURFACE_DARK}" stroke="{BORDER_DARK}" stroke-width="1"/>
    
    <!-- TermiusBadgeL -->
    <rect x="20" y="20" width="38" height="38" rx="8" fill="{BADGE_BG}"/>
    <text x="39" y="46" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="900" text-anchor="middle">L</text>

    <text x="70" y="38" class="sf-h2">Amsterdam Edge-02</text>
    <text x="70" y="58" class="sf-mono" fill="{TEXT_SECONDARY}">SSH: Amsterdam Edge-02 (localhost:2087)</text>

    <!-- Status Badge (Auto-forward) -->
    <g transform="translate(500, 20)">
      <rect width="136" height="32" rx="8" fill="{AMBER_WARN}" fill-opacity="0.15"/>
      <text x="568" y="42" fill="{AMBER_WARN}" class="sf-badge" text-anchor="middle">Авто-проброс</text>
    </g>

    <!-- Route summary surface -->
    <g transform="translate(20, 78)">
      <rect width="616" height="42" rx="8" fill="{SURFACE_VARIANT}" fill-opacity="0.5"/>
      <text x="16" y="26" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="13" font-weight="700">SSH ТУННЕЛЬ</text>
      <path d="M125,22 L140,22 M135,17 L140,22 L135,27" stroke="{TEXT_SECONDARY}" stroke-width="2" stroke-linecap="round"/>
      <text x="155" y="26" class="sf-mono" fill="{TEXT_SECONDARY}">admin@198.51.100.***</text>
    </g>

    <!-- Bottom Action Row -->
    <g transform="translate(20, 142)">
      <rect width="140" height="48" rx="10" fill="{PRIMARY_BLUE}"/>
      <path d="M22,24 L36,24 M30,18 L36,24 L30,30" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
      <rect x="16" y="16" width="6" height="16" rx="2" fill="#FFFFFF"/>
      <text x="78" y="30" fill="#FFFFFF" class="sf-btn">Открыть</text>
    </g>

    <g transform="translate(540, 146)">
      <circle cx="20" cy="20" r="18" fill="{SURFACE_VARIANT}"/>
      <path d="M13,27 L25,15 L28,18 L16,30 L11,31 Z" fill="none" stroke="{TEXT_SECONDARY}" stroke-width="1.8"/>

      <circle cx="68" cy="20" r="18" fill="{SURFACE_VARIANT}"/>
      <path d="M62,15 L74,15 M64,15 L64,27 M72,15 L72,27 M61,12 L75,12" stroke="{RED_DANGER}" stroke-width="2" stroke-linecap="round"/>
    </g>
  </g>

  <!-- ================= SERVER CARD 3 (Tokyo) ================= -->
  <g transform="translate(32, 814)">
    <rect width="656" height="236" rx="18" fill="{SURFACE_DARK}" stroke="{BORDER_DARK}" stroke-width="1"/>
    
    <rect x="20" y="20" width="38" height="38" rx="8" fill="{BADGE_BG}"/>
    <text x="39" y="46" fill="#FFFFFF" font-family="sans-serif" font-size="20" font-weight="900" text-anchor="middle">L</text>

    <text x="70" y="38" class="sf-h2">Tokyo Relay-03</text>
    <text x="70" y="58" class="sf-mono" fill="{TEXT_SECONDARY}">SSH: Tokyo Relay-03 (localhost:2096)</text>

    <g transform="translate(500, 20)">
      <rect width="136" height="32" rx="8" fill="{AMBER_WARN}" fill-opacity="0.15"/>
      <text x="568" y="42" fill="{AMBER_WARN}" class="sf-badge" text-anchor="middle">Авто-проброс</text>
    </g>

    <g transform="translate(20, 78)">
      <rect width="616" height="42" rx="8" fill="{SURFACE_VARIANT}" fill-opacity="0.5"/>
      <text x="16" y="26" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="13" font-weight="700">SSH ТУННЕЛЬ</text>
      <path d="M125,22 L140,22 M135,17 L140,22 L135,27" stroke="{TEXT_SECONDARY}" stroke-width="2" stroke-linecap="round"/>
      <text x="155" y="26" class="sf-mono" fill="{TEXT_SECONDARY}">deploy@192.0.2.***</text>
    </g>

    <g transform="translate(20, 142)">
      <rect width="140" height="48" rx="10" fill="{PRIMARY_BLUE}"/>
      <path d="M22,24 L36,24 M30,18 L36,24 L30,30" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round"/>
      <rect x="16" y="16" width="6" height="16" rx="2" fill="#FFFFFF"/>
      <text x="78" y="30" fill="#FFFFFF" class="sf-btn">Открыть</text>
    </g>

    <g transform="translate(540, 146)">
      <circle cx="20" cy="20" r="18" fill="{SURFACE_VARIANT}"/>
      <path d="M13,27 L25,15 L28,18 L16,30 L11,31 Z" fill="none" stroke="{TEXT_SECONDARY}" stroke-width="1.8"/>

      <circle cx="68" cy="20" r="18" fill="{SURFACE_VARIANT}"/>
      <path d="M62,15 L74,15 M64,15 L64,27 M72,15 L72,27 M61,12 L75,12" stroke="{RED_DANGER}" stroke-width="2" stroke-linecap="round"/>
    </g>
  </g>

  <!-- Floating Action Button (FAB) -->
  <g transform="translate(608, 1380)">
    <rect width="68" height="68" rx="20" fill="{PRIMARY_BLUE}" filter="drop-shadow(0px 8px 16px rgba(22,119,255,0.4))"/>
    <!-- Plus Icon -->
    <path d="M34,22 L34,46 M22,34 L46,34" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round"/>
  </g>
</svg>"""

# 2. screen_webpanel.svg - Authentic 3x-ui Embedded Web Configurator
svg_webpanel = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1520" width="720" height="1520">
  <defs>
    <style>
      .tb-title {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 20px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .tb-sub {{ font-family: 'SF Mono', Menlo, monospace; font-size: 13px; fill: {GREEN_MINT}; }}
      .p-title {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 18px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .p-sub {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 13px; fill: {TEXT_SECONDARY}; }}
      .p-val {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 24px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .t-th {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 14px; font-weight: 600; fill: {TEXT_SECONDARY}; }}
      .t-td {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 15px; fill: {TEXT_PRIMARY}; }}
    </style>
  </defs>

  <rect width="720" height="1520" fill="{BG_DARK}"/>

  <!-- Status Bar -->
  <rect width="720" height="52" fill="{SURFACE_DARK}"/>
  <text x="40" y="36" fill="{TEXT_PRIMARY}" font-family="-apple-system, sans-serif" font-size="20" font-weight="600">09:41</text>
  <g transform="translate(620, 18)">
    <path d="M0,16 L4,16 L4,20 L0,20 Z M6,12 L10,12 L10,20 L6,20 Z M12,8 L16,8 L16,20 L12,20 Z M18,4 L22,4 L22,20 L18,20 Z" fill="{TEXT_PRIMARY}"/>
    <rect x="32" y="5" width="28" height="15" rx="3.5" fill="none" stroke="{TEXT_PRIMARY}" stroke-width="2"/>
    <rect x="35" y="8" width="18" height="9" rx="2" fill="{GREEN_MINT}"/>
  </g>

  <!-- Android Top Bar in WebPanelScreen (height ~56dp) -->
  <rect y="52" width="720" height="68" fill="{SURFACE_DARK}"/>
  <line x1="0" y1="120" x2="720" y2="120" stroke="{BORDER_DARK}" stroke-width="1"/>

  <!-- Back Button (ArrowBack) -->
  <g transform="translate(18, 68)">
    <path d="M22,18 L10,18 M16,12 L10,18 L16,24" stroke="{TEXT_PRIMARY}" stroke-width="2.5" stroke-linecap="round"/>
  </g>

  <!-- Server Name & Tunnel Status Dot -->
  <g transform="translate(64, 68)">
    <circle cx="6" cy="12" r="5" fill="{GREEN_MINT}"/>
    <text x="20" y="16" class="tb-title">Frankfurt Node-01</text>
    <text x="20" y="34" class="tb-sub">Туннель ➔ Frankfurt Node-01</text>
  </g>

  <!-- Right Actions: Tabs [2], Desktop Mode [🖥 ПК], Refresh [⟳] -->
  <g transform="translate(480, 64)">
    <!-- Tabs Badge Button -->
    <rect width="44" height="44" rx="12" fill="{SURFACE_VARIANT}"/>
    <path d="M22,12 L12,17 L22,22 L32,17 Z M12,21 L22,26 L32,21 M12,25 L22,30 L32,25" fill="none" stroke="{PRIMARY_BLUE}" stroke-width="2" stroke-linecap="round"/>
    <rect x="30" y="-3" width="18" height="16" rx="8" fill="{PRIMARY_BLUE}"/>
    <text x="39" y="9" fill="#FFFFFF" font-family="sans-serif" font-size="10" font-weight="bold" text-anchor="middle">2</text>
  </g>

  <g transform="translate(536, 64)">
    <rect width="112" height="44" rx="12" fill="{PRIMARY_BLUE}" fill-opacity="0.18" stroke="{PRIMARY_BLUE}" stroke-width="1.2"/>
    <text x="592" y="92" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">🖥 ПК 1280px</text>
  </g>

  <g transform="translate(660, 64)">
    <rect width="44" height="44" rx="12" fill="{SURFACE_VARIANT}"/>
    <path d="M22,12 A10,10 0 1,1 14,20 M14,14 L14,20 L20,20" fill="none" stroke="{TEXT_PRIMARY}" stroke-width="2" stroke-linecap="round"/>
  </g>

  <!-- ================= EMBEDDED REAL 3X-UI WEB INTERFACE ================= -->
  <!-- 3x-ui Top Header (Ant Design dark header: #001529) -->
  <rect y="120" width="720" height="56" fill="#001529"/>
  <text x="24" y="156" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="20" font-weight="900" letter-spacing="1">3X-UI</text>
  <text x="96" y="156" fill="#FFFFFF" font-family="sans-serif" font-size="15" font-weight="600">v2.4.2</text>
  
  <text x="550" y="156" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="14">👤 admin</text>
  <text x="650" y="156" fill="{RED_DANGER}" font-family="sans-serif" font-size="14">Выход ↪</text>

  <!-- 3x-ui Submenu Tabs: [Статус] [Подключения] [Настройки Xray] [Логи] -->
  <g transform="translate(0, 176)">
    <rect width="720" height="46" fill="#141414"/>
    <text x="32" y="206" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="15" font-weight="bold">📊 Статус системы</text>
    <rect x="28" y="218" width="145" height="3" fill="{PRIMARY_BLUE}"/>
    
    <text x="210" y="206" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="15">👥 Подключения (Inbounds)</text>
    <text x="440" y="206" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="15">⚙️ Настройки Xray</text>
    <text x="630" y="206" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="15">📜 Логи</text>
  </g>

  <!-- System Overview Card -->
  <g transform="translate(24, 246)">
    <rect width="672" height="84" rx="12" fill="#1F1F1F" stroke="#303030" stroke-width="1"/>
    <text x="20" y="34" class="p-title">Ядро Xray: v1.8.24</text>
    <rect x="175" y="18" width="90" height="24" rx="4" fill="{GREEN_MINT}" fill-opacity="0.2"/>
    <text x="220" y="34" fill="{GREEN_MINT}" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">Работает</text>
    
    <text x="20" y="64" class="p-sub">ОС: Ubuntu 22.04.4 LTS (GNU/Linux 5.15.0-x86_64) • Время работы: 48 дн.</text>
  </g>

  <!-- 4 Ant Design Metrics Cards -->
  <g transform="translate(24, 348)">
    <!-- CPU Card -->
    <rect width="326" height="136" rx="12" fill="#1F1F1F" stroke="#303030" stroke-width="1"/>
    <text x="20" y="32" class="p-sub">Нагрузка ЦП (CPU)</text>
    <text x="20" y="72" class="p-val" fill="{GREEN_MINT}">14.2%</text>
    <text x="110" y="72" class="p-sub">4 vCPU</text>
    <rect x="20" y="96" width="286" height="8" rx="4" fill="#303030"/>
    <rect x="20" y="96" width="41" height="8" rx="4" fill="{GREEN_MINT}"/>

    <!-- RAM Card -->
    <rect x="346" width="326" height="136" rx="12" fill="#1F1F1F" stroke="#303030" stroke-width="1"/>
    <text x="366" y="32" class="p-sub">Оперативная память (RAM)</text>
    <text x="366" y="72" class="p-val" fill="{PRIMARY_BLUE}">1.22 GB</text>
    <text x="470" y="72" class="p-sub">/ 4.00 GB (30%)</text>
    <rect x="366" y="96" width="286" height="8" rx="4" fill="#303030"/>
    <rect x="366" y="96" width="86" height="8" rx="4" fill="{PRIMARY_BLUE}"/>
  </g>

  <g transform="translate(24, 500)">
    <!-- Disk Card -->
    <rect width="326" height="136" rx="12" fill="#1F1F1F" stroke="#303030" stroke-width="1"/>
    <text x="20" y="32" class="p-sub">Диск (NVMe Storage)</text>
    <text x="20" y="72" class="p-val" fill="#FFFFFF">12.4 GB</text>
    <text x="120" y="72" class="p-sub">/ 50.0 GB (24%)</text>
    <rect x="20" y="96" width="286" height="8" rx="4" fill="#303030"/>
    <rect x="20" y="96" width="70" height="8" rx="4" fill="{PRIMARY_BLUE}"/>

    <!-- Traffic Card -->
    <rect x="346" width="326" height="136" rx="12" fill="#1F1F1F" stroke="#303030" stroke-width="1"/>
    <text x="366" y="32" class="p-sub">Суммарный трафик</text>
    <text x="366" y="72" class="p-val" fill="#FFFFFF">3.60 TB</text>
    <text x="366" y="104" class="p-sub" fill="{TEXT_SECONDARY}">↓ 1.48 TB  •  ↑ 2.12 TB</text>
  </g>

  <!-- Inbounds Table Card -->
  <g transform="translate(24, 656)">
    <rect width="672" height="820" rx="14" fill="#1F1F1F" stroke="#303030" stroke-width="1"/>
    
    <!-- Table Header Toolbar -->
    <text x="24" y="42" class="p-title">Список подключений (Inbounds)</text>
    <rect x="490" y="16" width="158" height="38" rx="8" fill="{PRIMARY_BLUE}"/>
    <text x="569" y="41" fill="#FFFFFF" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">+ Добавить</text>

    <!-- Table Header Row -->
    <rect y="70" width="672" height="42" fill="#262626"/>
    <text x="24" y="96" class="t-th">ID</text>
    <text x="70" y="96" class="t-th">Протокол</text>
    <text x="180" y="96" class="t-th">Название</text>
    <text x="360" y="96" class="t-th">Порт</text>
    <text x="440" y="96" class="t-th">Трафик</text>
    <text x="560" y="96" class="t-th">Статус</text>

    <!-- Row 1: VLESS Reality -->
    <g transform="translate(0, 114)">
      <text x="24" y="38" class="t-td">1</text>
      <rect x="68" y="18" width="60" height="24" rx="4" fill="{PRIMARY_BLUE}" fill-opacity="0.2"/>
      <text x="98" y="34" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">vless</text>
      <text x="180" y="30" class="t-td" font-weight="bold">VLESS-Reality-443</text>
      <text x="180" y="48" class="p-sub">Vision • TCP • www.microsoft.com</text>
      <text x="360" y="38" class="sf-mono" fill="#FFFFFF">443</text>
      <text x="440" y="38" class="t-td">842 GB</text>
      <rect x="555" y="20" width="80" height="26" rx="4" fill="{GREEN_MINT}" fill-opacity="0.2"/>
      <text x="595" y="37" fill="{GREEN_MINT}" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">Включен</text>
      <line x1="0" y1="68" x2="672" y2="68" stroke="#2B2B2B" stroke-width="1"/>
    </g>

    <!-- Row 2: Shadowsocks 2022 -->
    <g transform="translate(0, 184)">
      <text x="24" y="38" class="t-td">2</text>
      <rect x="68" y="18" width="80" height="24" rx="4" fill="#722ED1" fill-opacity="0.2"/>
      <text x="108" y="34" fill="#B37FEB" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">shadowsocks</text>
      <text x="180" y="30" class="t-td" font-weight="bold">SS-2022-Fast</text>
      <text x="180" y="48" class="p-sub">2022-blake3-aes-128-gcm</text>
      <text x="360" y="38" class="sf-mono" fill="#FFFFFF">2053</text>
      <text x="440" y="38" class="t-td">120 GB</text>
      <rect x="555" y="20" width="80" height="26" rx="4" fill="{GREEN_MINT}" fill-opacity="0.2"/>
      <text x="595" y="37" fill="{GREEN_MINT}" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">Включен</text>
      <line x1="0" y1="68" x2="672" y2="68" stroke="#2B2B2B" stroke-width="1"/>
    </g>

    <!-- Row 3: VMess WebSocket -->
    <g transform="translate(0, 254)">
      <text x="24" y="38" class="t-td">3</text>
      <rect x="68" y="18" width="60" height="24" rx="4" fill="{AMBER_WARN}" fill-opacity="0.2"/>
      <text x="98" y="34" fill="{AMBER_WARN}" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">vmess</text>
      <text x="180" y="30" class="t-td" font-weight="bold">VMess-WS-CDN</text>
      <text x="180" y="48" class="p-sub">ws • path: /vmess-ws • Cloudflare</text>
      <text x="360" y="38" class="sf-mono" fill="#FFFFFF">2083</text>
      <text x="440" y="38" class="t-td">315 GB</text>
      <rect x="555" y="20" width="80" height="26" rx="4" fill="{GREEN_MINT}" fill-opacity="0.2"/>
      <text x="595" y="37" fill="{GREEN_MINT}" font-family="sans-serif" font-size="12" font-weight="bold" text-anchor="middle">Включен</text>
      <line x1="0" y1="68" x2="672" y2="68" stroke="#2B2B2B" stroke-width="1"/>
    </g>
  </g>
</svg>"""

# 3. screen_tabs_drawer.svg - Authentic RightTabsDrawer
svg_drawer = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1520" width="720" height="1520">
  <defs>
    <style>
      .d-title {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 20px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .d-sub {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 13px; fill: {TEXT_SECONDARY}; }}
      .d-card-title {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 17px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .d-mono {{ font-family: 'SF Mono', Menlo, monospace; font-size: 12px; fill: {TEXT_SECONDARY}; }}
    </style>
  </defs>

  <!-- Dimmed Scrim Overlay (Black 55% over Main Screen) -->
  <rect width="720" height="1520" fill="#000000" opacity="0.65"/>

  <!-- Dimmed glimpse of left background items -->
  <g opacity="0.15">
    <rect x="20" y="80" width="200" height="40" fill="#FFFFFF"/>
    <rect x="20" y="160" width="220" height="180" rx="16" fill="{SURFACE_DARK}"/>
    <rect x="20" y="360" width="220" height="180" rx="16" fill="{SURFACE_DARK}"/>
  </g>

  <!-- Right-Side Sliding Panel (Surface width 560 on right, rounded topStart/bottomStart 20dp) -->
  <g transform="translate(160, 0)">
    <rect width="560" height="1520" rx="24" fill="{SURFACE_DARK}" stroke="{BORDER_DARK}" stroke-width="1.5"/>

    <!-- Drawer Header -->
    <g transform="translate(24, 68)">
      <!-- Cyan box with Layers Icon -->
      <rect width="44" height="44" rx="12" fill="{PRIMARY_BLUE}" fill-opacity="0.15"/>
      <path d="M22,12 L12,17 L22,22 L32,17 Z M12,21 L22,26 L32,21 M12,25 L22,30 L32,25" fill="none" stroke="{PRIMARY_BLUE}" stroke-width="2.2" stroke-linecap="round"/>

      <!-- Title & Count -->
      <text x="56" y="22" class="d-title">Открытые панели</text>
      <text x="56" y="40" class="d-sub">Открыто вкладок: 2</text>

      <!-- Close Drawer (✕) Button on right -->
      <g transform="translate(460, 6)">
        <circle cx="16" cy="16" r="16" fill="{SURFACE_VARIANT}"/>
        <path d="M10,10 L22,22 M22,10 L10,22" stroke="{TEXT_SECONDARY}" stroke-width="2.2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- Main Menu / All Servers Button -->
    <g transform="translate(24, 138)">
      <rect width="512" height="52" rx="12" fill="{SURFACE_VARIANT}" fill-opacity="0.5"/>
      <!-- Dns Icon -->
      <rect x="18" y="15" width="22" height="10" rx="2" fill="none" stroke="{PRIMARY_BLUE}" stroke-width="2"/>
      <rect x="18" y="27" width="22" height="10" rx="2" fill="none" stroke="{PRIMARY_BLUE}" stroke-width="2"/>
      <circle cx="23" cy="20" r="1.5" fill="{PRIMARY_BLUE}"/>
      <circle cx="23" cy="32" r="1.5" fill="{PRIMARY_BLUE}"/>
      
      <text x="52" y="32" fill="{TEXT_PRIMARY}" font-family="sans-serif" font-size="16" font-weight="600">Главное меню (Все сервера)</text>
    </g>

    <!-- Divider Line -->
    <line x1="24" y1="208" x2="536" y2="208" stroke="{BORDER_DARK}" stroke-width="1"/>

    <!-- ================= TAB 1: Current Server (Selected) ================= -->
    <g transform="translate(24, 226)">
      <!-- Container with Cyan 1.5dp border and tint -->
      <rect width="512" height="104" rx="14" fill="{PRIMARY_BLUE}" fill-opacity="0.15" stroke="{PRIMARY_BLUE}" stroke-width="1.5"/>

      <!-- Status Dot (Green) -->
      <circle cx="24" cy="36" r="5" fill="{GREEN_MINT}"/>

      <!-- Server Title -->
      <text x="38" y="40" class="d-card-title">Frankfurt Node-01</text>
      
      <!-- Status Badge "Активна" -->
      <rect x="220" y="22" width="76" height="24" rx="6" fill="{GREEN_MINT}" fill-opacity="0.2"/>
      <text x="258" y="38" fill="{GREEN_MINT}" font-family="sans-serif" font-size="11" font-weight="bold" text-anchor="middle">Активна</text>

      <!-- URL -->
      <text x="38" y="68" class="d-mono">https://203.0.113.***:2053</text>
      <text x="38" y="86" class="d-sub" font-size="11" fill="{PRIMARY_BLUE}">Туннель активен ➔ localhost:2053</text>

      <!-- Close Tab (✕) Icon Button -->
      <g transform="translate(456, 32)">
        <circle cx="18" cy="18" r="18" fill="{SURFACE_VARIANT}"/>
        <path d="M12,12 L24,24 M24,12 L12,24" stroke="{TEXT_SECONDARY}" stroke-width="2.2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- ================= TAB 2: Background Server ================= -->
    <g transform="translate(24, 346)">
      <!-- Container -->
      <rect width="512" height="104" rx="14" fill="{SURFACE_VARIANT}" fill-opacity="0.35" stroke="{BORDER_DARK}" stroke-width="1"/>

      <!-- Status Dot (Cyan/Blue) -->
      <circle cx="24" cy="36" r="5" fill="{PRIMARY_BLUE}"/>

      <!-- Server Title -->
      <text x="38" y="40" class="d-card-title">Amsterdam Edge-02</text>
      
      <!-- Status Badge "В фоне" -->
      <rect x="220" y="22" width="70" height="24" rx="6" fill="{AMBER_WARN}" fill-opacity="0.2"/>
      <text x="255" y="38" fill="{AMBER_WARN}" font-family="sans-serif" font-size="11" font-weight="bold" text-anchor="middle">В фоне</text>

      <!-- URL -->
      <text x="38" y="68" class="d-mono">https://198.51.100.***:2087</text>
      <text x="38" y="86" class="d-sub" font-size="11">SSH туннель выключен</text>

      <!-- Close Tab (✕) Icon Button -->
      <g transform="translate(456, 32)">
        <circle cx="18" cy="18" r="18" fill="{SURFACE_VARIANT}"/>
        <path d="M12,12 L24,24 M24,12 L12,24" stroke="{TEXT_SECONDARY}" stroke-width="2.2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- Bottom Actions Section (OutlinedButton 'Закрыть все') -->
    <g transform="translate(24, 1410)">
      <rect width="512" height="54" rx="12" fill="none" stroke="{RED_DANGER}" stroke-width="1.5"/>
      <!-- LayersClear Icon -->
      <path d="M185,27 L198,18 L211,27 M185,33 L198,24 L211,33" stroke="{RED_DANGER}" stroke-width="2" fill="none" stroke-linecap="round"/>
      <line x1="180" y1="18" x2="216" y2="36" stroke="{RED_DANGER}" stroke-width="2"/>
      <text x="255" y="34" fill="{RED_DANGER}" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">Закрыть все</text>
    </g>
  </g>
</svg>"""

# 4. screen_settings.svg - Authentic SettingsDialog with M3 Tabs & Cross
svg_settings = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 1520" width="720" height="1520">
  <defs>
    <style>
      .set-title {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 22px; font-weight: 700; fill: {TEXT_PRIMARY}; }}
      .set-lbl {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 14px; font-weight: 600; fill: {TEXT_SECONDARY}; }}
      .set-txt {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 15px; fill: {TEXT_PRIMARY}; }}
      .set-desc {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 12px; fill: {TEXT_SECONDARY}; }}
    </style>
  </defs>

  <!-- Dimmed Scrim Overlay -->
  <rect width="720" height="1520" fill="#000000" opacity="0.7"/>

  <!-- Centered Floating AlertDialog Surface -->
  <g transform="translate(24, 140)">
    <rect width="672" height="1240" rx="24" fill="{SURFACE_DARK}" stroke="{BORDER_DARK}" stroke-width="1.5" filter="drop-shadow(0px 16px 32px rgba(0,0,0,0.5))"/>

    <!-- Title Bar with Top-Right Close Cross (✕) -->
    <g transform="translate(28, 36)">
      <!-- Settings Icon -->
      <circle cx="16" cy="16" r="14" fill="{PRIMARY_BLUE}" fill-opacity="0.18"/>
      <circle cx="16" cy="16" r="5" fill="none" stroke="{PRIMARY_BLUE}" stroke-width="2"/>
      <text x="42" y="24" class="set-title">Настройки</text>

      <!-- Top-Right Close Cross (✕) Button -->
      <g transform="translate(584, 0)">
        <circle cx="16" cy="16" r="16" fill="{SURFACE_VARIANT}"/>
        <path d="M10,10 L22,22 M22,10 L10,22" stroke="{TEXT_SECONDARY}" stroke-width="2.2" stroke-linecap="round"/>
      </g>
    </g>

    <!-- TabRow: [⚙️ Общие] | [💾 Резервная копия] -->
    <g transform="translate(28, 86)">
      <rect width="616" height="48" fill="{SURFACE_DARK}"/>
      
      <!-- Tab 0: Общие (Selected) -->
      <text x="154" y="28" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">⚙️ Общие</text>
      <!-- Bottom blue indicator line -->
      <rect x="20" y="44" width="268" height="3" fill="{PRIMARY_BLUE}"/>

      <!-- Tab 1: Резервная копия (Unselected) -->
      <text x="462" y="28" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="16" font-weight="normal" text-anchor="middle">💾 Резервная копия</text>
      <line x1="0" y1="46" x2="616" y2="46" stroke="{BORDER_DARK}" stroke-width="1"/>
    </g>

    <!-- ================= 1. Language Selection (Cards) ================= -->
    <g transform="translate(28, 160)">
      <text x="0" y="16" class="set-lbl">Язык оформления</text>

      <!-- 3 Cards: [Auto], [RU] (selected), [EN] -->
      <!-- Auto -->
      <rect x="0" y="28" width="195" height="58" rx="12" fill="{SURFACE_VARIANT}" fill-opacity="0.4"/>
      <text x="97" y="62" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="15" text-anchor="middle">Auto</text>

      <!-- RU (Selected) -->
      <rect x="210" y="28" width="195" height="58" rx="12" fill="{PRIMARY_BLUE}" fill-opacity="0.18" stroke="{PRIMARY_BLUE}" stroke-width="1.5"/>
      <text x="307" y="62" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">RU (Русский)</text>

      <!-- EN -->
      <rect x="420" y="28" width="195" height="58" rx="12" fill="{SURFACE_VARIANT}" fill-opacity="0.4"/>
      <text x="517" y="62" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="15" text-anchor="middle">EN (English)</text>
    </g>

    <!-- ================= 2. Theme Selection (Cards) ================= -->
    <g transform="translate(28, 276)">
      <text x="0" y="16" class="set-lbl">Тема приложения</text>

      <!-- 3 Cards: [Темная] (selected), [Светлая], [Системная] -->
      <!-- Темная (Selected) -->
      <rect x="0" y="28" width="195" height="58" rx="12" fill="{PRIMARY_BLUE}" fill-opacity="0.18" stroke="{PRIMARY_BLUE}" stroke-width="1.5"/>
      <text x="97" y="62" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="16" font-weight="bold" text-anchor="middle">🌙 Темная</text>

      <!-- Светлая -->
      <rect x="210" y="28" width="195" height="58" rx="12" fill="{SURFACE_VARIANT}" fill-opacity="0.4"/>
      <text x="307" y="62" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="15" text-anchor="middle">☀️ Светлая</text>

      <!-- Системная -->
      <rect x="420" y="28" width="195" height="58" rx="12" fill="{SURFACE_VARIANT}" fill-opacity="0.4"/>
      <text x="517" y="62" fill="{TEXT_SECONDARY}" font-family="sans-serif" font-size="15" text-anchor="middle">⚙️ Система</text>
    </g>

    <!-- ================= 3. Tunnel Lifecycle Policy Card ================= -->
    <g transform="translate(28, 392)">
      <rect width="616" height="136" rx="14" fill="{SURFACE_VARIANT}" fill-opacity="0.35" stroke="{BORDER_DARK}" stroke-width="1"/>
      <text x="18" y="32" class="set-lbl" fill="{TEXT_PRIMARY}">Режим закрытия туннеля</text>
      <text x="18" y="52" class="set-desc">Управляет поведением SSH-соединения при выходе из панели</text>

      <!-- Dropdown Row inside Card -->
      <rect x="18" y="70" width="580" height="48" rx="10" fill="{SURFACE_DARK}" stroke="{PRIMARY_BLUE}" stroke-width="1.2"/>
      <text x="36" y="100" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="15" font-weight="bold">Закрывать при выходе из приложения</text>
      <text x="560" y="100" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="14">▾</text>
    </g>

    <!-- ================= 4. WebDAV & Cloud Backup Preview ================= -->
    <g transform="translate(28, 552)">
      <rect width="616" height="240" rx="14" fill="{SURFACE_VARIANT}" fill-opacity="0.35" stroke="{BORDER_DARK}" stroke-width="1"/>
      <!-- WebDAV Header -->
      <text x="18" y="34" class="set-lbl" fill="{TEXT_PRIMARY}">Синхронизация с WebDAV (Яндекс Диск / Nextcloud)</text>
      <text x="18" y="54" class="set-desc">Резервная копия зашифрована и сохраняется в облако</text>

      <!-- URL Input Box -->
      <rect x="18" y="72" width="580" height="46" rx="10" fill="{SURFACE_DARK}" stroke="{BORDER_DARK}" stroke-width="1"/>
      <text x="32" y="100" fill="{TEXT_PRIMARY}" font-family="monospace" font-size="14">https://webdav.yandex.ru/backup.json</text>

      <!-- Action Buttons -->
      <g transform="translate(18, 140)">
        <!-- Upload Button -->
        <rect width="280" height="48" rx="10" fill="{PRIMARY_BLUE}"/>
        <text x="140" y="29" fill="#FFFFFF" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">☁️ Выгрузить на WebDAV</text>

        <!-- Download Button -->
        <g transform="translate(300, 0)">
          <rect width="280" height="48" rx="10" fill="{SURFACE_DARK}" stroke="{PRIMARY_BLUE}" stroke-width="1.2"/>
          <text x="140" y="29" fill="{PRIMARY_BLUE}" font-family="sans-serif" font-size="15" font-weight="bold" text-anchor="middle">⬇️ Восстановить</text>
        </g>
      </g>
    </g>

    <!-- ================= 5. App Version & GitHub Updates Card ================= -->
    <g transform="translate(28, 816)">
      <rect width="616" height="156" rx="14" fill="{SURFACE_VARIANT}" fill-opacity="0.35" stroke="{BORDER_DARK}" stroke-width="1"/>
      
      <!-- Version Title -->
      <circle cx="32" cy="34" r="14" fill="{PRIMARY_BLUE}" fill-opacity="0.18"/>
      <path d="M26,34 L32,40 L40,28" stroke="{PRIMARY_BLUE}" stroke-width="2.2" fill="none" stroke-linecap="round"/>
      <text x="56" y="39" class="set-title" font-size="19">3x manager • Версия v1.1.0</text>
      <text x="56" y="60" class="set-desc" fill="{GREEN_MINT}">У вас установлена последняя версия (v1.1.0)</text>

      <!-- Two Buttons: Changelog & Check Updates -->
      <g transform="translate(18, 84)">
        <!-- OutlinedButton Changelog -->
        <rect width="280" height="48" rx="10" fill="{SURFACE_DARK}" stroke="{BORDER_DARK}" stroke-width="1"/>
        <text x="140" y="30" fill="{TEXT_PRIMARY}" font-family="sans-serif" font-size="14" font-weight="600" text-anchor="middle">История изменений</text>

        <!-- Button Check Updates -->
        <g transform="translate(300, 0)">
          <rect width="280" height="48" rx="10" fill="{PRIMARY_BLUE}"/>
          <text x="140" y="30" fill="#FFFFFF" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">⟳ Проверить обновления</text>
        </g>
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
    print(f"Written authentic mockup {path}")

