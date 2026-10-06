import zlib
import struct
import os

def create_png(width, height, raw_rgba):
    # PNG signature
    png = b'\x89PNG\r\n\x1a\n'
    
    # IHDR chunk
    # width (4), height (4), bit depth (1), color type (6=RGBA), compression (0), filter (0), interlace (0)
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data)
    png += struct.pack('>I', len(ihdr_data)) + b'IHDR' + ihdr_data + struct.pack('>I', ihdr_crc)
    
    # IDAT chunk (scanlines with filter byte 0)
    scanlines = bytearray()
    row_stride = width * 4
    for y in range(height):
        scanlines.append(0) # Filter type 0 (None)
        start = y * row_stride
        scanlines.extend(raw_rgba[start:start + row_stride])
        
    compressed_idat = zlib.compress(bytes(scanlines), 9)
    idat_crc = zlib.crc32(b'IDAT' + compressed_idat)
    png += struct.pack('>I', len(compressed_idat)) + b'IDAT' + compressed_idat + struct.pack('>I', idat_crc)
    
    # IEND chunk
    iend_crc = zlib.crc32(b'IEND')
    png += struct.pack('>I', 0) + b'IEND' + struct.pack('>I', iend_crc)
    return png

W, H = 360, 720

def draw_rect(buf, x, y, w, h, r, g, b, a=255):
    for j in range(max(0, y), min(H, y + h)):
        for i in range(max(0, x), min(W, x + w)):
            idx = (j * W + i) * 4
            buf[idx] = r
            buf[idx+1] = g
            buf[idx+2] = b
            buf[idx+3] = a

def generate_screen_1():
    buf = bytearray(W * H * 4)
    # Background
    for y in range(H):
        for x in range(W):
            idx = (y * W + x) * 4
            buf[idx] = 15
            buf[idx+1] = 17
            buf[idx+2] = 26
            buf[idx+3] = 255
    # Top Bar
    draw_rect(buf, 0, 0, W, 50, 20, 23, 36)
    # Title cyan indicator
    draw_rect(buf, 16, 20, 100, 14, 255, 255, 255)
    # Badge
    draw_rect(buf, 180, 14, 24, 24, 0, 229, 255)
    # Update button
    draw_rect(buf, 210, 14, 52, 24, 0, 229, 255)
    # Eye
    draw_rect(buf, 270, 14, 24, 24, 0, 229, 255)
    # Settings
    draw_rect(buf, 310, 14, 24, 24, 156, 163, 175)

    # Active Tunnel Banner
    draw_rect(buf, 16, 62, W - 32, 44, 22, 35, 47)
    draw_rect(buf, 26, 74, 10, 10, 0, 230, 118)
    draw_rect(buf, 44, 72, 160, 10, 255, 255, 255)
    draw_rect(buf, 44, 86, 220, 8, 0, 230, 118)

    # Server Card 1 (Active)
    draw_rect(buf, 16, 118, W - 32, 110, 27, 30, 44)
    draw_rect(buf, 26, 130, 8, 8, 0, 230, 118)
    draw_rect(buf, 42, 128, 140, 12, 255, 255, 255)
    draw_rect(buf, 42, 148, 60, 16, 0, 229, 255)
    draw_rect(buf, 110, 148, 50, 16, 0, 230, 118)
    draw_rect(buf, 26, 175, 180, 8, 156, 163, 175)
    draw_rect(buf, 26, 190, 140, 8, 0, 229, 255)
    draw_rect(buf, W - 96, 168, 70, 32, 0, 229, 255)

    # Server Card 2
    draw_rect(buf, 16, 240, W - 32, 110, 27, 30, 44)
    draw_rect(buf, 26, 252, 8, 8, 107, 114, 128)
    draw_rect(buf, 42, 250, 130, 12, 255, 255, 255)
    draw_rect(buf, 42, 270, 60, 16, 156, 163, 175)
    draw_rect(buf, 110, 270, 50, 16, 0, 230, 118)
    draw_rect(buf, 26, 297, 180, 8, 156, 163, 175)
    draw_rect(buf, 26, 312, 140, 8, 0, 229, 255)
    draw_rect(buf, W - 96, 290, 70, 32, 34, 39, 56)

    # Server Card 3
    draw_rect(buf, 16, 362, W - 32, 110, 27, 30, 44)
    draw_rect(buf, 26, 374, 8, 8, 107, 114, 128)
    draw_rect(buf, 42, 372, 150, 12, 255, 255, 255)
    draw_rect(buf, 42, 392, 70, 16, 0, 229, 255)
    draw_rect(buf, 120, 392, 50, 16, 0, 230, 118)
    draw_rect(buf, 26, 419, 180, 8, 156, 163, 175)
    draw_rect(buf, 26, 434, 140, 8, 0, 229, 255)
    draw_rect(buf, W - 96, 412, 70, 32, 34, 39, 56)

    # FAB
    draw_rect(buf, W - 66, H - 70, 48, 48, 0, 229, 255)
    return create_png(W, H, buf)

