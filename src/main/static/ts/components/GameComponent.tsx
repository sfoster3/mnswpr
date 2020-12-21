import * as React from "react";
import {
  ActionResponse,
  Cell,
  CellTypes,
  ResultTypes,
  ServerApi,
} from "../utils/ServerApi";
import Coordinate from "../utils/Coordinate";

interface GameComponentProps {
  gameId: string;
  width: number;
  height: number;
  count: number;

  exit(): void;
}

class Board extends Map<string, Cell> {}

class Stats extends Map<string, number> {}

const initialState: {
  board: Board;
  started: boolean;
  count: number;
  lastIndex: number;
  result: ResultTypes;
  stats: Stats;
  autoPlay: boolean;
} = {
  board: new Map<string, Cell>(),
  started: false,
  count: 0,
  lastIndex: 0,
  result: ResultTypes.None,
  stats: new Map<string, number>(),
  autoPlay: false,
};
type State = Readonly<typeof initialState>;

export class GameComponent extends React.Component<GameComponentProps, State> {
  state: State = initialState;

  private static mapKey(co: Coordinate): string {
    return `${co.x}|${co.y}`;
  }

  private static unKey(key: string): Coordinate {
    const keyParts = key.split("|").map((s) => parseInt(s));
    return new Coordinate(keyParts[0], keyParts[1]);
  }

  clickNumber(co: Coordinate): Promise<ResultTypes> {
    const { result } = this.state;
    if (result == ResultTypes.None) {
      const idx: number = +Date.now();
      const { gameId } = this.props;
      return ServerApi.revealAdj(gameId, co).then(
        this.handleResponse.bind(this, idx)
      );
    } else {
      return Promise.resolve(result);
    }
  }

  clickUnknown(
    event: React.MouseEvent<HTMLDivElement>,
    co: Coordinate
  ): Promise<ResultTypes> {
    const { started, result } = this.state;
    if (result == ResultTypes.None) {
      const idx: number = +Date.now();
      const { gameId } = this.props;
      const alt: boolean = event.altKey || event.shiftKey || event.ctrlKey;
      if (started && !alt) {
        return ServerApi.flag(gameId, co).then(
          this.handleResponse.bind(this, idx)
        );
      } else {
        return ServerApi.reveal(gameId, co).then(
          this.handleResponse.bind(this, idx)
        );
      }
    } else {
      return Promise.resolve(result);
    }
  }

  clickFlag(co: Coordinate): Promise<ResultTypes> {
    const { result } = this.state;
    if (result == ResultTypes.None) {
      const idx: number = +Date.now();
      const { gameId } = this.props;
      return ServerApi.flag(gameId, co).then(
        this.handleResponse.bind(this, idx)
      );
    } else {
      return Promise.resolve(result);
    }
  }

  clickSolve(): Promise<ResultTypes> {
    const { result } = this.state;
    if (result == ResultTypes.None) {
      const idx: number = +Date.now();
      const { gameId } = this.props;
      return ServerApi.solve(gameId).then(this.handleResponse.bind(this, idx));
    } else {
      return Promise.resolve(result);
    }
  }

  toggleAutoPlay(): void {
    const { autoPlay } = this.state;
    if (autoPlay) {
      this.setState({ autoPlay: false });
    } else {
      this.setState({ autoPlay: true }, () => this.autoStepFlag());
    }
  }

  shouldAutoPlay(): boolean {
    return this.state.autoPlay;
  }

  autoStepFlag(helped = false): void {
    if (!this.shouldAutoPlay()) return;
    const { stats } = this.state;

    // Go off hints
    const choices = Array.from(stats.entries())
      .sort(([, v1], [, v2]) => v1 - v2)
      .filter((c) => c[1] >= 1);
    if (choices.length) {
      const coord = GameComponent.unKey(choices[0][0]);
      this.clickFlag(coord).then(() => this.autoStepFlag());
      return;
    } else if (!helped) {
      this.autoStepNumber();
    } else {
      this.toggleAutoPlay();
    }
  }

