import * as React from "react";
import {ActionResponse, Cell, CellTypes, ServerApi} from "../utils/ServerApi";

interface GameComponentProps {
    gameId: string
    width: number
    height: number
    count: number

    exit(): any
}

interface Coordinate {
    x: number
    y: number
}

class Board extends Map<string, Cell> {
}


const initialState: { board: Board, started: boolean, count: number, lastIndex: number, loss: boolean } = {
    board: new Map<string, Cell>(),
    started: false,
    count: 0,
    lastIndex: 0,
    loss: false
};
type State = Readonly<typeof initialState>;

export class GameComponent extends React.Component<GameComponentProps, State> {
    state: State = initialState;

    private mapKey(co: Coordinate): string {
        return `${co.x}|${co.y}`;
    }

    clickNumber(co: Coordinate) {
        const idx: number = +Date.now();
        const {gameId} = this.props;
        ServerApi.revealAdj(gameId, co).then(this.handleResponse.bind(this, idx));
    }

    clickUnknown(event: React.MouseEvent<HTMLDivElement>, co: Coordinate) {
        const idx: number = +Date.now();
        const {started} = this.state;
        const {gameId} = this.props;
        const alt: boolean = event.altKey || event.shiftKey || event.ctrlKey;
        if (started && !alt) {
            ServerApi.flag(gameId, co).then(this.handleResponse.bind(this, idx));
        } else {
            ServerApi.reveal(gameId, co).then(this.handleResponse.bind(this, idx));
        }
    }

    clickFlag(co: Coordinate) {
        const idx: number = +Date.now();
        const {gameId} = this.props;
        ServerApi.flag(gameId, co).then(this.handleResponse.bind(this, idx));
    }

    handleResponse(requestIndex: number, response: ActionResponse) {
        const {board: {cells, remainingCount: newCount}, isLoss} = response;
        const {lastIndex, board, count, loss} = this.state;
        let newState: State = {started: true, loss: isLoss, lastIndex, board, count};
        if (requestIndex > lastIndex && !loss) {
            const newBoard = new Map<string, Cell>(cells.map(v => [this.mapKey(v[0]), v[1]]));
            newState = {...newState, board: newBoard, count: newCount, lastIndex: requestIndex};
        }
        this.setState(newState);
    }

    renderCell(cell: Cell, co: Coordinate): React.ReactFragment {
        if (typeof cell === "number") {
            return <div className={`cell cell-${cell}`} onClick={() => this.clickNumber(co)}>{cell || ''}</div>;
        } else if (cell === CellTypes.Flag) {
            return <div className="cell cell-flagged" onClick={() => this.clickFlag(co)}>F</div>
        } else if (cell === CellTypes.Unknown) {
            return <div className="cell cell-unknown" onClick={event => this.clickUnknown(event, co)}/>
        } else if (cell === CellTypes.Mine) {
            return <div className="cell cell-mine">X</div>
        }
    }

    renderBoard(): React.ReactFragment {
        const {width, height} = this.props;
        const {board} = this.state;
        const rows: Array<React.ReactFragment> = [];
        for (let x: number = 0; x < width; x++) {
            const column: Array<React.ReactFragment> = [];
            for (let y: number = 0; y < height; y++) {
                const k = this.mapKey({x, y});
                const cell: Cell = board.has(k) ? board.get(k) : CellTypes.Unknown;
                column.push(<td>{this.renderCell(cell, {x, y})}</td>);
            }
            rows.push(<tr>{column}</tr>);
        }
        return rows;
    }

    render() {
        const {exit, count} = this.props;
        const {count: currentCount, started, loss} = this.state;
        const remaining = started ? currentCount : count;
        return (
            <div>
                <div className="container">
                    <div className="navbar fixed-top navbar-light">
                        <div>
                            {loss ?
                                <span>You lose!</span> :
                                <span>{remaining} mines remaining</span>}

                        </div>
                        <div className="form-group">
                            <button className="form-control" onClick={exit}>New Game</button>
                        </div>
                    </div>
                    <div className="content">
                        <table className="board">
                            <tbody>
                            {this.renderBoard()}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>);
    }

}