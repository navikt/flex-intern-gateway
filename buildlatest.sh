echo "Bygger flex-intern-gateway latest"

./gradlew bootJar

docker build . -t flex-intern-gateway:latest
