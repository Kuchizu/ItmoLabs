import React from "react";

import Link from "../../atoms/Link";
import Clock from "../../atoms/Clock";

import "./index.scss";

const Header = () => {
    return (
        <header className="header">
            <HeaderItem className="header__item_left">
                <HeaderClock />
            </HeaderItem>
            <HeaderItem>
                <span>Nodiri Khisravkhon. Laboratory Work №4 Var: 37462424.</span>
            </HeaderItem>
            <HeaderItem className="header__item_right">
                <Link href="https://github.com/Kuchizu">Github</Link>
            </HeaderItem>
        </header>
    );
}

const HeaderItem = ({ className, children }) => {
    return (
        <div className={"header-item header__item " + (className == undefined ? "" : className)}>
            {children}
        </div>
    );
}


let clockTickInterval = 1000;

const HeaderClock = () => {
    return (
        <div className="header-clock">
            <span key="1" className="header-clock__label">Текущее время: </span>
            <Clock key="2" className="header-clock__clock" interval={clockTickInterval} />
        </div>
    );
}


export default Header;