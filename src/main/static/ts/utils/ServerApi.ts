import * as requestPromise from "request-promise-native";
import Coordinate from "./Coordinate";

export interface GameArgs {
  width: number;
  height: number;
  count: number;
}

export interface NewGame {
  gameId: string;
}

export enum CellTypes {
  Flag = "F",
  Unknown = "U",
  Mine = "X",
}

export enum ResultTypes {
  None = "N",
  Win = "W",
  Loss = "L",
}

export type Cell = CellTypes | number;

export interface ActionResponse {
  board: BoardState;
  result: ResultTypes;
}

export interface BoardState {
  cells: Array<readonly [Coordinate, Cell]>;
  remainingCount: number;
  stats: Array<readonly [Coordinate, number]> | null;
}

enum CoordinateAction {
  reveal = "reveal",
  revealAdj = "revealAdj",
  flag = "flag",
}

const baseUrl: string = location.origin;

/**
 * Static methods for interacting with the APIs
 */
export class ServerApi {
  public static createGame(gameArgs: GameArgs): Promise<NewGame> {
    return requestPromise
      .post("/api/v1/board", { body: gameArgs, json: true, baseUrl })
      .then((o: NewGame) => o);
  }

  private static _coordinateAction(
    action: CoordinateAction,
    gameId: string,
    co: Coordinate
  ): Promise<ActionResponse> {
    return requestPromise.post(`/api/v1/board/${gameId}/${action}`, {
      body: co,
      json: true,
      baseUrl,
    });
  }

  public static reveal(
    gameId: string,
    co: Coordinate
  ): Promise<ActionResponse> {
    return this._coordinateAction(CoordinateAction.reveal, gameId, co);
  }

  public static revealAdj(
    gameId: string,
    co: Coordinate
  ): Promise<ActionResponse> {
    return this._coordinateAction(CoordinateAction.revealAdj, gameId, co);
  }

  public static flag(gameId: string, co: Coordinate): Promise<ActionResponse> {
    return this._coordinateAction(CoordinateAction.flag, gameId, co);
  }

  public static solve(gameId: string): Promise<ActionResponse> {
    return requestPromise.get(`/api/v1/board/${gameId}/solve`, {
      json: true,
      baseUrl,
    });
  }
}
