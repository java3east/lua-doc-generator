---@class Config
---Base configuration class
Config = {}

---@class Config.General
---@field name string The name setting
---@field enabled boolean Whether it's enabled
---General configuration settings
Config.General = {}

Config.General.name = "default"
Config.General.enabled = true

---@class Config.Target
---@field maxTargets number Maximum number of targets
---Target configuration settings
Config.Target = {}
Config.Target.maxTargets = 10

---@class Config.Target.FloatingText
---@field color string The color of floating text
---Floating text configuration for targets
Config.Target.FloatingText = {}
Config.Target.FloatingText.color = "white"
