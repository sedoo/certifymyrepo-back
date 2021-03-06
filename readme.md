# Instructions for developers

# Table of Contents

1. [Spring boot application](#1-spring-boot-application)
    - 1.1 [Project setup](#1.1-project-setup)
    - 1.2 [Localhost with remote preprod mongoDB](#1.2-localhost-with-remote-preprod-mongodb)
    - 1.3 [Deploy as init.d service](#1.3-deploy-as-init.d-service)
    - 1.4 [Notes](#notes)
2. [Dockerized application](#2-dockerized-application)   
3. [Environment variables description and authentication instructions](#3-environment-variables-description-and-authentication-instructions)   

# 1 Spring boot application

You must have a mongoDB installed and a FTP server access

### 1.1 Project setup

```
mvn clean install
```

Compiles and launch application on eclipse for development use those args :

```
-Dspring.profiles.active=dev 
-DFTP_PASSWORD=xx 
-DCLIENT_ID=xx 
-DCLIENT_SECRET=xx
-DMONGODB_PASSWORD=xx
-DSUPER_ADMIN_ORCID_LIST=xx,xx
-DADMIN_ORCID_LIST=xx,xx,xx
-DSIGNIN_KEY=xxx 
-DTOKEN_VALIDITY=xxx 
-DTOKEN_ACCESS_REQUEST_VALIDITY=xxx
```

or add the file **application-local.yml** in src/main/resources with this conent (cannot be commited):

```
FTP_PASSWORD: xx
CLIENT_ID: xx
CLIENT_SECRET: xx
MONGODB_PASSWORD: xx
SUPER_ADMIN_ORCID_LIST: xx,xx
ADMIN_ORCID_LIST: xx,xx,xx
SIGNIN_KEY: xxx 
TOKEN_VALIDITY: xxx 
TOKEN_ACCESS_REQUEST_VALIDITY: xxx
```

MongoDB: your application will point to local MongoDB. Make sure those follow command have been run on Robo3T or other tools: 

> use certifymyrepo

> db.createUser({user: "crusoe", pwd: "xxx",roles: [ "readWrite", "dbAdmin" ]})
     
EmailSender: **TestEmailSender.java** is used to route notification to the hard coded email in this class. It's activated with dev profile.


Dev swagger: ``http://localhost:8485/swagger-ui.html``


### 1.2 Localhost with remote preprod mongoDB.

ssh tunnel

> ssh -p host-port username@host -L port-y:host:port-x

then use the spring profile **distant-preprod**



### 1.3 Deploy as init.d service

> scp ./target/sedoo-certifymyrepo-rest-0.0.1-SNAPSHOT.jar username@host:/export1/crusoe-preprod/services/crusoe-rest.jar


Reboot

> service crusoe-preprod restart

Logs

> tail -f /export1/crusoe-preprod/logs/crusoe-preprod.log 


More information on: [Installation as an init.d Service (System V)](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment.installing.nix-services.init-d)


### 1.4 Notes

Spring profile is set to **prod** in ``/export1/certifymyrepo/services/sedoo-certifymyrepo-rest.conf``

Spring profile is set to **preprod** in ``/export1/crusoe-preprod/services/crusoe-rest.conf``

**logback-spring.xml** provide file rolling when prod profile is activated and classic console otherwise.

Prod swagger: ``https://services.sedoo.fr/certifymyrepo/swagger-ui.html``

Preprod swagger ``https://services.sedoo.fr/crusoe-preprod/swagger-ui.html``


# 2 Dockerized application

### 2.1 Publish image with maven

The following command will publish the image on DockerHub:

> mvn compile jib:build

### 2.1 Run

The application can be deployed using docker compose with the following command:
> docker-compose --env-file ./.env.dev up --build

The two files below must be present in the root folder

.env.dev file:

```
FTP_PASSWORD=*
CLIENT_ID=*
CLIENT_SECRET=*
SIGNIN_KEY=*
TOKEN_VALIDITY=*
TOKEN_ACCESS_REQUEST_VALIDITY=*
MONGODB_PASSWORD=*
SUPER_ADMIN_ORCID_LIST=*,*
ADMIN_ORCID_LIST=*,*,*
EMAIL_NOTIFICATION_DEV=*
```

mongo-init.js:

```JS
db = db.getSiblingDB('crusoe');
db.createUser(
        {
            user: "**********",
            pwd: "**********",
            roles: [
                {
                    role: "readWrite",
                    db: "dbAdmin"
                }
            ]
        }
);
```

# 3 Environment variables description and authentication instructions



| Variable name  | Description | Type |
| :--------------- |:---------------:| :---------------: | 
| spring.profiles.active  		| spring profile     |  		| 
| FTP_USERUSERNAME 	| FTP server user name. This server is used to store reports attachments 	|  	| 
| FTP_PASSWORD  		| FTP server password       |  		| 
| MONGODB_PASSWORD 		| MongoDB password       |  		| 
| CLIENT_ID  		|   ORCID Client id    |  		| 
| CLIENT_SECRET  		|  ORCID Client Secret     |  		| 
| SIGNIN_KEY  		|   JWT signin key     |  		| 
| TOKEN_VALIDITY  		| Token validity when an user is logged on the website. This token is refreshed as long as the user is using the website.   | Number in ms 		| 
| TOKEN_ACCESS_REQUEST_VALIDITY  		| Token validity used when a request has been made to access a repository. An email is sent with a link to accept the request. This token must have a longer validity       |  Number in ms		| 

### ORCID sign in
The application use ORCID authentication and has been registered as redirect URL. **CLIENT_ID** and **CLIENT_SECRET** values come from orcid.
For more information (Get an Authenticated ORCID iD)[https://info.orcid.org/documentation/api-tutorials/api-tutorial-get-and-authenticated-orcid-id/#easy-faq-2606]

### JSON Web Tokens
JSON Web Token (JWT) is an open standard (RFC 7519) that defines a way for securely transmitting information between parties as a JSON object. It is used to securized communication between the spring boot backend application and the VueJS frontend application. The tree variables used are **SIGNIN_KEY**, **TOKEN_VALIDITY**, **TOKEN_ACCESS_REQUEST_VALIDITY**.




