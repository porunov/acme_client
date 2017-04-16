# Java ACME Client for manual step-by-step SSL certificate management.

### A CLI management agent designed for use with your own automation tools (ansible, puppet, chef, saltstack, etc.). Also suitable if you want to automate certificate management with your own scripts/programs/crond-jobs etc.).

ACME is a protocol that a Certificate Authority (CA) and an applicant can use to automate the process of verification and certificate issuance.

This application is based on Java ACME library implementation: https://github.com/shred/acme4j  
If you need more options or your own implementation you can use that library.

## Supported functions
* Account creation
* Account deactivation
* E-mail address association
* Subscriber Agreement URL retrieval
* Subscriber Agreement (auto) update
* Domain authorization
* Domain authorization deactivation
* Challenge download (supports both HTTP and DNS challenges)
* Domain verification
* Certificate generation
* Certificate download
* Certificate renewal
* Certificate revocation

All functions are executed separately, so you can integrate them seamlessly in your own particular environment.

## System requirements

Java Runtime Environment (JRE) 8 or higher

## Installation

No installation needed. The application is packed into a single executable JAR file, just download or compile it and you're ready to go.

## Usage

See the [wiki](../../wiki/) for information on how to use the application.


-----
[![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=HTPAA8RYN7APE&lc=UA&item_name=Developing%20open%20source%20projects&item_number=porunov_acme_client&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted)
