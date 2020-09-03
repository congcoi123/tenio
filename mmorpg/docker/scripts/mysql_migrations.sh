#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# Retrieve the database container
CONTAINER_ID="$(docker ps | awk '$2 ~ /^mysql:5.5.62$/ { print $1 }')"

echo "【＊】Start migrating for MySQL"
echo "MySQL5.5.62 Container ID：${CONTAINER_ID}"

# Check whether the database container has existed or not?
if [[ -z "${CONTAINER_ID}" ]]
then
	# Not existed
	echo "【error】MySQL migration was failed ！"
	echo "・Maybe the bash [docker_setup.sh] is not completed (confirm it  with the bash [env_status.sh])"
	echo "・Maybe the bash [docker_setup.sh] (Docker setup) was failed (check the log for furthermore details)"
else
	# Existed
	docker exec ${CONTAINER_ID} /bin/bash /docker/2_migrations.sh

	echo "【done】MySQL migration is completed !"
fi
