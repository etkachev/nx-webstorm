<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# nx-webstorm Changelog

## [Unreleased]
### Added
- Nrwl icon added to Nx tool tab
- Added autocomplete dropdown to show list of projects to choose from. 
This is for schematic properties that have `$default.$source` set to `projectName`.

### Changed
- Plugin Settings are transferred to be project settings instead of application settings.
- Some general refactor and performance tweaks

### Deprecated

### Removed

### Fixed

### Security
## [0.3.0]
### Added
- Added settings to configure plugin. Be able to configure things such as external packages to scan for schematics, and configuring location of custom schematics.

## [0.2.1]
### Added
- Added mechanism to refresh list of schematics. Should be a button next to the search bar on panel showing list of schematics.

### Fixed
- If shell tab is still executing commands, do not try to run commands again. 

## [0.2.0]
### Changed
- When running nx commands, use local installed nx cli instead of global

### Fixed
- When going between doing `Dry Run` and `Run`, UI now switches tabs to the currently running shell.
- When there are required fields that aren't filled out, it won't run the terminal commands anymore

## [0.1.2]
### Changed
- Updating Readme to the latest badges with new plugin id

### Fixed
- Fixed to use `nx generate` vs `ng generate` in executing commands
- Fixed flag values that have spaces to be wrapped in single quotes

## [0.1.1]
### Fixed
- Fixed terminal commands so that it won't add flags that have empty values

## [0.1.0]
### Fixed
- Fixed terminal commands generated on non custom schematics (such as @nrwl/workspace).


## [0.0.2]
### Added
- `Generate` menu option to run schematics (including custom schematics) with `Dry Run` option.
