# Instructions for developers

###Localhost with local mongoDB. 

Use those args :

> -Dspring.profiles.active=dev -DFTP_USERUSERNAME=crusoe -DFTP_PASSWORD=xxx -DCLIENT_ID=xxx -DCLIENT_SECRET=xxx -DMONGODB_USERNAME=crusoe -DMONGODB_PASSWORD=xxx -DSIGNIN_KEY=xxx -DTOKEN_VALIDITY=xxx -DTOKEN_ACCESS_REQUEST_VALIDITY=xxx

* MongoDB: your application will point to local MongoDB. Make sure those follow command have been run on Robo3T or other tools: 

> use certifymyrepo

> db.createUser({user: "crusoe", pwd: "xxx",roles: [ "readWrite", "dbAdmin" ]})
     
* EmailSender: **TestEmailSender.java** is used to route notification to the hard coded email in this class. It's activated with dev profile.


* Dev swagger: ``http://localhost:8485/swagger-ui.html``


###Localhost with remote preprod mongoDB.

* ssh tunnel

> ssh -p 2222 wwwadm@sedur.sedoo.fr -L 27018:sedur.sedoo.fr:27017

* then use:

> -Dspring.profiles.active=dev,tunnelssh-preprod -DFTP_USERUSERNAME=crusoe -DFTP_PASSWORD=xxx -DCLIENT_ID=xxx -DCLIENT_SECRET=xxx -DMONGODB_USERNAME=crusoe-preprod -DMONGODB_PASSWORD=xxx -DSIGNIN_KEY=xxx -DTOKEN_VALIDITY=xxx -DTOKEN_ACCESS_REQUEST_VALIDITY=xxx


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




# Prod Deployment instructions (v1)

* Run the following maven command on eclipse

> mvn clean install

* Open a terminal on your local computer

> cd /data/git/certifymyrepo (or the path on your machine)

> scp ./sedoo-certifymyrepo-rest-0.0.1-SNAPSHOT.jar wwwadm@twodoo:/export1/certifymyrepo/services/sedoo-certifymyrepo-rest.jar

> [Enter twodoo password]

* Open a terminal on twodoo

> service sedoo-certifymyrepo-rest restart


# Preprod Deployment instructions (v2)

* Run the following maven command on eclipse

> mvn clean install

* Open a terminal on your local computer

> scp ./target/sedoo-certifymyrepo-rest-0.0.1-SNAPSHOT.jar  wwwadm@twodoo.sedoo.fr:/export1/crusoe-preprod/services/crusoe-rest.jar
> [Enter twodoo password]

* Open a terminal on twodoo

> service crusoe-preprod restart


#Notes

Spring profile is set to **prod** in ``/export1/certifymyrepo/services/sedoo-certifymyrepo-rest.conf``

Spring profile is set to **preprod** in ``/export1/crusoe-preprod/services/crusoe-rest.conf``

**logback-spring.xml** provide file rolling when prod profile is activated and classic console otherwise.

Prod swagger: ``https://services.sedoo.fr/certifymyrepo/swagger-ui.html``

Preprod swagger ``https://services.sedoo.fr/crusoe-preprod/swagger-ui.html``
