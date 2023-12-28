FROM openjdk:11
VOLUME /tmp
ADD build/libs/*.jar order_service.jar
ENTRYPOINT ["java", "-jar", "order_service.jar"]
