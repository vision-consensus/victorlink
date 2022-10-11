FROM gradle:6.6.1-hotspot
ADD . /java/
WORKDIR /java
RUN gradle clean build -x test

FROM 289631370416.dkr.ecr.us-east-2.amazonaws.com/java:openjdk1.8.1.27
VOLUME /opt/logs
VOLUME /opt/conf
COPY --from=0 /java/node/build/libs/node-v1.0.jar /opt/node-v1.0.jar
RUN sh -c 'touch /opt/node-v1.0.jar'
RUN     mkdir -p /opt/logs && \
        mkdir -p /opt/conf && \
        chown -R root.nobody /opt/node-v1.0.jar && \
        chown -R nobody.nobody /opt/logs && \
        chown -R nobody.nobody /opt/conf && \
        chmod 640 -R /opt/node-v1.0.jar
USER nobody
EXPOSE 8060
ENTRYPOINT ["java","-jar","/opt/node-v1.0.jar","-k","/opt/conf/key.store","-vrfK","/opt/conf/vrfKeyStore.yml","-e","vpioneer"]
