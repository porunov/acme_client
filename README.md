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
3. Download an executable file `acme_client.jar`: ...

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

## Options:

```
--account-key, -a
   Your private key for account.
--agreement-url
   Url for agreement
--cert-dir
   Directory where generated certificates will be downloaded
   Default: /var/acme_work_dir/cert/
--challenge-type
   Challenge type (supported: HTTP01, DNS01. Default: HTTP01)
   Default: HTTP01
--command
   Command to execute. Command can be on of the next commands: register,
   get-agreement-url, update-agreement, add-email, deactivate-account, authorize-domains,
   deactivate-domain-authorization, download-challenges, verify-domains,
   generate-certificate,download-certificate, revoke-certificate, renew-certificate. 
   Read below 'Main commands' section to know how to use these commands.
--csr, -c
   CSR file.
--dns-digests-dir
   Directory to place dns digest files
   Default: /var/acme_work_dir/dns_digests/
--domain, -d
   Domain addresses. Can be any count till providers limits (I.e.
   Letsencrypt has 100 domains limit for one certificate).
--email, -e
   Email address to retrieve an account if you lost your private key (If it
   is supported by your provider)
--force
   Force renewal if you don't want to check expiration time before renew.
   Works the same as 'generate-certificate' command
   Default: false
--from-time
   When you revoke your certificates you can set this option. It means that
   all certificates which are started from this date will be revoked (date in
   milliseconds GMT.)
   Default: -9223372036854775808
--help
   Show help.
   Default: false
--log-dir
   Directory for log files
   Default: /var/log/acme/
--log-level
   Log level. Can be one of the next: 'OFF' - without logs; 'ERROR' - only
   errors; 'WARN' - errors and warnings; 'INFO' - errors, warnings, information;
   'DEBUG' - errors, warnings, information, debug; 'TRACE' - errors, warnings,
   information, debug, trace;
   Default: WARN
--max-expiration-time
   Set expiration time in milliseconds. A certificate will be renewed only
   when your existed certificates will expire after max-expiration-time. By
   default it is 2592000000 milliseconds which is equal to 30 days. It means that
   when you call 'renew-certificate' command it will be renewed only if your
   certificate will expire in 30 days.
   Default: 2592000000
--newest-only
   If you want to download only the newest certificate you can use this
   parameter. Otherwise you will download all your certificates.
   Default: false
--one-dir-for-well-known
   By default well-known files will be placed separately for each domain (in
   different directories). Set this parameter if you want to generate all challenges
   in the same directory.
   Default: false
--server-url, -u
   Acme api URL. You can use debug api for example
   Default: https://acme-v01.api.letsencrypt.org/directory
--to-time
   When you revoke your certificates you can set this option. It means that
   all certificates which will expire till this date will be revoked (date in
   milliseconds GMT.)
   Default: 9223372036854775807
--well-known-dir
   Directory to place well-known files. All your well-known files must be
   accessible via link: http://${domain}/.well-known/acme-challenge/${token} where
   ${token} is a name of a file and ${domain} is your domain name
   Default: /var/acme_work_dir/well_known/
--with-agreement-update
   Automatically updates agreement to the newest. Some times provider can
   change an agreement and you should update the agreement link manually. (I.e. get
   an agreement url, read an agreement, update an agreement). If you want to
   automate this process you can use this parameter with each command you are using.
   You shouldn't use this parameter if you are not agree with a new agreement or
   if you use different commands very often.
   Default: false
--work-dir, -w
   Working directory to place authorization information and certificates
   information (i.e. files 'authorization_uri_list' and 'certificate_uri_list'). This
   files are public but you have to keep them safe to not lose them. Otherwise you
   have to contact your provider to recover your lost certificates (Or create new
   ones).
   Default: /var/acme_work_dir/
```

