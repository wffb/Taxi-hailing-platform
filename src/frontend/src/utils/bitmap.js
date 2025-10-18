export function windowsToBitmap(windows) {
    const bits = Array(48).fill(0);
    for (const w of windows) {
      const [sh, sm] = w.start.split(":").map(Number);
      const [eh, em] = w.end.split(":").map(Number);
      const startIdx = sh * 2 + (sm >= 30 ? 1 : 0);
      const endIdx = eh * 2 + (em >= 30 ? 1 : 0);
      for (let i = startIdx; i < endIdx; i++) bits[i] = 1;
    }
    return bits.join(""); // 48 bits
  }