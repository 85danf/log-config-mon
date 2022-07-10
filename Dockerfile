FROM azul/zulu-openjdk-alpine:17 AS builder
RUN apk add maven
ADD src /tmp/configmon/src
ADD pom.xml /tmp/configmon/pom.xml
WORKDIR /tmp/configmon/
RUN mvn dependency:resolve
RUN mvn package -DskipTests

FROM azul/zulu-openjdk-alpine:17
MAINTAINER 85danf@gmail.com

RUN install -d -m 0755 /opt/configmon/
WORKDIR /opt/configmon/
COPY --from=builder /tmp/configmon/target/config-mon-*.jar config-mon.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","config-mon.jar"]
