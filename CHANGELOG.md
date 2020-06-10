# [0.5.0](https://github.com/MaethorNaur/restui/compare/v0.4.0...v0.5.0) (2020-06-10)


### Code Refactoring

* 💡 Remove docker java client for a custom on with Akka ([#30](https://github.com/MaethorNaur/restui/issues/30)) ([571b4f6](https://github.com/MaethorNaur/restui/commit/571b4f6f3fcb605585d8b644a14e1a63145ee56c)), closes [#24](https://github.com/MaethorNaur/restui/issues/24)


### Features

* Create a webhook provider ([#42](https://github.com/MaethorNaur/restui/issues/42)) ([f8f5b97](https://github.com/MaethorNaur/restui/commit/f8f5b9799075a62822bf2556697139e48d13f556)), closes [#32](https://github.com/MaethorNaur/restui/issues/32)


### BREAKING CHANGES

* 🧨 TLS connection is dropped at the moment



# [0.4.0](https://github.com/MaethorNaur/restui/compare/v0.3.0...v0.4.0) (2020-06-03)


### Code Refactoring

* 💡 Rename swagger by openapi/specification ([#27](https://github.com/MaethorNaur/restui/issues/27)) ([39466a8](https://github.com/MaethorNaur/restui/commit/39466a891f9b29b7d27fbf96a835f16cabf6fd5d)), closes [#26](https://github.com/MaethorNaur/restui/issues/26)


### BREAKING CHANGES

* 🧨 Default configuration of labels for Docker and K8S
Configuration for git and restui config file



# [0.3.0](https://github.com/MaethorNaur/restui/compare/v0.2.0...v0.3.0) (2020-06-03)


### Bug Fixes

* 🐛 Fix variable name ([eeea641](https://github.com/MaethorNaur/restui/commit/eeea6415071f4827bab038c74e6c5051d68f2576))


### Code Refactoring

* 💡 Add an id to the service to handle name changes ([#21](https://github.com/MaethorNaur/restui/issues/21)) ([e7e2e36](https://github.com/MaethorNaur/restui/commit/e7e2e3655ef9944dd3fdfa7752a3c8dcd18391a0)), closes [#20](https://github.com/MaethorNaur/restui/issues/20)


### Features

* Add a git provider ([5589337](https://github.com/MaethorNaur/restui/commit/5589337699f0cb3dac21cf71e0facc8f832f674f)), closes [#16](https://github.com/MaethorNaur/restui/issues/16)


### BREAKING CHANGES

* 🧨 Service and front Events now have an id field
* - Renamed `ServiceDiscoveryProvider` into `Provider` (as the file structures)
- Providers now scrap the file content and send it to RestUI



# [0.2.0](https://github.com/MaethorNaur/restui/compare/v0.1.2...v0.2.0) (2020-05-21)



## [0.1.2](https://github.com/MaethorNaur/restui/compare/v0.1.0...v0.1.2) (2020-05-20)



# 0.1.0 (2020-05-19)



