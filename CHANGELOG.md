# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

This is a minor release with added convenience features. Also includes upgrades to dependencies.

### Added

 * Maven build.
 * Option `--config-file`, `-p`.
    - Configuration properties file to load default parameters from.
    - Default: `config.properties`.
    - The configuration properties file is optional, however it provides a convenience for defaulting commonly used values for the command line parameters. The values can still be overridden on the command line. The properties in the file are named in exactly the same way as they are on the command line, except without the leading dashes.
    - An example config.properties file is included in the source.
 * Command `http-get` and `http-post`. 
    - Convenience commands for dynamic DNS updates.
    - These are convenience commands for executing either a HTTP POST or HTTP GET to an arbitrary dynamic DNS update service. 
    - This provides a mechanism to set the message digest on the DNS service for the ACME challenge. 
    - SAN certificates are supported, in which case multiple calls will be made to update each DNS record. 
    - The `domain-tokens` parameter is the password for your dynamic DNS update service. 
    - Domain alias mappings are supported if your DNS maps a CNAME to an alternate record.
    - The `domain-tokens` and `domain-aliases` parameters are specified as a comma separated list of `name=value` pairs.
 * Release artifact as ZIP, alternative release for shaded uber JAR is still included.
    - Shading required removal of signing information from the dependency JARs, lessening the trustworthiness that the dependencies hadn't been tampered with in some way.
    - Just unzip the release ZIP file and run the JAR in exactly the same way you would have run the uber JAR.
    - The two releases are built by activating the appropriate Maven profile.
    - Run either `mvn clean package -P release-lib` to build the ZIP, or `mvn clean package -P release-shade` to build the uber JAR.
 * Integration Tests. 
    - A full workflow life cycle example with DNS validation is tested from account registration through to deactivation.
    - An example integration-test.properties file is included in the source.
    - The integration tests can be run with `mvn clean verify`.
 * Change log.

### Fixed

 * Changed `CSRParser`  to use less specific cast to parent `ASN1Sequence` instead of `DERSequence` as it could sometimes also be a `DLSequence`.

### Changed

 * Update `acme4j` `2.7` -> `2.16`
 * Read version number information from JAR Manifest.

### Security

 * [CVE-2020-15522] Update `bcprov-jdk15on` `1.62` -> `bcprov-jdk18on` `1.76`
 * [CVE-2022-25647] Update `gson` `2.8.5`   -> `2.10.1`

### Removed

 * Removed Gradle build (migrated to Maven).
 * Removed donation section from README (this should be moved to the wiki).

## [3.0.1] - 2019-08-02

v3.0.1 acme_client (PJAC)

### Fixed

 * Fix fullchain contains duplicate certificate.
 * Fix chain contains generated certificate (should contains only chain without generated certificate)

### Changed

 * Updated acme4j to version 2.7
 * Updated gradle to 5.5.1
 * Updated bcprov-jdk15on to 1.62

## [3.0.0] - 2018-07-21

v3.0.0 acme_client (PJAC)

### Changed

 * acme4j updated to version 2.2
 * ACME protocol updated to version 2 (wildcard certificates are supported now)

### Breaking Changes

 * Commands: `renew-certificate`, `update-agreement` and `authorize-domains` are removed.
 * New command `order-certificate` is added.

See [the wiki](https://github.com/porunov/acme_client/wiki) to get information about new certificate order flow.

## [2.1.3] - 2018-04-24

2.1.3 acme_client (PJAC)

### Changed

 * Update acme4j to v1.0

The next major release will support acme v2

## [2.1.2] - 2017-08-22

2.1.2 acme_client (PJAC)

### Fixed

 * Fix retrieval of expired authorizations and certificates

### Changed

 * Improve help command
 * Update acme4j to v0.11

## [2.1.1] - 2017-04-25

2.1.1 acme_client (PJAC)

### Fixed

 * Fix wrong status for `verify-domains` command when not authorized domains are used
 * Fix wrong status for `download-challenges` command when not authorized domains are used

## [2.1.0] - 2017-04-18

2.1.0 acme_client (PJAC)

### Changed

 * Added `--version` parameter
 * Added an output param `renewed` to the command `renew-certificate`
 * Added a possible output param `failed_certificates` to the command `download-certificates`
 * More accurate output parameters
 
### Fixed

 * Bug fixes

## [2.0] - 2017-03-25

v2.0 acme_client

### Changed

 * The ACME client now works with a `work-dir` differently. Now it doesn't serialize objects, but saves only json arrays with links to authorization or certificates. That is why all next releases will be compatible.
 
### Fixed

 * Bug fixes.
 
### Breaking Changes

_Warning!_

acme_client v2.0 isn't compatible with the acme_client v1.0. All next releases will be compatible to with the acme_client v2.0.
To move from the client v1.0 to the client v2.0 you need to remove files `authorization_uri_list` and `certificate_uri_list` from the working directory. After that you need to generate a new certificate.

## [1.0] - 2016-12-06

v1.0 acme_client

First release

