--- Eine Test-Klasse um die See Also Links zu testen
--- @class TestClass
--- @see MyOtherClass Eine verwandte Klasse
--- @see myGlobalFunction Eine globale Funktion
--- @see TestClass.myMethod Eine Methode dieser Klasse
local TestClass = {}

--- Eine Test-Methode
--- @param value string Der Eingabewert
--- @return string Der bearbeitete Wert
--- @see myGlobalFunction Siehe auch diese globale Funktion
--- @see MyOtherClass Siehe auch diese verwandte Klasse
function TestClass:myMethod(value)
    return "processed: " + value
end

--- Eine globale Test-Funktion
--- @param data table Die Eingabedaten
--- @return boolean Erfolg oder Fehler
--- @see TestClass Siehe auch diese Klasse
--- @see TestClass.myMethod Siehe auch diese Methode
function myGlobalFunction(data)
    return true
end

--- Eine andere Klasse für Tests
--- @class MyOtherClass
--- @see TestClass Die Haupttest-Klasse
local MyOtherClass = {}
