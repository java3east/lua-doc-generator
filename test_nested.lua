---@class Config
---Base configuration class
Config = {}

---@class Config.General
---General configuration settings
Config.General = {}

---@field name string The name setting
---@field enabled boolean Whether it's enabled
Config.General.name = "default"
Config.General.enabled = true

---@class Config.Target
---Target configuration settings
Config.Target = {}

---@field maxTargets number Maximum number of targets
Config.Target.maxTargets = 10

---@class Config.Target.FloatingText
---Floating text configuration for targets
Config.Target.FloatingText = {}

---@field color string The color of floating text
Config.Target.FloatingText.color = "white"
