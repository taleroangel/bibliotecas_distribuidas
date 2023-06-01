#!/usr/bin/env bash

# Prepare mvn dependencies
mvn compile
mvn package

# Copy to containers folder
cp ./target/bibliotecas_distribuidas-1.0-manager.jar ./containers/manager
mv ./containers/manager/*.jar ./containers/manager/main.jar

cp ./target/bibliotecas_distribuidas-1.0-worker.jar ./containers/worker
mv ./containers/worker/*.jar ./containers/worker/main.jar