import *  as requestPromise from "request-promise-native";

export interface GameArgs {
    width: number
    height: number
    count: number
}

export interface NewGame {
    gameId: string
}

interface Coordinate {
    x: number
    y: number
}

export enum CellTypes {
    Flag = "F", Unknown = "U"
}

export type Cell = CellTypes | number

export interface BoardResponse {
    cells: Array<readonly [Coordinate, Cell]>
    remainingCount: number
}

export interface LossResponse {
    cells: Array<readonly [Coordinate, Cell]>
    remainingCount: number
}

enum CoordinateAction {
    reveal = 'reveal',
    revealAdj = 'revealAdj',
    flag = 'flag'
}

const baseUrl: string = location.origin;

/**
 * Static methods for interacting with the APIs
 */
export class ServerApi {
    public static createGame(gameArgs: GameArgs): Promise<NewGame> {
        return requestPromise.post('/api/v1/board', {body: gameArgs, json: true, baseUrl})
            .then((o: NewGame) => o);
    }

    private static _coordinateAction(action: CoordinateAction, gameId: string, co: Coordinate): Promise<BoardResponse | LossResponse> {
        return requestPromise.post(`/api/v1/board/${gameId}/${action}`, {body: co, json: true, baseUrl});
    }

    public static reveal(gameId: string, co: Coordinate): Promise<BoardResponse | LossResponse> {
        return this._coordinateAction(CoordinateAction.reveal, gameId, co);
    }

    public static revealAdj(gameId: string, co: Coordinate): Promise<BoardResponse | LossResponse> {
        return this._coordinateAction(CoordinateAction.revealAdj, gameId, co);
    }

    public static flag(gameId: string, co: Coordinate): Promise<BoardResponse | LossResponse> {
        return this._coordinateAction(CoordinateAction.flag, gameId, co);
    }
}