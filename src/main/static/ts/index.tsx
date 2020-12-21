import * as React from "react";
import * as ReactDOM from "react-dom";
import { RootComponentLoader } from "./components/RootComponentLoader";
import "popper.js";
import "bootstrap";
import "../css/index.scss";

const rootEl: HTMLElement = document.getElementById("react-root");
if (rootEl) {
  ReactDOM.render(<RootComponentLoader />, rootEl);
}
