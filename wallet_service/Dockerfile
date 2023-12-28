FROM openjdk:11
VOLUME /tmp
ADD build/libs/*.jar wallet_service.jar
ENTRYPOINT ["java", "-jar", "wallet_service.jar"]
