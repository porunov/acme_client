# Porunov Java ACME Client (PJAC)
### An ACME client application for step-by-step SSL certificate management.

PJAC is a CLI management agent designed for use with your own automation tools (ansible, puppet, chef, saltstack, etc.). Also suitable if you want to automate certificate management with your own scripts/programs/crond-jobs etc.).

ACME is a protocol that a Certificate Authority (CA) and an applicant can use to automate the process of verification and certificate issuance.

This application is based on [acme4j](https://github.com/shred/acme4j), a Java ACME library implementation. If you need your own implementation you can use that library.

## Supported functions
* Account creation
* Account deactivation
* E-mail address association
* Subscriber Agreement URL retrieval
* Make a certificate request (supports HTTP and DNS challenges)
* Domain authorization deactivation
* Challenge download
* Domains verification
* Certificate generation
* Certificate download
* Certificate revocation

All functions are executed separately, so you can integrate them seamlessly in your own particular environment.

## Supported ACME protocol versions
PJAC version 2.x.y and earlier support acme protocol version 1.  
PJAC version from 3.x.y supports acme protocol version 2.

## System requirements

Java Runtime Environment (JRE) 8 or higher.

## Installation

No installation needed. The application is packed into a single executable JAR file, just [download](../../releases/) or [compile](../../wiki/How-to-compile-PJAC-from-source) it and you're ready to go.

## Usage

See [the wiki](../../wiki/) for information on how to use the application.

