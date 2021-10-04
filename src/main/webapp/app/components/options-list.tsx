import React, { useState } from 'react';
import BgContainer from './BgContainer';
import './options-list.scss';

const OptionsList = props => {
  const [selectedCategory, setSelectedCategory] = useState('-1');
  const selectHandler = e => {
    const { _, value } = e.target;
    setSelectedCategory(value);
    props.callback(value);
  };

  return (
    <div className="options">
      {props.data.map(category => (
        <div key={category.id + Math.random()}>
          <input
            type="radio"
            key={category.id + Math.random()}
            value={category.id}
            onChange={selectHandler}
            checked={selectedCategory === category.id + ''}
          />
          <label key={category.id + Math.random()}>{category.name}</label>
        </div>
      ))}
    </div>
  );
};

export default OptionsList;
