# Instructions for developers

#1 Application as service

You must have a mongoDB installed and a FTP server access

###1.1 Project setup

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


###1.2 Localhost with remote preprod mongoDB.

ssh tunnel

> ssh -p 2222 wwwadm@sedur.sedoo.fr -L 27018:sedur.sedoo.fr:27017

then use the spring profile **tunnelssh-preprod**



###1.3 Deploy as init.d service (System V)
```
scp ./target/sedoo-certifymyrepo-rest-0.0.1-SNAPSHOT.jar wwwadm@twodoo.sedoo.fr:/export1/crusoe-preprod/services/crusoe-rest.jar
```
Reboot

```
service crusoe-preprod restart
```

Logs

```
tail -f /export1/crusoe-preprod/logs/crusoe-preprod.log 
```

More information on: [Installation as an init.d Service (System V)](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment.installing.nix-services.init-d)


###1.5 Notes

Spring profile is set to **prod** in ``/export1/certifymyrepo/services/sedoo-certifymyrepo-rest.conf``

Spring profile is set to **preprod** in ``/export1/crusoe-preprod/services/crusoe-rest.conf``

**logback-spring.xml** provide file rolling when prod profile is activated and classic console otherwise.

Prod swagger: ``https://services.sedoo.fr/certifymyrepo/swagger-ui.html``

Preprod swagger ``https://services.sedoo.fr/crusoe-preprod/swagger-ui.html``


#2 Dockerized application

The application can be deployed using docker compose with the following command:
> docker-compose --env-file ./.env.dev up --build

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

#3 Authentication instructions



| Variable name  | Description | Type |
| :--------------- |:---------------:| :---------------: | 
| spring.profiles.active  		| spring profile     |  		| 
| FTP_USERUSERNAME 	| FTP server user name. This server is used to store reports attachments 	|  	| 
| FTP_PASSWORD  		| FTP server password       |  		| 
| CLIENT_ID  		|   ORCID Client id    |  		| 
| CLIENT_SECRET  		|  ORCID Client Secret     |  		| 
| SIGNIN_KEY  		|   JWT signin key     |  		| 
| TOKEN_VALIDITY  		| Token validity when an user is logged on the website. This token is refreshed as long as the user is using the website.   | Number in ms 		| 
| TOKEN_ACCESS_REQUEST_VALIDITY  		| Token validity used when a request has been made to access a repository. An email is sent with a link to accept the request. This token must have a longer validity       |  Number in ms		| 



