#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# Start setup

cd ..

cd ..

docker-compose build

docker-compose up -d

docker-compose ps

echo "-----------------------------------------------------------------------------------------------"

RUNNING="true,"
WEB_RUNNING="$(docker inspect sample_web | grep "Running" | awk '$1 ~ /^"Running":$/ { print $2 }')"
APP_RUNNING="$(docker inspect sample_app | grep "Running" | awk '$1 ~ /^"Running":$/ { print $2 }')"

if [[ -z "${APP_RUNNING}" ]]
then
	# Not existed
	echo "【error】The application container has not existed !"
else
	# Existed
	echo "【done】The application container has existed"
	if [[ "${RUNNING}" = "${APP_RUNNING}" ]]
	then
		echo "【done】The application is running"
		if [[ -z "${WEB_RUNNING}" ]]
			then
				# Not existed
				echo "【error】Nginx container has not existed !"
		else
			# Existed
			echo "【done】Nginx container has existed"
			if [[ "${RUNNING}" = "${WEB_RUNNING}" ]]
			then
				echo "【done】Nginx is running"
				# Setup base on the current OS
				if [[ "$OSTYPE" = "linux-gnu" ]]
				then
			        # Linux
			        chmod +x ./docker/startup/env_setup.sh
			        ./startup/env_setup.sh
				elif [[ "$OSTYPE" = "darwin"* ]]
				then
			        # Mac OSX
			        chmod +x ./docker/startup/env_setup.sh
			        bash ./docker/startup/env_setup.sh
			    else
			    	echo "【error】This bash does not support running in this current OS"
				fi        
			else
				echo "【error】Nginx is not running"
			fi

		fi
	else
		echo "【error】The application is not running"
	fi
fi

echo "-----------------------------------------------------------------------------------------------"
docker ps
echo "-----------------------------------------------------------------------------------------------"
