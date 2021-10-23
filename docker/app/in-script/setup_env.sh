#!/bin/bash
# @author: kong<congcoi123@gmail.com>

# Move to the target folder
cd /var/www

# Create a symlink for the PHP object
ln -s /usr/bin/php /usr/local/bin/php

# Update the composer itself
/usr/local/bin/composer self-update

# Composer Clock up
/usr/local/bin/composer global require "hirak/prestissimo"

# Install all libraries that are defined in the composer.json file
/usr/local/bin/composer update

# Define the root of Nodejs
export NODE_PATH=`npm root -g`;

echo "The development environment is configured successfully !"
