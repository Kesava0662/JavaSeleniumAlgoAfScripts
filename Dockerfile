# Use Amazon Corretto 11 as base image
FROM amazoncorretto:11

# Set maintainer (optional but good practice)
LABEL maintainer="kesava"

# Set environment to avoid interactive prompts during build
ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:99

# Install dependencies for Selenium, Maven, and headless Chrome
# Using 'dnf' for Amazon Linux 2 (which Corretto is based on)
# Consolidating RUN commands to reduce image layers
RUN yum update -y && \
    yum install -y \
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
    # Add nss and libgbm which are often required for Chrome in headless environments
    nss \
    libgbm \
    && yum clean all \
    && rm -rf /var/cache/yum

# Install Google Chrome
# It's often better to install Chrome directly from Google's repository for stability and updates.
# This also handles potential dependency issues more gracefully.
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_x86_64.rpm && \
    yum localinstall -y google-chrome-stable_current_x86_64.rpm && \
    rm google-chrome-stable_current_x86_64.rpm && \
    ln -sf /usr/bin/google-chrome-stable /usr/bin/google-chrome && \
    ln -sf /usr/bin/google-chrome-stable /usr/bin/chrome

# Set working directory
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download all dependencies to speed up rebuilds
# It's better to use `mvn package -DskipTests` here to also download plugins
# and prepare the build environment more completely.
RUN mvn dependency:go-offline || true # `|| true` to prevent build failure if some dependencies are not found yet (unlikely here)

# Copy rest of the project files
COPY src ./src
COPY lib ./lib

# Install local JAR dependencies
# Consolidating these into a single RUN command to reduce image layers.
# Ensure that these JARs are indeed needed as local dependencies and not managed by Maven.
# If these are meant to be project dependencies, they should ideally be in pom.xml.
RUN mvn install:install-file -Dfile=lib/JiraIntegration.jar -DgroupId=com.jira -DartifactId=jira-integration -Dversion=1.0 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/org.apache.log44j_1.2.15.v201012070815.jar -DgroupId=org.apache.log4j -DartifactId=log4j -Dversion=1.2.15 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/poi-3.17.jar -DgroupId=org.apache.poi -DartifactId=poi -Dversion=3.17 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/poi-ooxml-3.17.jar -DgroupId=org.apache.poi -DartifactId=poi-ooxml -Dversion=3.17 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/poi-ooxml-schemas-3.17.jar -DgroupId=org.apache.poi -DartifactId=poi-ooxml-schemas -Dversion=3.17 -Dpackaging=jar && \
    mvn install:install-file -Dfile=lib/xmlbeans-2.6.0.jar -DgroupId=org.apache.xmlbeans -DartifactId=xmlbeans -Dversion=2.6.0 -Dpackaging=jar

# Build the project (compile classes and create JAR)
# Use `mvn clean install` to build the project and install it to the local Maven repository.
# This will also create the `target` directory with the compiled classes and JAR.
RUN mvn clean install -DskipTests

# Install chromedriver manually
# Ensure the chromedriver version matches your Chrome browser version.
# A common practice is to download chromedriver dynamically based on the Chrome version,
# but for a fixed image, this approach is fine as long as you update it.
COPY src/test/resources/chromedriver /usr/local/bin/chromedriver
RUN chmod +x /usr/local/bin/chromedriver

# Expose app/test results port if applicable (optional)
EXPOSE 8080

# Define entrypoint for better command control and signal handling
# This allows the CMD to be overridden easily.
ENTRYPOINT ["/bin/bash", "-c"]

# Start Xvfb (for headless Chrome) and run MainApp
# Using `exec` in the final command ensures that signals are passed correctly to the Java process.
# Consider using `target/<your-app-name>.jar` if your build creates an executable JAR.
# If `runner.MainApp` is a class, the classpath needs to include the generated JAR.
# If `target/classes` contains your compiled code, then this classpath is mostly fine.
# For better clarity, you could explicitly add the project's compiled JAR to the classpath.
CMD Xvfb :99 -screen 0 1024x768x16 & \
    java -cp "target/classes:target/test-classes:lib/*:target/dependency/*:/app/target/*.jar" runner.MainApp
