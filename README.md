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

-----
## Donate to the PJAC project :hearts:
<span class="badge-patreon"><a href="https://www.patreon.com/porunov" title="Donate to this project using Patreon"><img src="https://img.shields.io/badge/patreon-donate-blue.svg" alt="Patreon donate button" /></a></span>
<span class="badge-bountysource"><a href="https://salt.bountysource.com/checkout/amount?team=porunov" title="Donate to this project using Bountysource"><img src="https://img.shields.io/badge/bountysource-donate-blue.svg" alt="Patreon donate button" /></a></span><br>
Bitcoin address: 15PrkYhv9EZLiQbjQMJDRZsqdheE574XPe<br>
Ethereum address: 0x65a92111d599aa0f6695b011c1c01390d4f29a2a<br>
