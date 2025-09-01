---@class BaseTable<T>
---@field data T The data stored in this table
---@field count number Number of items

---@class ComplexChild : BaseTable<{id: string, value: number, metadata: {tags: string[], priority: number}}>
---@field extraField string Additional field in child class
local ComplexChild = {}

---@class SimpleChild : BaseTable<string>
---@field simpleExtra boolean Simple additional field
local SimpleChild = {}

---@class GenericParent<K, V> : table<K, V>
---@field size number Size of the collection
local GenericParent = {}

---@class NestedGeneric : GenericParent<string, {name: string, data: table<string, any>}>
---@field specialMethod function Special method for nested generics
local NestedGeneric = {}
