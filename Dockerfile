FROM gradle:7.5.1-alpine
ADD . /java/
WORKDIR /java
RUN gradle clean build -x test

FROM 289631370416.dkr.ecr.us-east-2.amazonaws.com/java:openjdk1.8.1.27
VOLUME /opt/logs
COPY application.yml /opt/application.yml
COPY application-pro.yml /opt/application-pro.yml
COPY --from=0 /java/node/build/libs/node-v1.0.jar /opt/node-v1.0.jar
RUN sh -c 'touch /opt/node-v1.0.jar'
RUN     mkdir -p /opt/logs && \
        chown -R root.nobody /opt/node-v1.0.jar && \
        chown -R nobody.nobody /opt/logs && \
        chmod 640 -R /opt/node-v1.0.jar
USER nobody
EXPOSE 8019
ENTRYPOINT ["java","-jar","/opt/node-v1.0.jar","--spring.config.location=/opt/application.properties","--key /opt/conf/key.store","--vrfKey /opt/conf/vrfKeyStore.yml"]
