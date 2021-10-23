#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# This bash will access the application (the bash “docker_setup.sh” must be run before)
# Retrieve the container ID while Docker is running
CONTAINER_ID="$(docker ps | awk '$2 ~ /^docker_php_nginx_sample_app$/ { print $1 }')"

echo "Application Container ID：${CONTAINER_ID}"

# Check if the container has existed or not?
if [[ -z "${CONTAINER_ID}" ]]
then
	# Not existed
	echo "【error】 Application access failed !"
	echo "・Maybe the bash [docker_setup.sh] is not completed (confirm it  with the bash [env_status.sh])"
	echo "・Maybe the bash [docker_setup.sh] (Docker setup) was failed (check the log for furthermore details)"
else
	# Existed
	docker exec -it ${CONTAINER_ID} /bin/bash

	echo "【done】Application accessed successful !"
fi
