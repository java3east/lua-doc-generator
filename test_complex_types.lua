---@class TestClass
---@field myTable table<string, any> A table with string keys and any values
---@field myMap table<number, string> A table mapping numbers to strings
---@field complexType table<string, table<number, boolean>> A nested table type
local TestClass = {}

---@param data table<string, any> Input data with string keys
---@param options table<string, boolean> Configuration options
---@return table<number, string> Result mapping
function TestClass:processData(data, options)
    return {}
end

---@type table<string, any>
local globalConfig = {}
