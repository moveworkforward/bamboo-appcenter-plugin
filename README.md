# Bamboo App Center Plugin

## Overview
Plugin for Atlassian Bamboo for uploading artifact files on AppCenter using App Center API.

## Build
Here are the SDK commands you'll use immediately:

* atlas-run -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-cli -- after atlas-run or atlas-debug, opens a Maven command line window: - 'pi' reinstalls the plugin into the running product instance
* atlas-help -- prints description for all commands in the SDK

Full documentation is always available at:

https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK

## Used Library:
App Center Rest API
## Data security and privacy statement
This plugin does not collect any data despite the task configuration. The task configuration will be saved in your bamboo database. The plugin will not download or upload any data to third party companies, except files of your app that you select to publish on App Center - this files uploads directly to App Center. You can also check the source code to prove this statement and/or compile the plugin from source if you do not trust atlassian market place.

## License
Apache-2.0 © [Roman Ivannikov]()
