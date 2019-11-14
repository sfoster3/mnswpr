import * as React from 'React';
import {MainMenuComponent} from "./MainMenuComponent"

export interface RootProps {
}

export interface RootState {
    gameId: string | undefined;
}

export class RootComponentLoader extends React.Component<RootProps, RootState> {

    constructor(props: RootProps) {
        super(props);
        this.state = {gameId: undefined};
    }

    startNewGame() {

    }

    render(): React.ReactNode {
        const {gameId} = this.state;
        return (
            <div>
                {Boolean(gameId) ?
                    <div></div> :
                    <MainMenuComponent startNewGame={this.startNewGame}/>}
            </div>);
    }
}