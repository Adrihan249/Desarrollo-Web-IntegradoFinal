// ========== src/components/common/Checkbox.jsx ==========
import React from 'react';

const Checkbox = ({ checked, onChange, className = '', ...props }) => {
  return (
    <input
      type="checkbox"
      checked={checked}
      onChange={onChange}
      className={`h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer ${className}`}
      {...props}
    />
  );
};

export default Checkbox;