Every time you run acme_client you must set the parameter command parameter `--command`.<br>
Optional parameters for all commands are: `--log-dir`, `--log-level`, `--server-url`, `--with-agreement-update`, `--agreement-url`.<br>
For most of your commands you should specify working directory for your account (`--work-dir`) but you can left it by default.<br>
Every command returns a JSON object which always contains either `"status":"ok"` or `"status":"error"` and sometimes an additional information. You should check your log file if you get `"status":"error"`.

## Commands:

1. `register` - create a new account.<br>
    Requires parameters: `--account-key` <br>
    Optional parameters: `--email`

2. `get-agreement-url` - return a JSON object with `agreement_ur` key where value is the most up to date agreement. You must accept agreement if you want to use a service.<br>
    Requires parameters: `--account-key`

3. `update-agreement` - accept agreement. If you do not specify `--agreement-url` you will automatically agree with the newest agreement.<br>
    Requires parameters: `--account-key`<br>
    Optional parameters: `--agreement-url`

4. `add-email` - add email to your account (some providers can recover your email if you lose your account private key).<br>
    Requires parameters: `--account-key`, `--email`

5. `deactivate-account` - deactivate your account<br>
    Requires parameters: `--account-key`

6. `authorize-domains`  authorize specified domains. You must specify all domains which you use in CSR (i.e. main domain and alternative domain names).<br> If you get `"status":"error"` this command may include domains which were not authorized (`"failed_domains":["example.com", "blog.example.com"]`). You can see  the reason in your log file.<br>
    Requires parameters: `--account-key`, `--domain`<br>
    Optional parameters: `--challenge-type`

7. `deactivate-domain-authorization` - deactive domain authorization for specific domain address (or for all if not specified) if you want to remove/sell your domain addresses.<br> If you get `"status":"error"` this command may include domains which were not deactivated (`"failed_domains":["example.com", "blog.example.com"]`). You can see  the reason in your log file.<br>
    Requires parameters: `--account-key`<br>
    Optional parameters: `--domain`<br>
    Must have a file in working dir: `authorization_uri_list`

8. `download-challenges` - Download challenges from your authorizations.<br> If you get `"status":"error"` this command may include authorizations' locations from which challenges wasn't been downloaded (`"failed_authorizations_to_download":["https://...", "https://..."]`). You can see  the reason in your log file.<br>
    Requires parameters: `--account-key`<br>
    Optional parameters: `--domain`<br>
    Must have file in working dir: `authorization_uri_list`

9. `verify-domains` - Check your challenges and verify your domains.<br> If you get `"status":"error"` this command may include domains which were not verified (`"failed_domains":["example.com", "blog.example.com"]`). You can see  the reason in your log file.<br>
    Requires parameters: `--account-key`<br>
    Optional parameters: `--domain`<br>
    Must have a file in working dir: `authorization_uri_list`

10. `generate-certificate` - Generate new certificate.<br>
     Requires parameters: `--account-key`, `--csr`<br>
     Optional parameters: `--cert-dir`

11. `download-certificate` - Download your certificates which you have created earlier. If you specify `--newest-only` then you will download only newest certificate. Without that parameter you will download all certificates sorted by expiration date (i.e. `cert_0.pem` is the newest and `cert_15.pem` is the oldest).<br>
     Requires parameters: `--account-key`<br>
     Optional parameters: `--newest-only`<br>
     Must have a file in working dir: `certificate_uri_list`

12. `revoke-certificate` - revoke certificates. You can revoke either all your certificates or by time criteria. All certificates will be removed which are started after `--from-time` and which will be expired till `--to-time`. These parameters are written as GMT milliseconds.<br> If you get `"status":"error"` this command may include certificates' locations which were not revoked (`"failed_certificates":["https://...", "https://..."]`). You can see  the reason in your log file.<br>
     Requires parameters: `--account-key`<br>
     Optional parameters: `--from-time`, `--to-time`<br>
     Must have a file in working dir: `certificate_uri_list`

