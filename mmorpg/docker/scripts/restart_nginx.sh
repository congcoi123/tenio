#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# This bash is for restarting the nginx container (The bash [docker_setup.sh] must be run before)
# Retrieve the nginx container ID
CONTAINER_ID="$(docker ps -a | awk '$2 ~ /^docker_php_nginx_sample_web$/ { print $1 }')"

echo "Nginx container ID ：${CONTAINER_ID}"

# Check whether the Nginx container has existed or not?
if [[ -z "${CONTAINER_ID}" ]]
then
	# Not existed
	echo "【error】The Nginx container is failed to restart ！"
	echo "・Maybe the bash [docker_setup.sh] is not completed (confirm it  with the bash [env_status.sh])"
	echo "・Maybe the bash [docker_setup.sh] (Docker setup) was failed (check the log for furthermore details)"
else
	# Existed
	docker restart ${CONTAINER_ID}

	echo "【done】The Nginx container was restarted !"
fi
