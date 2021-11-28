export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home; java -version

mvn package -DskipTests -Pjlink-image

# Build image
if [ -z "$1" ]
then # no argument supplied
    docker build -t tanger46/shopping-service -f Dockerfile.jlink .
else
    docker build -t arm64v8/tanger46/shopping-service -f Dockerfile.jlink.arm64v8 .
fi