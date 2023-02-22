# Changelog
All notable changes to `cnj-common-observability-spring` will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2023-02-22
### Changed
- upgraded to Java 17
- upgraded to Spring Boot 3.0.2
- updated detection of auto-configuration class to Spring Boot 3 method 
- moved to new build pipeline on AWS

## [0.5.0] - 2022-09-12
### Fixed
- now REST trace works if application property `cnj.observability.rest.tracing.enabled` is set to `true`.
- added auto-configuration capability to library, in order to get all components provided by this library scanned by default.

## [0.4.0] - 2022-08-12
### Changed
- consolidated undertow metric names with Prometheus metric naming conventions 

## [0.3.0] - 2022-07-29
### Added
- added support for Undertow metrics.
### Changed

## [0.2.1] - 2022-02-28
### Added
### Changed
- removed obsolete dependency on Log4J 2 which caused duplicate SLF4J implementations in library users.

## [0.2.0] - 2022-02-28
### Added
### Changed
- refactored package names by dropping the `spring` subpackage from the hierarchy
- added support for logging context information via a new LoggingMdcFilter plus auto-configuration.

## [0.1.0] - 2022-02-11
### Added
### Changed
- first revision
