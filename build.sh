#!/bin/bash

sudo apt-get update
sudo apt-get install default-jdk
sudo apt-get install gradle

sudo chmod +x ./gradlew

./gradlew test
if [ $? -ne 0 ]; then
    echo "Test Error"
    exit 1
fi

./gradlew installDist


if [ $? -ne 0 ]; then
    echo "Error"
    exit 1
fi