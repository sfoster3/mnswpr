import * as React from "React";
import { MainMenuComponent } from "./MainMenuComponent";
import { GameComponent } from "./GameComponent";
import { GameArgs, ServerApi } from "../utils/ServerApi";

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface RootProps {}

export interface RootState {
  gameId: string | undefined;
  args: GameArgs | undefined;
}

export class RootComponentLoader extends React.Component<RootProps, RootState> {
  constructor(props: RootProps) {
    super(props);
    this.state = { gameId: undefined, args: undefined };
  }

  startNewGame(args: GameArgs): void {
    ServerApi.createGame(args).then(({ gameId }) =>
      this.setState({ gameId, args })
    );
  }

  exitGame(): void {
    this.setState({ gameId: undefined, args: undefined });
  }

  render(): React.ReactNode {
    const { gameId, args } = this.state;
    return (
      <div>
        {gameId ? (
          <GameComponent
            gameId={gameId}
            {...args}
            exit={this.exitGame.bind(this)}
          />
        ) : (
          <MainMenuComponent startNewGame={this.startNewGame.bind(this)} />
        )}
      </div>
    );
  }
}
