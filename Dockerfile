FROM maven:3-eclipse-temurin-17 as build

COPY src /build/src
COPY pom.xml /build/
WORKDIR /build

RUN apt-get update && \
    apt-get -y install build-essential zlib1g-dev && \
    mkdir /opt/graalvm && \
    curl -o /opt/graalvm/install.sh -sL https://get.graalvm.org/jdk && \
    chmod +x /opt/graalvm/install.sh && \
    /opt/graalvm/install.sh --to /opt/graalvm --no-progress

RUN export GRAALVM_HOME=`find /opt/graalvm -mindepth 1 -maxdepth 1 -type d` && \
    mvn -T1C -Pnative clean native:compile

FROM ubuntu

COPY --from=build /build/target/inboxmove /inboxmove

USER nobody

CMD ["/inboxmove"]
