# nx-webstorm

![Build](https://github.com/etkachev/nx-webstorm/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.etkachev.nxwebstorm.svg)](https://plugins.jetbrains.com/plugin/15000-nx-webstorm)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.etkachev.nxwebstorm.svg)](https://plugins.jetbrains.com/plugin/15000-nx-webstorm)

<!-- Plugin description -->

This plugin is the Webstorm version
of [Nx Console](https://marketplace.visualstudio.com/items?itemName=nrwl.angular-console) or at least in the process to
be. This plugin will only work for projects that use [Nx](http://nx.dev/) dev tools. For this alpha release, this plugin
will support the `Generate` functionality which will allow you to display and run/debug custom schematics, along with
default angular, ngrx, nestjs schematics.

#### Compatibility

This works with any [Nx](http://nx.dev/) project. But this can also work with any regular Angular project that doesn't
use Nx, since the schematics are built-in the `@schematics/angular` collection.

#### Nx Projects

- Typically your custom schematics will be within your directory: `/tools/schematics`, but this can be configured within
  the Plugin Settings, if your project is setup differently.
- Nx can be installed either globally or local to your project.

#### Angular Projects

- For [custom schematics](https://angular.io/guide/schematics-authoring), the plugin will search within the directory
  set in the Plugin Settings. Please update this for your regular angular project, since the default is set
  to `/tools/schematics` and this is the default for Nx Projects.
- Make sure that you have `@angular-devkit/schematics-cli` installed either local to your project or globally for this
  to work with custom schematics.

#### How it works

- On startup, this plugin will check if you have a valid nx.json or angular.json file in your root directory, and only
  be enabled if so.
- When clicking on the `Nx` menu (either at the top menubar, or top right tab), you will have option to `Generate` which
  will popup a list of schematics to choose from.
- Upon choosing one of the schematics, the next screen will be an auto-generated form of all the field controls to fill
  out in order to run the schematic.
- Required fields will have the label contain (*)
- If using the side tool-window Generate UI, you will have the option to either do `Run`, `Dry Run`, or `Debug`
    - `Dry Run` will open your IDE terminal and run the schematic with the filled out fields but without affecting your
      local files. You will see the output of what files will be affected in your repo.
    - `Run` will do the same things as `Dry Run` except it will actually affect your local files and add/update/delete
      files depending on what the selected schematic is designed to do.
    - `Debug` button allows you to setup breakpoints in your schematic files and run the debug execution, so you can
      easily and quickly debug your schematics right within Webstorm!

#### Known Issues

- When debugging schematics, any schematic that has a `path` property for the schematic, if it's left empty, it is
  defaulted to the path of the executable (usually within node_modules). This causes the output files placed in the
  incorrect place. To fix this, make sure you pass in the `path` property correctly pointing to the module you would
  like to run the schematic against. This is only an issue during debugging, and does not affect when you are doing
  regular `Dry Run` or `Run` executions.

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "nx-webstorm"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/etkachev/nx-webstorm/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Template ToDo list

- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml)
  and [sources package](/src/main/kotlin).
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [x] [Publish a plugin manually](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html)
  for the first time.
- [x] Set the Plugin ID in the above README badges.
- [x] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [x] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified
  about releases containing new features and fixes.

---
Plugin based on the [IntelliJ Platform Plugin Template][template] v1.0.0.

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
