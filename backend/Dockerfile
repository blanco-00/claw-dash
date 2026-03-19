FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy settings.xml for Chinese Maven mirror
COPY settings.xml /root/.m2/settings.xml

# Install Maven 3.9.x manually (apt-get version is too old for annotationProcessorPaths)
RUN apt-get update && apt-get install -y wget && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -O /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.6/bin/mvn /usr/bin/mvn && \
    rm /tmp/maven.tar.gz && \
    apt-get clean

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 5178

ENTRYPOINT ["java", "-jar", "app.jar"]
