<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# nx-webstorm Changelog

## [Unreleased]
### Added
- Added mechanism to refresh list of schematics. Should be a button next to the search bar on panel showing list of schematics.

### Changed

### Deprecated

### Removed

### Fixed

### Security
## [0.2.0]
### Added

### Changed
- When running nx commands, use local installed nx cli instead of global

### Deprecated

### Removed

### Fixed
- When going between doing `Dry Run` and `Run`, UI now switches tabs to the currently running shell.
- When there are required fields that aren't filled out, it won't run the terminal commands anymore

### Security

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
