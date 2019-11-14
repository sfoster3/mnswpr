import * as React from "react";
import {BoardResponse, Cell, CellTypes, LossResponse, ServerApi} from "../utils/ServerApi";
import {ComponentUtils} from "../utils/ComponentUtils";

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


const initialState: { board: Board, loading: boolean, started: boolean } = {board: new Map<string, Cell>(), loading: false, started: false};
type State = Readonly<typeof initialState>;

export class GameComponent extends React.Component<GameComponentProps, State> {
    state: State = initialState;

    private mapKey(co: Coordinate): string {
        return `${co.x}|${co.y}`;
    }

    clickNumber(co: Coordinate) {
        const {gameId} = this.props;
        this.setState({loading: true}, () => {
            ServerApi.revealAdj(gameId, co).then(this.handleResponse.bind(this));
        });
    }

    clickUnknown(co: Coordinate) {
        const {started} = this.state;
        const {gameId} = this.props;
        this.setState({loading: true}, () => {
            if (started) {
                ServerApi.flag(gameId, co).then(this.handleResponse.bind(this));
            } else {
                ServerApi.reveal(gameId, co).then(this.handleResponse.bind(this));
            }
        });
    }

    clickFlag(co: Coordinate) {
        const {gameId} = this.props;
        this.setState({loading: true}, () => {
            ServerApi.flag(gameId, co).then(this.handleResponse.bind(this));
        });
    }

    handleResponse(response: BoardResponse | LossResponse) {
        const {cells} = response;
        const board: Map<string, Cell> = new Map<string, Cell>(cells.map(v => [this.mapKey(v[0]), v[1]]));
        this.setState({board, loading: false, started: true});
    }

    renderCell(cell: Cell, co: Coordinate): React.ReactFragment {
        if (typeof cell === "number") {
            return <div className={`cell cell-${cell}`} onClick={ComponentUtils.safeClick(this, () => this.clickNumber(co))}>{cell}</div>;
        } else if (cell === CellTypes.Flag) {
            return <div className="cell cell-flagged" onClick={ComponentUtils.safeClick(this, () => this.clickFlag(co))}>F</div>
        } else {
            return <div className="cell cell-unknown" onClick={ComponentUtils.safeClick(this, () => this.clickUnknown(co))}/>
        }
    }

    renderBoard(): React.ReactFragment {
        const {width, height} = this.props;
        const {board} = this.state;
        const rows: Array<React.ReactFragment> = [];
        for (let x: number = 0; x < width; x++) {
            const column: Array<React.ReactFragment> = [];
            for (let y: number = 0; y < height; y++) {
                const cell: Cell = board.get(this.mapKey({x, y})) || CellTypes.Unknown;
                column.push(<td>{this.renderCell(cell, {x, y})}</td>);
            }
            rows.push(<tr>{column}</tr>);
        }
        return rows;
    }

    render() {
        const {gameId, exit} = this.props;
        return (
            <div>
                <div>
                    <span>This is game {gameId}</span>
                </div>
                <table className="board">
                    <tbody>
                    {this.renderBoard()}
                    </tbody>
                </table>
                <div className="form-group">
                    <button className="form-control" onClick={exit}>Exit</button>
                </div>
            </div>);
    }

}