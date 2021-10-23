FROM amazonlinux:2

MAINTAINER kong <congcoi123@gmail.com>

#################### System ####################
# System update
RUN yum update -y

# Configure the Japanese for this environment
RUN yum install -y glibc-langpack-ja && \
    unlink /etc/localtime && \
    ln -s /usr/share/zoneinfo/Japan /etc/localtime && \
    echo "LANG=ja_JP.UTF-8" | tee /etc/sysconfig/i18n

# Configure the Japanese time zone for this environment
RUN ln -sf /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    echo -e 'ZONE="Asia/Tokyo"\nUTC=true' | tee /etc/sysconfig/clock

# Install the necessary packages for this system
RUN yum install -y zip unzip php-devel php-pear php-mbstring php-xml glibc-langpack-ja npm sudo ccze gcc vim procps libpng-devel git cronie wget bzip2 tar fontconfig freetype freetype-devel fontconfig-devel libstdc++ gcc-c++ cmake wget tar gzip at make -y

# Install EPEL
RUN yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
RUN yum install -y epel-release

# Create a folder for the application
RUN mkdir -p /var/www/sample-app

#################### Add Users ####################
# Add the desired user and its corresponding group
RUN useradd -g users kong

#################### Development Environment ####################
# The latest version of PHP 7.2 (The current one is 7.2.24)
RUN amazon-linux-extras install php7.2

# 【PHP7.2 Supporter】Debugger tool
RUN pecl install xdebug

# 【PHP7.2 Supporter】Install the Composer tool
RUN cd /opt && \
	php -r "copy('https://getcomposer.org/installer', 'composer-setup.php');" && \
    php composer-setup.php && \
    mv composer.phar /usr/local/bin/composer && \
    rm -f composer-setup.php

# 【PHP7.2 Supporter】Install the xhprof tool (@link: https://www.php.net/manual/en/book.xhprof.php)
RUN cd /opt && \
	git clone "https://github.com/yaoguais/phpng-xhprof.git" xhprof && \
	cd xhprof && \
	phpize && \
	./configure && \
	make && \
	make install && \
	mkdir -p /var/log/xhprof && \
	chmod 777 /var/log/xhprof

#【PHP7.2 Supporter】The GD Image extension
RUN yum install -y php-gd

# Install Phantomjs (The current version: 2.1.1) -> {@link: https://phantomjs.org/}
RUN cd /opt && \
	wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
	mkdir -p /opt/phantomjs && \
	bzip2 -d phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
	tar -xvf phantomjs-2.1.1-linux-x86_64.tar --directory /opt/phantomjs/ --strip-components 1 && \
	ln -s /opt/phantomjs/bin/phantomjs /usr/bin/phantomjs && \
	rm -f phantomjs-2.1.1-linux-x86_64.tar

# Install Nodejs (The current version: v8.12.0) -> {@link: https://nodejs.org/en/}
# This OS maybe has an old version of Nodejs (version 6x), so it's better that we remove that first
RUN yum remove -y node && \
	yum remove -y nodejs && \
	rm -rf /var/cache/yum && \
	yum clean all && \
	curl --silent --location https://rpm.nodesource.com/setup_8.x | sudo bash - && \
	yum -y install nodejs

# All the necessary packages of Nodejs should be installed in here
RUN npm install date-utils

# Install Puppeteer -> {@link: https://pptr.dev/}
RUN cd /opt && \
	mkdir puppeteer && \
	cd puppeteer && \
	npm install puppeteer

# 【Puppeteer Supporter】All the necessary libraries of Puppeteer (include the Chromium)
RUN yum -y install libX11 libXcomposite libXcursor libXdamage libXext libXi libXtst cups-libs libXScrnSaver libXrandr alsa-lib pango atk at-spi2-atk gtk3

# 【Puppeteer Supporter】Support the Japanese font
RUN yum -y install ipa-gothic-fonts xorg-x11-fonts-100dpi xorg-x11-fonts-75dpi xorg-x11-utils xorg-x11-fonts-cyrillic xorg-x11-fonts-Type1 xorg-x11-fonts-misc

# Expose one desired port for PHP-FPM service
EXPOSE 9000

# Run the PHP-FPM service in background
ENTRYPOINT /usr/sbin/php-fpm -F

#################### Development Environment Configuration ####################
# Create a new folder for the configurations
RUN mkdir -p /docker/composer/
COPY /docker/app/in-script/setup_env.sh /docker/composer/setup_env.sh
