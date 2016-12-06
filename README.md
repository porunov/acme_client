# Java ACME client application for fully manual certificate installation/management.
### Suitable if you have your own automation tools (ansible, puppet, chef, saltstack...) or you want to automate your certificate management (generate/renew/revoke...) by your own (with your own scripts/programs/crond jobs...).

This application is based on Java ACME library implementation:<br>
https://github.com/shred/acme4j<br>
If you need more options or your own implementation you can use that library.

## Supported functions:
1. Account creation  
2. Retrieve url of the newest agreement.  
3. Update agreement.  
4. Add email to existing account.  
5. Deactivate account.  
6. Authorize domains.  
7. Deactivate domain authorization for different domains.  
8. Download challenges (either HTTP or DNS).  
9. Verify domains  
10. Generate certificates  
11. Download certificate/certificates  
12. Revoke certificate/certificates  
13. Renew certificate  

All functions are separated, so you can execute them as needed.

## How to install acme_client?
1. You have to install Java 8.
2. Check if your java has certificates for your provider. If your java release hasn't certificates you should add them to your java. ( Only newest Java 8 versions has Letsencrypt certificates ).
3. Download an executable file `acme_client.jar`: https://github.com/porunov/acme_client/releases/download/v1.0/acme_client.jar

## How to compile it from sources?
1. Clone the project (or download it) 
  
  ```
  git clone https://github.com/porunov/acme_client.git
  ```
  
2. Go to the project's directory

  ```
  cd acme_client
  ```

3. Build the project

  ```
  gradlew build
  ```

4. Go to the directory with the executalbe file (Or just copy the file) 

  ```
  cd build/libs/
  ```

5. There will be an executable file `acme_client.jar`.

## Usage: 

```
java -jar acme_client.jar --command <command> [options]
```

#### ! Warning !
By default acme_client uses Letsencrypt's production server. I.e.:<br>
`https://acme-v01.api.letsencrypt.org/directory`

If you want to test the client then use a test server:<br>
`--server-url https://acme-staging.api.letsencrypt.org/directory`

If you use Letsencrypt's production server for testing you can reach limits which are set by Letsencrypt (or your ACME provider).

## Options:
See all available options in wiki: https://github.com/porunov/acme_client/wiki/Options

## Commands:
See all available commands in wiki: https://github.com/porunov/acme_client/wiki/Commands

## Example scenarios:
1. [Get a certificate for different domains](https://github.com/porunov/acme_client/wiki/Scenario-1:-Get-a-certificate-for-different-domains)<br>
2. [Renew certificate](https://github.com/porunov/acme_client/wiki/Scenario-2:-Renew-certificate)<br> 
3. [Revoke certificates](https://github.com/porunov/acme_client/wiki/Scenario-3:-Revoke-certificates)<br>
4. [Download certificates](https://github.com/porunov/acme_client/wiki/Scenario-4:-Download-certificates)<br>
5. [Download challenges](https://github.com/porunov/acme_client/wiki/Scenario-5:--Download-challenges)<br>
6. [Verify domains](https://github.com/porunov/acme_client/wiki/Scenario-6:-Verify-domains)<br>
7. [Get and Update agreement](https://github.com/porunov/acme_client/wiki/Scenario-7:-Get-and-Update-agreement)<br>
8. [Add an email to your account](https://github.com/porunov/acme_client/wiki/Scenario-8:-Add-an-email-to-your-account)<br>
9. [Create an account](https://github.com/porunov/acme_client/wiki/Scenario-9:-Create-an-account)<br>
10. [Deactivate domain authorization](https://github.com/porunov/acme_client/wiki/Scenario-10:-Deactivate-domain-authorization)<br>
11. [Deactivate an account](https://github.com/porunov/acme_client/wiki/Scenario-11:-Deactivate-an-account)<br>
12. [Generate a certificate](https://github.com/porunov/acme_client/wiki/Scenario-12:-Generate-a-certificate)<br>

## Troubleshooting
See [Troubleshooting](https://github.com/porunov/acme_client/wiki/Troubleshooting) if you have any problems with installation/usage.
