---@class MyOtherClass : MyDocumentedClass this is another class
---that extends MyDocumentedClass
---@field public otherField number this is another field
---@field private secretData string some secret data
MyOtherClass = {}

---This method does something else
---@param value number the input value
---@param callback MyDocumentedClass optional callback
---@return boolean success indicator
function MyOtherClass:processValue(value, callback)
    return value > 0
end

---Static utility function
---@param data string input data
---@return MyOtherClass new instance
function MyOtherClass.create(data)
    return setmetatable({}, MyOtherClass)
end

---@type MyOtherClass global instance of MyOtherClass
GlobalInstance = MyOtherClass.create("test")

---Global utility function
---@param obj MyDocumentedClass the object to process
function ProcessObject(obj)
    print("Processing object")
end
