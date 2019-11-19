import * as React from 'React';
import {GameArgs} from "../utils/ServerApi";

interface MainMenuComponentProps {
    startNewGame(args: GameArgs): any
}

const initialState = {loading: false, height: 30, width: 30, mines: 100};
type State = Readonly<typeof initialState>;

export class MainMenuComponent extends React.Component<MainMenuComponentProps, State> {
    state: State = initialState;

    clickStartNewGame() {
        const {width, height, mines} = this.state;
        this.setState({loading: true}, () => this.props.startNewGame({width, height, count: mines}));
    }

    onChangeWidth({target: {value}}: { target: { value: string } }) {
        this.setState({width: Number(value)});
    }

    onChangeHeight({target: {value}}: { target: { value: string } }) {
        this.setState({height: Number(value)});
    }

    onChangeMines({target: {value}}: { target: { value: string } }) {
        this.setState({mines: Number(value)});
    }


    render(): React.ReactNode {
        const {loading, height, width, mines} = this.state;
        return (
            <div className="main-menu">
                <h2>Mnswpr</h2>

                <div className="form-row">
                    <div className="col">
                        <label htmlFor="widthI">Width</label>
                        <input className="form-control" type="number" id="widthI" placeholder="Width" value={width}
                               onChange={this.onChangeWidth.bind(this)} disabled={loading}/>
                    </div>
                    <div className="col">
                        <label htmlFor="heightI">Height</label>
                        <input className="form-control" type="number" id="heightI" placeholder="Height" value={height}
                               onChange={this.onChangeHeight.bind(this)} disabled={loading}/>
                    </div>
                </div>
                <div className="form-row">
                    <div className="col">
                        <label htmlFor="mineI">Mines</label>
                        <input className="form-control" type="number" id="mineI" placeholder="Mines" value={mines}
                               onChange={this.onChangeMines.bind(this)} disabled={loading}/>
                    </div>
                    <div className="col">
                        <button className="form-control" disabled={loading} onClick={this.clickStartNewGame.bind(this)}>
                            New Game
                        </button>
                    </div>
                </div>
            </div>);
    }
}