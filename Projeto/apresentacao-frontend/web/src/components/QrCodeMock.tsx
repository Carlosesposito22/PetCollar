/**
 * QR Code visual fake — apenas para demonstração.
 * Em produção seria substituído pelo BR Code real renderizado a partir do payload PIX.
 */
export function QrCodeMock({ seed, size = 200, color = "#02aab5" }: {
  seed: string;
  size?: number;
  color?: string;
}) {
  const cells = 21;
  const cellSize = Math.floor(size / cells);
  const actualSize = cells * cellSize;
  const random = mulberry32(hashString(seed));
  const pattern: boolean[][] = Array.from({ length: cells }, () =>
    Array.from({ length: cells }, () => random() > 0.5)
  );
  drawFinder(pattern, 0, 0);
  drawFinder(pattern, cells - 7, 0);
  drawFinder(pattern, 0, cells - 7);

  return (
    <svg width={actualSize} height={actualSize} viewBox={`0 0 ${actualSize} ${actualSize}`} aria-label="QR Code de pagamento (mock)">
      <rect width={actualSize} height={actualSize} fill="#ffffff" />
      {pattern.flatMap((row, y) =>
        row.map((on, x) =>
          on ? (
            <rect
              key={`${x}-${y}`}
              x={x * cellSize}
              y={y * cellSize}
              width={cellSize}
              height={cellSize}
              fill={color}
            />
          ) : null
        )
      )}
    </svg>
  );
}

function drawFinder(grid: boolean[][], ox: number, oy: number) {
  for (let y = 0; y < 7; y++) {
    for (let x = 0; x < 7; x++) {
      const borda = x === 0 || x === 6 || y === 0 || y === 6;
      const meio = x >= 2 && x <= 4 && y >= 2 && y <= 4;
      grid[oy + y][ox + x] = borda || meio;
    }
  }
}
function hashString(s: string) {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
  return h >>> 0;
}
function mulberry32(seed: number) {
  let a = seed;
  return function () {
    a |= 0;
    a = (a + 0x6d2b79f5) | 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}
