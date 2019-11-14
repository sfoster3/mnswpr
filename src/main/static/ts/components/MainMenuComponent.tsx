import * as React from 'React';

interface MainMenuComponentProps {
    startNewGame: VoidFunction
}

const initialState = {loading: false, height: 30, width: 30, mines: 30};
type State = Readonly<typeof initialState>;

export class MainMenuComponent extends React.Component<MainMenuComponentProps, State> {
    state: State = initialState;

    clickStartNewGame() {
        this.setState({loading: true}, () => this.props.startNewGame());
    }

    onChangeWidth({target: {value: width}}: { target: { value: number } }) {
        this.setState({width});
    }

    onChangeHeight({target: {value: height}}: { target: { value: number } }) {
        this.setState({height});
    }

    onChangeMines({target: {value: mines}}: { target: { value: number } }) {
        this.setState({mines});
    }


    render(): React.ReactNode {
        const {loading, height, width, mines} = this.state;
        return (
            <div>
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