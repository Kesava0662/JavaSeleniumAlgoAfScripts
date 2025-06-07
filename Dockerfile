successful built image content  in Deep VM

# Use Amazon Corretto 11 as base image
FROM amazoncorretto:11

# Install dependencies for Selenium and browsers
RUN yum install -y \
    unzip \
    wget \
    xorg-x11-server-Xvfb \
    gtk3 \
    libXScrnSaver \
    GConf2 \
    alsa-lib \
    maven

# Install Chrome
RUN curl https://intoli.com/install-google-chrome.sh | bash && \
    ln -s /usr/bin/google-chrome-stable /usr/bin/chrome

# Set working directory
WORKDIR /app

# Copy only the POM first for dependency caching
COPY pom.xml .

# Download all dependencies first (creates layer cache)
RUN mvn dependency:go-offline

# Now copy the rest of the project
COPY src ./src
COPY lib ./lib

# Install all local JAR dependencies
RUN mvn install:install-file \
    -Dfile=lib/JiraIntegration.jar \
    -DgroupId=com.jira \
    -DartifactId=jira-integration \
    -Dversion=1.0 \
    -Dpackaging=jar && \
    mvn install:install-file \
    -Dfile=lib/org.apache.log4j_1.2.15.v201012070815.jar \
    -DgroupId=org.apache.log4j \
    -DartifactId=log4j \
    -Dversion=1.2.15 \
    -Dpackaging=jar && \
    mvn install:install-file \
    -Dfile=lib/poi-3.17.jar \
    -DgroupId=org.apache.poi \
    -DartifactId=poi \
    -Dversion=3.17 \
    -Dpackaging=jar && \
    mvn install:install-file \
    -Dfile=lib/poi-ooxml-3.17.jar \
    -DgroupId=org.apache.poi \
    -DartifactId=poi-ooxml \
    -Dversion=3.17 \
    -Dpackaging=jar && \
    mvn install:install-file \
    -Dfile=lib/poi-ooxml-schemas-3.17.jar \
    -DgroupId=org.apache.poi \
    -DartifactId=poi-ooxml-schemas \
    -Dversion=3.17 \
    -Dpackaging=jar && \
    mvn install:install-file \
    -Dfile=lib/xmlbeans-2.6.0.jar \
    -DgroupId=org.apache.xmlbeans \
    -DartifactId=xmlbeans \
    -Dversion=2.6.0 \
    -Dpackaging=jar

# Build the project with all dependencies
RUN mvn clean compile

# Set up chromedriver
COPY src/test/resources/chromedriver /usr/local/bin/chromedriver
RUN chmod +x /usr/local/bin/chromedriver

# Set up Xvfb for headless execution
ENV DISPLAY=:99

# Expose port
EXPOSE 8080

# Entry point to start Xvfb and run MainApp
CMD Xvfb :99 -screen 0 1024x768x16 & \
    java -cp "target/classes:target/test-classes:lib/*:target/dependency/*" runner.MainApp
