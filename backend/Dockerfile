FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/rookies.oam*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]