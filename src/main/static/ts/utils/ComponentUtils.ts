/**
 * Utils for react components
 */
import * as React from "react";

interface LoadingComponentState {
    loading: boolean
}

export class ComponentUtils {
    static safeClick(component: React.Component<any, LoadingComponentState>, onClick: Function): () => void {
        return () => {
            if (!component.state.loading) onClick();
        }
    }
}