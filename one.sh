#! /bin/sh
java -Xmx2048M -cp target:lib/ECLA.jar:lib/DTNConsoleConnection.jar core.DTNSim $*

# send a get request to the notification server
# curl -X GET https://api.day.app/i5xtQapPZVuE2gfQLvbZD4/completed