13. `renew-certificate` - Renew certificate either for existing CSR or for new CSR. Will create a new certificate only if all your certificates will expire after `--max-expiration-time`. `--max-expiration-time` is a time written in milliseconds (By default it is 2592000000 which is equal to 30 days).<br>
     Requires parameters: `--account-key`, `--csr`<br>
     Optional parameters: `--cert-dir`, `--max-expiration-time`, `--force`

# Example scenarios:
## Get a certificate for domains: 
`example.com`, `www.example.com`, `admin.example.com`, `www.admin.example.com`

1. generate a private account key

  ```
  openssl genrsa -out account.key 2048
  ```
  
2. generate a private domain key
  
  ```
  openssl genrsa -out example.com.key 2048 
  ```
  
3. Configure openssl to use alternative names. If you have only one domain you should skip this section

  1. Copy `openssl.cnf` config to your custom location. For example on CentOS 7:
    
    ```
    cp /etc/pki/tls/openssl.cnf openssl.cnf
    ```
    
  2. Edit your `openssl.cnf` file and change next parameters (you should create your own `[alt_names]`). Add only alternative name to your `[alt_names]`, do not put your main domain (i.e. example.com) into your `[alt_names]`:
  
    ```
    [ req ]
    req_extensions = v3_req

    [ v3_req ]
    basicConstraints = CA:FALSE
    keyUsage = nonRepudiation, digitalSignature, keyEncipherment
    subjectAltName = @alt_names

    [ v3_ca ]
    subjectAltName = @alt_names

    [alt_names]
    DNS.1 = www.example.com
    DNS.2 = admin.example.com
    DNS.3 = www.admin.example.com
    ```

4. generate a csr (Certificate Signing Reques) based on the private domain key
  
  ```
  openssl req -new -key example.com.key -sha256 -nodes \
  -subj '/C=US/ST=Delaware/L=Wilmington/O=My company/OU=IT Department/CN=example.com/emailAddress=support@example.com' \
  -config openssl.cnf -outform PEM -out example.com.csr
  ```
    
5. register your account

  ```
  java -jar acme_client.jar -a /path/to/account.key --command register --with-agreement-update --email admin@example.com
  ```
  
6. authorize domains

  ```
  java -jar acme_client.jar -a /path/to/account.key -w /path/to/workdir/ \
  --command authorize-domains -d example.com -d www.example.com -d admin.example.com -d www.admin.example.com \
  --well-known-dir /path/to/wellknown --one-dir-for-well-known
  ```

7. Copy your files from /path/to/wellknown directory to your server so that they can be retrivable via the next link:
`http://${domain}/.well-known/acme-challenge/${token}` where `${token}` is a name of a file and `${domain}` is your domain name.<br>
For example you can use next teqniue:

  1. Crete directory for your server and subdirectories for wellknown files
    
    ```
    mkdir -p /tmp/public_html/.well-known/acme-challenge
    ```
  
  2. Enter your server directory
    
    ```
    cd /tmp/public_html
    ```
    
  3. Copy your wellknown files into your server location under `.well-known/acme-challenge/`:
  
    ```
    cp /path/to/wellknown/* /tmp/public_html/.well-known/acme-challenge/
    ```
  
  4. Run your server:
  
    ```
    $(command -v python2 || command -v python2.7 || command -v python2.6) -c \
    "import BaseHTTPServer, SimpleHTTPServer; \
    s = BaseHTTPServer.HTTPServer(('', 80), SimpleHTTPServer.SimpleHTTPRequestHandler); \
    s.serve_forever()"
    ```

8. verify domains

  ```
  java -jar acme_client.jar -a /path/to/account.key -w /path/to/workdir/ \
  --command verify-domains -d example.com -d www.example.com -d admin.example.com -d www.admin.example.com
  ```
  
9. generate a certificates

  ```
  java -jar acme_client.jar -a /path/to/account.key -w /path/to/workdir/ \
  --command generate-certificate --csr /path/to/example.com.csr --cert-dir /path/to/cert/
  ```
  
10. Done. You should see next files in `/path/to/cert/` directory: `cert.pem`, `chain.pem`, `fullchain.pem`
