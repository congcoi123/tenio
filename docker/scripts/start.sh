#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# Start

cd ..

cd ..

echo "-----------------------------------------------------------------------------------------------"

docker-compose down

docker-compose up -d

docker-compose ps

echo "-----------------------------------------------------------------------------------------------"
