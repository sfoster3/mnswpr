import * as React from "react";
import * as ReactDOM from "react-dom";
import {RootComponentLoader} from "./components/RootComponentLoader";


const rootEl = document.getElementById("react-root");
ReactDOM.render(<RootComponentLoader/>, rootEl);