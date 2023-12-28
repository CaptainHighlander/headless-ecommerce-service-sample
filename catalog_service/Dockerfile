FROM openjdk:11
VOLUME /tmp
ADD build/libs/*.jar catalog_service.jar
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "catalog_service.jar"]
