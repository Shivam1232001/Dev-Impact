#!/bin/bash
##############functions ##################

LOCAL_FILE="./localDevDockerSetup.yml"
MAIN_FILE="./docker-compose.yml"

if [ "$1" = "start" ]; then
  echo "Starting ALL docker containers (localDevDockerSetup + docker-compose)..."

  docker compose -f $LOCAL_FILE up -d
  docker compose -f $MAIN_FILE up -d

elif [ "$1" = "stop" ]; then
  echo "Stopping ALL docker containers..."

  docker compose -f $LOCAL_FILE stop
  docker compose -f $MAIN_FILE stop

elif [ "$1" = "remove" ]; then
  echo "Stopping and removing ALL containers + volumes..."

  docker compose -f $LOCAL_FILE down -v
  docker compose -f $MAIN_FILE down -v

else
  echo "Valid args -> { start, stop, remove }"
  exit
fi
