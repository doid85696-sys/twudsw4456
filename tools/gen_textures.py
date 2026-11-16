# python3 tools/gen_textures.py  (нужен Pillow). Детерминированный шум: тот же алгоритм в 3D-превью.
from PIL import Image
import math, os
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src/main/resources/assets/hollowflayer/textures/entity")
os.makedirs(OUT, exist_ok=True)
M = 0xFFFFFFFF
def H(x, y, s):
    n = (x * 374761393 + y * 668265263 + s * 2147483647) & M
    n = ((n ^ (n >> 13)) * 1274126177) & M
    n = n ^ (n >> 16)
    return n / 4294967295.0
def vn(x, y, sc, s):
    gx = x / sc; gy = y / sc; ix = math.floor(gx); iy = math.floor(gy)
    fx = (gx - ix); fx = fx * fx * (3 - 2 * fx); fy = (gy - iy); fy = fy * fy * (3 - 2 * fy)
    a = H(ix, iy, s); b = H(ix + 1, iy, s); c = H(ix, iy + 1, s); d = H(ix + 1, iy + 1, s)
    top = a + (b - a) * fx
    return top + ((c + (d - c) * fx) - top) * fy
REG = [(0,0,32,13,'f'),(0,16,32,27,'f'),(32,0,52,22,'f'),(24,32,32,38,'f'),(52,0,56,5,'b'),(56,0,60,6,'c'),
       (52,8,64,10,'r'),(52,14,56,18,'r'),(56,14,60,17,'b'),(52,18,64,32,'b'),(32,22,52,24,'f'),(32,25,52,32,'f'),
       (0,32,24,64,'f'),(32,32,64,47,'f'),(32,47,44,61,'f'),(48,47,60,61,'f')]
cl = lambda v: int(max(0, min(255, v)))
def color(kind, t, x, y):
    n = (H(x, y, 1) - 0.5) * 28; q = H(x, y, 2); lo = vn(x, y, 6, 3); fi = vn(x, y, 2, 4)
    if t == 'b' or (t == 'r' and kind == 'raw'):
        col = [205 + n, 195 + n, 170 + n]
        if lo < .35: col = [col[0] - 25, col[1] - 30, col[2] - 35]
        if vn(x, y, 3, 8) > .80: col = [95, 75, 55]
    elif t == 'c': col = [35 + n / 2, 25 + n / 2, 25 + n / 2]
    elif kind == 'raw':
        fib = 25 if (x * 7) % 5 == 0 else 0
        col = [95 + lo * 55 + n + fib, 8 + lo * 16 + n / 3 + fib / 2, 14 + lo * 10 + n / 3 + fib / 2]
        if H(x, 0, 5) > .9: col = [205 - lo * 20 + n / 2, 172 - lo * 20 + n / 2, 158 + n / 2]
        if lo < .22: col = [48 + n / 3, 0, 5]
        if q < .03: col = [185, 55, 55]
    else:
        col = [150 - lo * 14 + n * .7, 144 - lo * 14 + n * .7, 134 - lo * 10 + n * .7]
        if t == 'r': col = [v + 8 for v in col]
        if vn(x, y, 5, 6) > .74: col = [112 + n / 2, 100 + n / 2, 126 + n / 2]
        if H(x, 0, 7) > .93 and fi > .35: col = [78, 88, 122]
        if q < .02: col = [60, 52, 48]
    return col
def make(kind):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0)); px = img.load()
    def P(x, y, r, g, b): px[x, y] = (r, g, b, 255)
    for x0, y0, x1, y1, t in REG:
        for x in range(x0, x1):
            for y in range(y0, y1):
                col = color(kind, t, x, y)
                if x == x0 or x == x1 - 1 or y == y0 or y == y1 - 1: col = [v * .82 for v in col]
                P(x, y, cl(col[0]), cl(col[1]), cl(col[2]))
    if kind == 'raw':
        for i in range(40):
            x = 32 + int(H(i, 0, 10) * 20); y0 = int(H(i, 1, 10) * 14); L = 3 + int(H(i, 2, 10) * 6)
            for k in range(L):
                if y0 + k < 22: P(x, y0 + k, 45, 0, 4)
    else:
        for y in (8, 12, 16):
            for x in range(32, 52, 2): P(x, y, 30, 20, 20)
        for i in range(12): P(2 + i % 9, 34 + i, 80, 40, 40); P(14 + i % 9, 50 + i, 80, 40, 40)
        for k in range(5): P(34 + k, 36 + k, 30, 20, 20); P(38 - k, 36 + k, 30, 20, 20)
    for x in range(8, 16):
        for y in range(8, 13):
            if kind == 'raw': P(x, y, 25, 5, 5)
            else: P(x, y, 40, 36, 36)
    for xs in ((9, 10), (13, 14)):
        for x in xs:
            for y in (10, 11, 12): P(x, y, 0, 0, 0)
    for x in range(8, 16):
        for y in range(24, 27):
            if x % 2 == 0: P(x, y, 225, 220, 200)
            else: P(x, y, 20, 0, 0)
    return img
make("skin").save(os.path.join(OUT, "flayer.png"))
make("raw").save(os.path.join(OUT, "flayer_raw.png"))
e = Image.new("RGBA", (64, 64), (0, 0, 0, 0)); p = e.load()
for xy in ((10, 11), (10, 12), (13, 11), (13, 12), (9, 11), (14, 11)): p[xy] = (255, 40, 20, 255)
e.save(os.path.join(OUT, "flayer_eyes.png")); print("textures ok")