  autoStepNumber(): void {
    if (!this.shouldAutoPlay()) return;
    const { board } = this.state;
    const { width, height } = this.props;

    // Reduce flags
    const clear = Array.from(board)
      .filter((v) => Number.isInteger(v[1]) && v[1] > 0)
      .map(([key, cell]) => {
        const count: number = typeof cell === "number" ? cell : 0;
        const co = GameComponent.unKey(key);
        const adj = co.getAdj(width, height);
        const adjFlags = adj.filter(
          (a) => board.get(GameComponent.mapKey(a)) == CellTypes.Flag
        );
        const adjUk = adj.filter((a) => {
          const cell = board.get(GameComponent.mapKey(a));
          return cell === undefined || cell == CellTypes.Unknown;
        });
        return { co, rem: count - adjFlags.length, clear: adjUk.length };
      })
      .filter(({ rem, clear }) => rem == 0 && clear > 0);
    if (clear.length) {
      this.clickNumber(clear[0].co).then(() => this.autoStepNumber());
    } else {
      this.autoStepHelp();
    }
  }

  autoStepHelp(): void {
    if (!this.shouldAutoPlay()) return;
    this.clickSolve().then(() => this.autoStepFlag(true));
  }

  handleResponse(requestIndex: number, response: ActionResponse): ResultTypes {
    const {
      board: { cells, remainingCount: newCount, stats: newRawStats = null },
      result: newResult,
    } = response;
    const { lastIndex, board, count, result, stats, autoPlay } = this.state;
    let newState: State = {
      started: true,
      result: newResult,
      lastIndex,
      board,
      count,
      stats,
      autoPlay,
    };
    if (requestIndex > lastIndex && result == ResultTypes.None) {
      const newBoard = new Map<string, Cell>(
        cells.map((v) => [GameComponent.mapKey(v[0]), v[1]])
      );
      const newStats =
        newRawStats == null
          ? new Map<string, number>(
              Array.from(stats.entries()).filter((v) => !newBoard.has(v[0]))
            )
          : new Map<string, number>(
              newRawStats.map((v) => [GameComponent.mapKey(v[0]), v[1]])
            );
      newState = {
        ...newState,
        board: newBoard,
        count: newCount,
        lastIndex: requestIndex,
        ...(Boolean(newStats) && { stats: newStats }),
      };
    }
    this.setState(newState);
    return newResult;
  }

  renderCell(cell: Cell, co: Coordinate, stat = 0): React.ReactFragment {
    if (typeof cell === "number") {
      return (
        <div
          className={`cell cell-${cell}`}
          onClick={() => this.clickNumber(co)}
        >
          {cell || ""}
        </div>
      );
    } else if (cell === CellTypes.Flag) {
      return (
        <div className="cell cell-flagged" onClick={() => this.clickFlag(co)}>
          F
        </div>
      );
    } else if (cell === CellTypes.Unknown) {
      return (
        <div
          className="cell cell-unknown"
          onClick={(event) => this.clickUnknown(event, co)}
        >
          {Boolean(stat) && Math.round(stat * 10) / 10}
        </div>
      );
    } else if (cell === CellTypes.Mine) {
      return <div className="cell cell-mine">X</div>;
    }
  }

  renderBoard(): React.ReactFragment {
    const { width, height } = this.props;
    const { board, stats = new Map<string, number>() } = this.state;
    const rows: Array<React.ReactFragment> = [];
    for (let x = 0; x < width; x++) {
      const column: Array<React.ReactFragment> = [];
      for (let y = 0; y < height; y++) {
        const co = new Coordinate(x, y);
        const k = GameComponent.mapKey(co);
        const cell: Cell = board.has(k) ? board.get(k) : CellTypes.Unknown;
        const stat = (stats ? stats.get(k) : 0) || 0;
        column.push(<td>{this.renderCell(cell, co, stat)}</td>);
      }
      rows.push(<tr>{column}</tr>);
    }
    return rows;
  }

  render(): JSX.Element {
    const { exit, count } = this.props;
    const { count: currentCount, started, result, autoPlay } = this.state;
    const remaining = started ? currentCount : count;
    return (
      <div>
        <div className="container">
          <div className="navbar fixed-top navbar-light">
            <div>
              {result == ResultTypes.None && (
                <span>{remaining} mines remaining</span>
              )}
              {result == ResultTypes.Loss && <span>You lose!</span>}
              {result == ResultTypes.Win && <span>You win!</span>}
            </div>
            <div className="form-group form-inline">
              <button className="form-control" onClick={exit}>
                New Game
              </button>
              <button
                className="form-control"
                onClick={() => this.clickSolve()}
              >
                Help!
              </button>
              <button
                className="form-control"
                onClick={() => this.toggleAutoPlay()}
              >
                {autoPlay ? "Stop " : "Start "}
                AutoPlay
              </button>
            </div>
          </div>
          <div className="content">
            <table className="board">
              <tbody>{this.renderBoard()}</tbody>
            </table>
          </div>
        </div>
      </div>
    );
  }
}
