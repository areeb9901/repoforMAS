I have this server with name eurvlii36858.xmp where I login using putty Now when I login with user id : lh59274
then I enter the password then I type the command to switch to user account 
sudo su - svc2auxbeagled
then I go to this path 
cd /data/beagle/scripts
then I write this command 
./tomcat.sh start

So now I have to write a shell script which does this ./tomcat.sh start command every monday 6:00 am 
we can execute this shell script on server using cron expression 

I will also share with you 
there is a shell script on this path 
cd /data/beagle/local_data/config/

log_deletion.sh 

and when I type command 
crontab -l

I see 
0 0 8 * * SUN sh /data/beagle/local_data/log_deletion.sh >>/data/beagle/local_data/config/log_deletion.log

Similarly I want this script for starting the server every monday at 5 am 

For your reference I will share with you log_deletion.sh file but beware that this is altogether a different functionality script I am sharing just for reference 

#!/bin/ksh

<<comment

Created By : Niranjan Salunke(E11041)

Cration Date : 1st Aug 2022

Purpose: To automate deletion of app logs and tomcat logs

comment

#below job is running using crontab -e on Node 2 for 10th and 25th of Every Month to delete older log files and folders

echo "************************************************************************"

echo "Archive log deletion Cronjob Started on $(date)"

echo "Check for archived logs at path /data/beagle/local_data/logs/"

#echo "Archived logs for current month is stored in folder named----- $(date +"%Y-%m")"

dt=$(echo "$(date --date="1 month ago" +%Y-%m)")

#echo "Name of backup log folder for previous month is-----$dt"

#echo "Name of backup log folder for current month is-----$(date +"%Y-%m")"

echo "-"

echo "-"

echo "ACTIVITY STARTED---Deleting backup folder for previous months."

#Below script will find archived log folde for previous month and delete it.


find /data/beagle/local_data/logs -maxdepth 1 -type d -mtime +30 -exec echo "Deleting back dated folder(s) {}" \; -exec /bin/rm -rf {} +

#find /data/beagle/local_data/testlogs -maxdepth 1 -type d -mtime +1 -name "$dt" -exec /bin/rm -rf {} +

echo "ACTIVITY COMPLETED---Deleting backup folder for previous months."

#below script will find all files and delete them which are present in /beagle_data/logs/ having '-' in name and not modified for 30 days

echo "-"

echo "-"

echo "ACTIVITY STARTED---Deleting backup log files older than 7 days."

find /data/beagle/local_data/logs/ -mtime +7 -type f ! -name "*.log" -exec echo "Deleting old log files {}" \; -exec /bin/rm {} +

##find /data/beagle/local_data/testlogs/ -mmin +7 -name "*.log.*" -type f -exec /bin/rm {} +

echo "ACTIVITY COMPLETED---Deleting backup log files older than 7 days. "

echo "-"

echo "-"

echo "Archive log deletion Cronjob Ended on $(date)"