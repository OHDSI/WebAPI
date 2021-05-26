# Stage 1. Create builder container and build sources
FROM maven:3.6.3-jdk-8 as builder

# Use bash
SHELL ["/bin/bash", "-c"]

# Install packages
RUN apt -y update && apt install -y git zip unzip curl wget make

# Make ssh dir
RUN mkdir /root/.ssh/

# Copy over private key, and set permissions
# Warning! Anyone who gets their hands on this image will be able
# to retrieve this private key file from the corresponding image layer
ADD ../../id_rsa /root/.ssh/id_rsa
RUN chmod 0600 /root/.ssh/id_rsa

# Create known_hosts
RUN touch /root/.ssh/known_hosts
# Add bitbuckets key
RUN ssh-keyscan bitbucket.org >> /root/.ssh/known_hosts

# Install Node 8
RUN curl -sL https://deb.nodesource.com/setup_8.x | bash -
RUN apt install -y nodejs
RUN apt install -y npm

RUN mkdir /app
WORKDIR /app/

# Clone repos
RUN git clone --branch versioning git@bitbucket.org:Odysseus/webapi.git
RUN git clone --branch versioning git@bitbucket.org:Odysseus/atlas.git

# Build WebAPI
WORKDIR /app/webapi/
RUN mvn install -e -Dmaven.test.skip -Pwebapi-postgresql

# Build Atlas
WORKDIR /app/atlas/
RUN npm run build

# Stage 2. Build ATLAS Docker image
FROM tomcat:8-jdk8-openjdk-buster

ENV RUNLEVEL=1

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y nano apt-utils dos2unix cron curl vim unzip wget rsyslog logrotate
RUN update-rc.d cron defaults && update-rc.d rsyslog defaults

COPY run.sh /bin/run.sh
RUN dos2unix /bin/run.sh

WORKDIR /usr/local/tomcat/webapps/
COPY --from=builder /app/webapi/target/WebAPI.war .

WORKDIR /usr/local/tomcat/webapps/ROOT/
COPY --from=builder /app/atlas/. .

VOLUME /usr/local/tomcat/webapps/ROOT/config

RUN chmod +x /bin/run.sh

EXPOSE 8080

CMD ["/bin/run.sh"]