#!/bin/bash

echo "Starting WebAPI with Broadsea configuration..."
mvn spring-boot:run -Dspring-boot.run.profiles=broadsea
