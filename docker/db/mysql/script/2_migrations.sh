#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# Migrate the necessary SQL files
MYSQL_HOST=localhost
MYSQL_DATABASE=sample_mysql
MYSQL_USER=sample_user
MYSQL_PASSWORD=sample_pass

for f in $(find /migrations -type f| sort); do
    echo "*********"
    echo "$f"
    mysql -h "${MYSQL_HOST}" -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -f "${MYSQL_DATABASE}" --default-character-set=utf8 < "$f"
done

echo "SQL files are migrated !"
