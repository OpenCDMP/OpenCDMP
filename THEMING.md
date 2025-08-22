# Theming 

**OpenCDMP** provides customizable themes for each tenant in the application, based on [**Material 3**](https://m3.material.io/) theming options.
There are 2 primary values that are used to generate the theme. 
- **Primary color** generates a base theme that will be applied throughout the application.
- **CSS Overrides** can be added in .json file format. The accepted variables are based on M3 theming, with a few additions specific to this application.
These variables are listed down below:

## Variables
- --link-color: The color used on all links in the application

- --md-sys-color-primary: The primary color used on buttons, highlighted icons, progress bars etc.
- --md-sys-color-on-primary: The text and icon color used on a primary background.
- --md-sys-color-primary-container
- --md-sys-color-on-primary-container

- --md-sys-color-secondary: The secondary color used in the application. Note that the "secondary" defined in previous versions of the application is now referenced by *tertiary*.
- --md-sys-color-on-secondary: The text and icon color used on a secondary background.
- --md-sys-color-secondary-container
- --md-sys-color-on-secondary-container

- --md-sys-color-tertiary: The tertiary color used on buttons, highlighted icons, progress bars etc. Note that in previous versions of the application this was referenced to as *secondary*.
- --md-sys-color-on-tertiary: The text and icon color used on a tertiary background. 
- --md-sys-color-tertiary-container
- --md-sys-color-on-tertiary-container

- --md-sys-color-error
- --md-sys-color-on-error
- --md-sys-color-error-container
- --md-sys-color-on-error-container

- --md-sys-color-background: The overall background color of the app
- --md-sys-color-on-background

- --md-sys-color-surface: Background color of separate app elements (cards, menus, pop ups etc)
- --md-sys-color-on-surface
- --md-sys-color-surface-variant
- --md-sys-color-on-surface-variant


## Extra resources
For ease of use, the [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/) can be used to generate the desired color values.
Note that currently, theming in OpenCDMP is based on light theme values.
