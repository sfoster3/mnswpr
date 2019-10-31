import * as React from 'React';

export interface RootProps {
}

export interface RootState {

}

export class RootComponentLoader extends React.Component<RootProps, RootState> {

    static propTypes = {};
    static defaultProps = {};

    render() {
        return (
            <div>
                <span>Hello World</span>
            </div>);
    }
}