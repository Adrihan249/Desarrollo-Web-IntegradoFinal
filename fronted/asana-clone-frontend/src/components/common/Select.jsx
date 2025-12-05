// ========== src/components/common/Select.jsx ==========
import React from 'react';

const Select = ({ options, value, onChange, className = '', ...props }) => {
  return (
    <select
      value={value}
      onChange={onChange}
      className={`block w-full rounded-md border-gray-300 shadow-sm 
                 focus:border-primary-500 focus:ring-primary-500 
                 text-sm py-2 px-3 ${className}`}
      {...props}
    >
      {options.map((option) => (
        <option 
          key={option.value} 
          value={option.value}
        >
          {option.label}
        </option>
      ))}
    </select>
  );
};

export default Select;