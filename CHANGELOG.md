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
    - The domain-tokens parameter is the password for your dynamic DNS update service. 
    - Domain alias mappings are supported if your DNS maps a CNAME to an alternate record.
    - The domain-tokens and domain-aliases parameters are specified as a comma separated list of name=value pairs.
 * Release artifact as ZIP, alternative release for shaded uber JAR is still included.
    - Shading required removal of signing information from the dependency JARs, lessening the trustworthiness that the dependencies hadn't been tampered with in some way.
    - Just unzip the release ZIP file and run the JAR in exactly the same way you would have run the uber JAR.
 * Integration Tests. 
    - A full workflow life cycle example with DNS validation is tested from account registration through to deactivation.
    - An example integration-test.properties file is included in the source.
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
 