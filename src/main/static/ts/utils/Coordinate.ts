class Coordinate {
  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  x: number;
  y: number;

  getAdj(width: number, height: number): Array<Coordinate> {
    const arr = [];
    for (const dx of [-1, 0, 1]) {
      for (const dy of [-1, 0, 1]) {
        if (dx !== 0 || dy !== 0) {
          const x = this.x + dx;
          const y = this.y + dy;
          if (x >= 0 && x < width && y >= 0 && y < height)
            arr.push(new Coordinate(x, y));
        }
      }
    }
    return arr;
  }
}

export default Coordinate;
