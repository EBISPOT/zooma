#!/bin/sh

docker volume ls | tail -n+2 | awk '{print $2}' | xargs docker volume rm