def generate_screen_2():
    buf = bytearray(W * H * 4)
    # Background
    for y in range(H):
        for x in range(W):
            idx = (y * W + x) * 4
            buf[idx] = 14
            buf[idx+1] = 16
            buf[idx+2] = 23
            buf[idx+3] = 255
    # Header
    draw_rect(buf, 0, 0, W, 44, 24, 27, 40)
    draw_rect(buf, 14, 14, 16, 16, 255, 255, 255)
    draw_rect(buf, 40, 16, 100, 12, 255, 255, 255)
    draw_rect(buf, W - 110, 10, 48, 24, 0, 229, 255)
    draw_rect(buf, W - 50, 10, 24, 24, 34, 38, 56)

    # 3x-ui Navbar
    draw_rect(buf, 0, 44, W, 28, 0, 21, 41)
    draw_rect(buf, 12, 52, 90, 10, 0, 229, 255)

    # Metrics
    draw_rect(buf, 12, 80, 160, 80, 28, 31, 46)
    draw_rect(buf, 22, 92, 60, 8, 156, 163, 175)
    draw_rect(buf, 22, 110, 70, 20, 0, 230, 118)
    draw_rect(buf, 22, 140, 140, 8, 0, 230, 118)

    draw_rect(buf, 188, 80, 160, 80, 28, 31, 46)
    draw_rect(buf, 198, 92, 60, 8, 156, 163, 175)
    draw_rect(buf, 198, 110, 90, 20, 0, 229, 255)
    draw_rect(buf, 198, 140, 140, 8, 0, 229, 255)

    # Table
    draw_rect(buf, 12, 172, W - 24, 480, 24, 27, 40)
    draw_rect(buf, 22, 186, 120, 12, 255, 255, 255)
    draw_rect(buf, W - 80, 182, 60, 20, 0, 229, 255)
    draw_rect(buf, 12, 212, W - 24, 24, 32, 36, 54)

    # Table rows
    for r in range(4):
        y_pos = 246 + r * 50
        draw_rect(buf, 22, y_pos, 70, 10, 0, 229, 255)
        draw_rect(buf, 110, y_pos, 30, 10, 255, 255, 255)
        draw_rect(buf, 170, y_pos, 50, 10, 255, 255, 255)
        draw_rect(buf, 240, y_pos, 45, 16, 0, 230, 118)
        draw_rect(buf, W - 50, y_pos, 30, 10, 0, 229, 255)
        draw_rect(buf, 12, y_pos + 30, W - 24, 1, 37, 42, 61)

    return create_png(W, H, buf)

