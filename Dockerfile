FROM openjdk:8-jdk-alpine
MAINTAINER sedoo
RUN apk add --no-cache openjdk8
COPY ./files/UnlimitedJCEPolicyJDK8/* /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/
COPY ./target/sedoo-certifymyrepo-rest-0.0.1-SNAPSHOT.jar certifymyrepo-rest.jar
ENTRYPOINT ["java","-jar","/certifymyrepo-rest.jar"]