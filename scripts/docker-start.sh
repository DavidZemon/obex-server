#!/bin/bash

if [ -d "/tmp/obex" ] ; then
  set -e
  git -C /tmp/obex fetch
  git -C /tmp/obex reset --hard origin/master
else
  set -e
  git clone https://github.com/parallaxinc/propeller.git /tmp/obex
fi

java -jar /opt/app.jar