def generate_screen_3():
    buf = bytearray(W * H * 4)
    # Dimmed bg
    for y in range(H):
        for x in range(W):
            idx = (y * W + x) * 4
            buf[idx] = 8
            buf[idx+1] = 9
            buf[idx+2] = 14
            buf[idx+3] = 255
    # Right Drawer
    drawer_x = 70
    draw_rect(buf, drawer_x, 0, W - drawer_x, H, 22, 25, 38)
    # Header
    draw_rect(buf, drawer_x + 16, 36, 120, 14, 255, 255, 255)
    draw_rect(buf, drawer_x + 150, 32, 20, 20, 0, 229, 255)
    draw_rect(buf, W - 36, 32, 20, 20, 37, 42, 60)

    # Tab 1 (Active)
    draw_rect(buf, drawer_x + 12, 75, W - drawer_x - 24, 85, 28, 36, 51)
    draw_rect(buf, drawer_x + 22, 88, 8, 8, 0, 230, 118)
    draw_rect(buf, drawer_x + 36, 86, 110, 12, 255, 255, 255)
    draw_rect(buf, drawer_x + 36, 106, 50, 16, 0, 230, 118)
    draw_rect(buf, drawer_x + 22, 134, 140, 8, 156, 163, 175)
    draw_rect(buf, W - 40, 86, 18, 18, 255, 82, 82)

    # Tab 2
    draw_rect(buf, drawer_x + 12, 170, W - drawer_x - 24, 85, 26, 30, 43)
    draw_rect(buf, drawer_x + 22, 183, 8, 8, 255, 179, 0)
    draw_rect(buf, drawer_x + 36, 181, 100, 12, 255, 255, 255)
    draw_rect(buf, drawer_x + 36, 201, 45, 16, 255, 179, 0)
    draw_rect(buf, drawer_x + 22, 229, 140, 8, 156, 163, 175)
    draw_rect(buf, W - 40, 181, 18, 18, 42, 47, 66)

    # Bottom Actions
    draw_rect(buf, drawer_x + 12, H - 90, W - drawer_x - 24, 34, 255, 82, 82)
    draw_rect(buf, drawer_x + 12, H - 48, W - drawer_x - 24, 34, 0, 229, 255)

    return create_png(W, H, buf)

def generate_screen_4():
    buf = bytearray(W * H * 4)
    # Dimmed bg
    for y in range(H):
        for x in range(W):
            idx = (y * W + x) * 4
            buf[idx] = 8
            buf[idx+1] = 9
            buf[idx+2] = 14
            buf[idx+3] = 255
    # Dialog
    draw_rect(buf, 16, 50, W - 32, 620, 25, 28, 41)
    # Header & close
    draw_rect(buf, 32, 70, 90, 14, 255, 255, 255)
    draw_rect(buf, W - 52, 68, 20, 20, 38, 42, 60)

    # Tabs
    draw_rect(buf, 32, 100, 140, 26, 0, 229, 255)
    draw_rect(buf, 178, 100, 140, 26, 30, 34, 50)

    # Setting Card 1
    draw_rect(buf, 32, 138, W - 64, 80, 28, 32, 48)
    draw_rect(buf, 44, 150, 120, 10, 255, 255, 255)
    draw_rect(buf, 44, 180, W - 88, 26, 37, 42, 62)

    # Setting Card 2 (Languages)
    draw_rect(buf, 32, 228, W - 64, 70, 28, 32, 48)
    draw_rect(buf, 44, 240, 120, 10, 255, 255, 255)
    draw_rect(buf, 44, 260, 80, 26, 0, 229, 255)
    draw_rect(buf, 132, 260, 80, 26, 37, 42, 62)
    draw_rect(buf, 220, 260, 80, 26, 37, 42, 62)

    # Setting Card 3 (WebDAV)
    draw_rect(buf, 32, 308, W - 64, 120, 28, 32, 48)
    draw_rect(buf, 44, 320, 110, 10, 255, 255, 255)
    draw_rect(buf, 44, 340, W - 88, 26, 20, 23, 34)
    draw_rect(buf, 44, 380, 110, 28, 0, 229, 255)
    draw_rect(buf, 165, 380, 110, 28, 37, 42, 62)

    # Setting Card 4 (Updates)
    draw_rect(buf, 32, 438, W - 64, 85, 28, 32, 48)
    draw_rect(buf, 44, 450, 140, 10, 255, 255, 255)
    draw_rect(buf, 44, 480, 100, 26, 37, 42, 62)
    draw_rect(buf, 155, 480, 120, 26, 0, 229, 255)

    return create_png(W, H, buf)

with open("docs/screenshots/screen_main.png", "wb") as f:
    f.write(generate_screen_1())
with open("docs/screenshots/screen_webpanel.png", "wb") as f:
    f.write(generate_screen_2())
with open("docs/screenshots/screen_tabs_drawer.png", "wb") as f:
    f.write(generate_screen_3())
with open("docs/screenshots/screen_settings.png", "wb") as f:
    f.write(generate_screen_4())

print("Successfully created all 4 PNG mockups!")
