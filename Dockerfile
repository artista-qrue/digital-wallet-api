FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copy the Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the mvnw script executable
RUN chmod +x mvnw

# Download dependencies and cache them in a separate layer
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application with the production profile
RUN ./mvnw package -DskipTests -Pprod
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Use a smaller base image for the runtime environment
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the exploded application from the build stage
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Expose the application port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Define health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java","-cp","app:app/lib/*","com.wallet.api.WalletApiApplication"] 