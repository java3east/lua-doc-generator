---@class MyDocumentedClass : table this class is some random
---documented class that has a description and some fields
---@field public myField string this is a field
---@field public myNumberField number this is a number field
---@field private myPrivateField boolean this is a private field
MyDocumentedClass = {}

---This is a method that does something
---@nodiscard
---@param a string? this is a parameter
---@return string this method returns a string
function MyDocumentedClass:myMethod(a)
    return a
end

---Some comment
---@return boolean returns a boolean
function MyDocumentedClass.myStaticFunction()
    return true
end

---this function dose nothing
function SomeGlobalFunction()
end

---@type number some local variable
local myLocal = 5

---@type string this is a global variable
GlobalVar = "hello"
