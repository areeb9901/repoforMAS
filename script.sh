#!/bin/ksh

echo "====================================================="
echo "Beagle Tomcat RESTART Cron Job Started at $(date)"
echo "====================================================="

# Stop Tomcat
echo "Stopping Tomcat..."
sudo su - svc2auxbeagled -c "cd /data/beagle/scripts && ./tomcat.sh stop"
STOP_STATUS=$?

if [ $STOP_STATUS -ne 0 ]; then
    echo "Tomcat STOP command FAILED with status $STOP_STATUS at $(date)"
else
    echo "Tomcat STOP command executed successfully at $(date)"
fi

# Wait for Tomcat to fully stop
echo "Waiting 60 seconds before starting Tomcat..."
sleep 60

# Start Tomcat
echo "Starting Tomcat..."
sudo su - svc2auxbeagled -c "cd /data/beagle/scripts && ./tomcat.sh start"
START_STATUS=$?

if [ $START_STATUS -eq 0 ]; then
    echo "Tomcat START command executed successfully at $(date)"
else
    echo "Tomcat START command FAILED with status $START_STATUS at $(date)"
fi

echo "====================================================="
echo "Beagle Tomcat RESTART Cron Job Ended at $(date)"
echo "====================================================="