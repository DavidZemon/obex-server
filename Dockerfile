# Use ubuntu as base image instead of openjdk and maven because it is the only
# one of these three that supports ARM and x86

FROM ubuntu:20.04 as builder

RUN apt-get update && apt-get install --yes --no-install-recommends \
        openjdk-11-jre-headless \
        maven

COPY . /opt/app
RUN mvn -f /opt/app/pom.xml clean package

FROM ubuntu:focal-20200720

RUN apt-get update && apt-get install --yes --no-install-recommends \
        git-core \
        openjdk-11-jre-headless \
    && rm --recursive --force /var/lib/apt/lists/*

ENV HOME=/home/captain

RUN groupadd --gid 1000 captain \
    && useradd --home-dir "${HOME}" --uid 1000 --gid 1000 captain

COPY --from=builder /opt/app/target/obex-server-*.jar /opt/app.jar
COPY scripts/docker-start.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 8080
