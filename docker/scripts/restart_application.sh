#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# This bash is for restarting the application container (The bash [docker_setup.sh] must be run before)
# Retrieve the application container ID
CONTAINER_ID="$(docker ps -a | awk '$2 ~ /^docker_php_nginx_sample_app$/ { print $1 }')"

echo "Application Container ID：${CONTAINER_ID}"

# Check whether the application container has existed or not?
if [[ -z "${CONTAINER_ID}" ]]
then
	# Not existed
	echo "【error】The application container is failed to restart ！"
	echo "・Maybe the bash [docker_setup.sh] is not completed (confirm it  with the bash [env_status.sh])"
	echo "・Maybe the bash [docker_setup.sh] (Docker setup) was failed (check the log for furthermore details)"
else
	# Existed
	docker restart ${CONTAINER_ID}

	echo "【done】The application container was restarted !"
fi
