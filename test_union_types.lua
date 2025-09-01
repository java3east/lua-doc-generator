---@class TestUnion
---@field framework 'esx'|'qb' The framework type - should be esx or qb
---@field mode 'client'|'server'|'shared' The execution mode
---@field optional string|nil Optional string value
local TestUnion = {}

---@param type 'weapon'|'item'|'money' The type of thing to give
---@param amount number|string The amount as number or string
---@return boolean|string Success status or error message
function TestUnion:giveItem(type, amount)
    return true
end

---@type 'debug'|'info'|'warn'|'error'
local logLevel = 'info'
