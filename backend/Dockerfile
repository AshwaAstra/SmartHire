FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:25-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-venv \
    && python3 -m venv /opt/speech \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/recruitment-platform-0.0.1-SNAPSHOT.jar app.jar
COPY requirements-speech.txt transcribe_audio.py ./
RUN /opt/speech/bin/pip install --no-cache-dir -r requirements-speech.txt
ENV SPEECH_PYTHON_LAUNCHER=/opt/speech/bin/python
ENV SPEECH_PYTHON_VERSION=
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
