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
* Subscriber Agreement (auto) update
* Domain authorization (supports both HTTP and DNS challenges)
* Domain authorization deactivation
* Challenge download
* Domain authorization verification
* Certificate generation
* Certificate download
* Certificate renewal
* Certificate revocation

All functions are executed separately, so you can integrate them seamlessly in your own particular environment.

## System requirements

Java Runtime Environment (JRE) 8 or higher.

## Installation

No installation needed. The application is packed into a single executable JAR file, just [download](../../releases/) or [compile](../../wiki/How-to-compile-PJAC-from-source) it and you're ready to go.

## Usage

See [the wiki](../../wiki/) for information on how to use the application.

-----
## Donate to the PJAC project :hearts:
<span><a href="https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=HTPAA8RYN7APE&lc=UA&item_name=Developing%20open%20source%20projects&item_number=porunov_acme_client&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted" title="Donate"><img src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" alt="Donate" /></a></span>
<details>
<summary><span>
<img src="https://cloud.githubusercontent.com/assets/17673243/25156847/f2fe2874-24a5-11e7-8664-028aaddce685.png" alt="Donate BTC" />
</span></summary>
  <p>
Bitcoin address: 15PrkYhv9EZLiQbjQMJDRZsqdheE574XPe<br>
<img src="https://cloud.githubusercontent.com/assets/17673243/25156849/f2ff079e-24a5-11e7-8977-60873a68adbf.png" alt="15PrkYhv9EZLiQbjQMJDRZsqdheE574XPe" />
</p></details>
<details>
<summary><span>
<img src="https://cloud.githubusercontent.com/assets/17673243/25156850/f2ff9218-24a5-11e7-9a6f-66ea9d7edbf9.png" alt="Donate ETH" />
</span></summary>
  <p>
Ethereum address: 0x65a92111d599aa0f6695b011c1c01390d4f29a2a<br>
<img src="https://cloud.githubusercontent.com/assets/17673243/25156848/f2fe4016-24a5-11e7-9fe0-bb300b1359b6.png" alt="0x65a92111d599aa0f6695b011c1c01390d4f29a2a" />
</p></details>
