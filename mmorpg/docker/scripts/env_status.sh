#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# Check the current docker running containers' status

cd ..

cd ..

echo "---------------------------------------------------------------------------------------------------"
docker-compose ps
echo "---------------------------------------------------------------------------------------------------"

echo "【＊】Checking Docker execution status"
echo "・The state of [Up] is Docker setting complete"
echo "・If there is a container with no [Up] status, it means that some errors have occurred"
