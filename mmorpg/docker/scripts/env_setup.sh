#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# Retrieve the current running container of application
CONTAINER_ID="$(docker ps | awk '$2 ~ /^docker_php_nginx_sample_app$/ { print $1 }')"

echo "【＊】Start setup the development environment"
echo "Application Container ID：${CONTAINER_ID}"

# Check if the container id has existed or not?
if [[ -z "${CONTAINER_ID}" ]]
then
	# Not existed
	echo "【error】Setting up the development environment has failed ！"
	echo "・Maybe the bash [docker_setup.sh] is not completed (confirm it  with the bash [env_status.sh])"
	echo "・Maybe the bash [docker_setup.sh] (Docker setup) was failed (check the log for furthermore details)"
else
	# Existed
	# composer setting up
	docker exec ${CONTAINER_ID} /bin/bash /docker/composer/setup_env.sh

	echo "【done】The development environment setup is completed !"
fi
