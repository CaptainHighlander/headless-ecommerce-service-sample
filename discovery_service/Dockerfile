FROM openjdk:11
VOLUME /tmp
ADD build/libs/*.jar discovery_service.jar
ENV JAVA_OPTS=""
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery_service.jar"]
