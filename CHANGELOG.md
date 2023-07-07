<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# nx-webstorm Changelog

## [Unreleased]
- Update to latest intellij template (v1.8.0)

## [0.16.0]
- Update to latest intellij template (v1.7.0)

## [0.15.0]
- Updated to latest intellij template (v1.5.0)

## [0.14.0]
- Updated to latest intellij template (v1.4.0)

## [0.13.0]
- Updated to latest intellij template
- support for webstorm 2022.3
- Updated plugin name to Nx Console UI to meet requirements of Intellij plugin names. [More info](https://plugins.jetbrains.com/docs/marketplace/plugin-overview-page.html#plugin-name)

## [0.12.0]
- Updated to latest intellij template
- support for running nx with pnpm.

## [0.11.0]
- upgraded to latest DSL ui builder
- Updated and fixed usage of generator debugger.

## [0.10.0]
### Changed
- update to platformVersion 2022.1 (away from EAP snapshot)
- Fix issue [#90](https://github.com/etkachev/nx-webstorm/issues/90) when launching IDE

## [0.9.3]
### Changed
- updated dependencies and plugin template to [1.1.2](https://github.com/JetBrains/intellij-platform-plugin-template/releases/tag/v1.1.2)
- updated to gradle-7.4.2
- upgrade jvm kotlin to 1.6.20
- upgrade intellij to 1.5.2
- update to platform 221

## [0.9.2]
### Changed
- updated dependencies and plugin template to [1.1.0](https://github.com/JetBrains/intellij-platform-plugin-template/releases/tag/v1.1.0)
- update props to support latest webstorm version (2021.3)

## [0.9.1]
### Fixed
- Fixed issue [#76](https://github.com/etkachev/nx-webstorm/issues/76) for projects using Nx 13

### Changed
- Updated Plugin template to v1.0.0

## [0.9.0]
### Added
- when searching custom schematics within nx workspaces, also search schema.json files for `$id` property, if `id` is
  not found. Ticket [#71](https://github.com/etkachev/nx-webstorm/issues/71)

## [0.8.2]
### Changed
- Updated Plugin template to v0.10.1
- Switched gradle-wrapper `distributionUrl` back to `-all` config.
- Update `pluginUntilBuild` to upcoming Webstorm version `212.*`

### Fixed
- Fixing issue [#54](https://github.com/etkachev/nx-webstorm/issues/54) with possible index out of bounds on dir split.

## [0.8.1]
### Changed
- added support for build 211.*

## [0.8.0]
### Added
- New Plugin Setting for configuring root directory of nx project. By default, it will be `/`

### Changed
- Under plugin settings, the default list of external schematics to scan, now includes more default nrwl packages

### Fixed
- Fixed situations where Debug schematic wouldn't work if workspace did not have any other run configs setup in
  Jetbrains IDE.

## [0.7.2]
### Changed
- updated the plugin to the latest plugin template of 0.8.1

### Fixed
- fixed issue where settings plugin for custom schematics directory was not able to configure or change.
- fixed issue where commands run would sometimes be replaced when running bash shortcuts such as those starting with "!"

## [0.7.1]
### Changed
- Refresh button for schematic list tool window is now an icon.
- updated the plugin to the latest plugin template of 0.8.0
- gradle.properties `platformVersion` updated to 2020.3

## [0.7.0]
### Changed
- Updated default directory for custom schematics with Nx 11
- Updated gradle configs and ymls to latest plugin template 0.7.1

## [0.6.1]
### Fixed
- update `pluginUntilBuild` to latest 203.* to handle latest webstorm update

## [0.6.0]
### Added
- Added support for running, debugging custom schematics within non-nx angular projects.

### Changed
- Updated Readme to include the latest updates.
- Renamed the column "Type" to "Collection" within the schematic table search.

## [0.5.0]
### Added
- \***Debugging Schematics**\* - you can now debug schematics within your IDE with a simple click of a button
- Added compatibility to allow regular angular projects (non-Nx projects), to use Generate UI.
- Added application setting page for ui
    - Added ability to set the placement of the new action button icons within the schematic ui window.

### Changed
- Schematic UI window "Dry Run" and "Run" buttons have been converted to icon buttons

### Fixed
- Loading Nrwl icon correctly on load.

## [0.4.0]
### Added
- Nrwl icon added to Nx tool tab
- Added autocomplete dropdown to show list of projects to choose from. This is for schematic properties that
  have `$default.$source` set to `projectName`.

### Changed
- Plugin Settings are transferred to be project settings instead of application settings.
- Some general refactor and performance tweaks

## [0.3.0]
### Added
- Added settings to configure plugin. Be able to configure things such as external packages to scan for schematics, and
  configuring location of custom schematics.

## [0.2.1]
### Added
- Added mechanism to refresh list of schematics. Should be a button next to the search bar on panel showing list of
  schematics.

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
