FROM openjdk:11
VOLUME /tmp
ADD build/libs/*.jar warehouse_service.jar
ENTRYPOINT ["java", "-jar", "warehouse_service.jar"]
