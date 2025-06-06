# Use Amazon Corretto 11 as base image
FROM amazoncorretto:11

# Set environment to avoid interactive prompts during build
ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:99

# Install dependencies for Selenium, Maven, and headless Chrome
RUN yum install -y \
    unzip \
    wget \
    xorg-x11-server-Xvfb \
    gtk3 \
    libXScrnSaver \
    GConf2 \
    alsa-lib \
    maven \
    which \
    fontconfig \
    && yum clean all

# Install Google Chrome
RUN curl -sSL https://intoli.com/install-google-chrome.sh | bash && \
    ln -sf /usr/bin/google-chrome-stable /usr/bin/google-chrome && \
    ln -sf /usr/bin/google-chrome-stable /usr/bin/chrome

# Set working directory
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download all dependencies to speed up rebuilds
RUN mvn dependency:go-offline

# Copy rest of the project files
COPY src ./src
COPY lib ./lib

# Install local JAR dependencies
RUN mvn install:install-file -Dfile=lib/JiraIntegration.jar -DgroupId=com.jira -DartifactId=jira-integration -Dversion=1.0 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/org.apache.log4j_1.2.15.v201012070815.jar -DgroupId=org.apache.log4j -DartifactId=log4j -Dversion=1.2.15 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/poi-3.17.jar -DgroupId=org.apache.poi -DartifactId=poi -Dversion=3.17 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/poi-ooxml-3.17.jar -DgroupId=org.apache.poi -DartifactId=poi-ooxml -Dversion=3.17 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/poi-ooxml-schemas-3.17.jar -DgroupId=org.apache.poi -DartifactId=poi-ooxml-schemas -Dversion=3.17 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/xmlbeans-2.6.0.jar -DgroupId=org.apache.xmlbeans -DartifactId=xmlbeans -Dversion=2.6.0 -Dpackaging=jar

# Build the project (compile classes)
RUN mvn clean compile

# Install chromedriver manually (you can automate versioning if needed)
COPY src/test/resources/chromedriver /usr/local/bin/chromedriver
RUN chmod +x /usr/local/bin/chromedriver

# Expose app/test results port if applicable (optional)
EXPOSE 8080

# Start Xvfb (for headless Chrome) and run MainApp
CMD Xvfb :99 -screen 0 1024x768x16 & \
    java -cp "target/classes:target/test-classes:lib/*:target/dependency/*" runner.MainApp
