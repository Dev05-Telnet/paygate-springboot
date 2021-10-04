import React from 'react';
import './container.scss';

const BgContainer = props => {
  return <div className="container">{props.children}</div>;
};

export default BgContainer;
