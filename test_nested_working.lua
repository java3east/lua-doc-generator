---@class Config
---Base configuration class
Config = {}

---@class Config.General
---@field name string The name setting
---@field enabled boolean Whether it's enabled
---General configuration settings
Config.General = {}

---@class Config.Target
---@field maxTargets number Maximum number of targets
---Target configuration settings
Config.Target = {}

---@class Config.Target.FloatingText
---@field color string The color of floating text
---@field size number The font size
---Floating text configuration for targets
Config.Target.FloatingText = {}

Config.General.name = "default"
Config.General.enabled = true
Config.Target.maxTargets = 10
Config.Target.FloatingText.color = "white"
Config.Target.FloatingText.size = 12
