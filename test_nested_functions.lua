---@class Server
---Hauptserver-Klasse
Server = {}

---@class Server.Core
---Core-Funktionalität des Servers
Server.Core = {}

---@param data string Die zu verarbeitenden Daten
---@return boolean true wenn erfolgreich
---Diese Funktion verarbeitet Daten im Server Core
Server.Core.myFunction = function(data)
    return true
end

---@return number Die aktuelle Zeit
---Gibt die aktuelle Serverzeit zurück
Server.Core.anotherFunction = function()
    return os.time()
end

---@class Server.Database
---Datenbankzugriff
Server.Database = {}

---@param host string Der Hostname
---@param port number Der Port
---@return boolean true wenn Verbindung erfolgreich
---Verbindet sich mit der Datenbank
Server.Database.connect = function(host, port)
    return true
end

-- Normale Funktion sollte weiterhin funktionieren
---@param x number Eine Zahl
---@return number Das Quadrat der Zahl
---Berechnet das Quadrat einer Zahl
function normalFunction(x)
    return x * x
end
