# Use lightweight Java 21
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY . .

# Compile
RUN javac LoadBalancer.java

# Run
CMD ["java", "LoadBalancer"]