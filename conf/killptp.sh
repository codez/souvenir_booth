#!/bin/bash
# kills the ptp process on mac os x
# use after gphoto2 camera was connected to the computer and before starting SouvenirBooth

kill `ps -ax  | grep PTP | grep -v grep | awk '{print $1}'`

