#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# Import the database
# It's better that this directory contains only 1 SQL file
MYSQL_HOST=localhost
MYSQL_DATABASE=sample_mysql
MYSQL_USER=sample_user
MYSQL_PASSWORD=sample_pass

for f in $(find /dump -type f| sort); do
	echo "*********"
    echo "$f"
    mysql -h "${MYSQL_HOST}" -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -f "${MYSQL_DATABASE}" --default-character-set=utf8 < "$f"
done

echo "Imported the database !"
