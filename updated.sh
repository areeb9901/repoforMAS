#!/bin/ksh

LOG_TS="$(date '+%Y-%m-%d %H:%M:%S %Z')"
echo "====================================================="
echo "Beagle Tomcat RESTART Cron Job Started at ${LOG_TS}"
echo "====================================================="

# Always run from scripts dir
cd /data/beagle/scripts || { echo "[ERROR] Cannot cd to /data/beagle/scripts"; exit 1; }

# (Optional but recommended) load env if your manual shell sets something needed
# Uncomment ONE of these if applicable in your setup:
# . /home/svc2auxbeagled/.profile
# . /home/svc2auxbeagled/.kshrc

echo "Stopping Tomcat..."
./tomcat.sh stop
STOP_STATUS=$?
echo "Stop exit code: ${STOP_STATUS}"

# Wait until Tomcat really stops (don’t rely only on sleep)
# If your tomcat.sh writes PID somewhere, use that. Otherwise we do a best-effort check.
echo "Waiting for Tomcat process to fully stop..."
i=0
while [ $i -lt 30 ]; do
  # try to find a tomcat/java process for this CATALINA_BASE
  ps -ef | grep -v grep | grep "/data/beagle/tomcat" >/dev/null 2>&1
  if [ $? -ne 0 ]; then
    echo "[INFO] Tomcat process appears stopped."
    break
  fi
  echo "[INFO] still stopping... waiting 5s"
  sleep 5
  i=$((i+1))
done

# --- CLEANUP STEP (this is what you are doing manually) ---
WEBAPPS_DIR="/data/beagle/tomcat/webapps"
EXPLODED_APP="${WEBAPPS_DIR}/beagle"
WORK_DIR="/data/beagle/tomcat/work/Catalina/localhost/beagle"

echo "Cleaning exploded app (if exists): ${EXPLODED_APP}"
if [ -d "${EXPLODED_APP}" ]; then
  rm -rf "${EXPLODED_APP}"
  echo "[INFO] Removed exploded folder: ${EXPLODED_APP}"
else
  echo "[INFO] Exploded folder not present, skipping."
fi

echo "Cleaning Tomcat work cache (optional): ${WORK_DIR}"
if [ -d "${WORK_DIR}" ]; then
  rm -rf "${WORK_DIR}"
  echo "[INFO] Removed work cache: ${WORK_DIR}"
else
  echo "[INFO] Work cache not present, skipping."
fi

# Start Tomcat
echo "Starting Tomcat..."
./tomcat.sh start
START_STATUS=$?
echo "Start exit code: ${START_STATUS}"

# Optional: quick health-check (adjust URL/port if needed)
# This helps confirm "process started" vs "app deployed"
sleep 20
# Try localhost first; if curl not installed, skip
if command -v curl >/dev/null 2>&1; then
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:8080/beagle/" 2>/dev/null)
  echo "[INFO] Healthcheck HTTP code for /beagle/ : ${HTTP_CODE}"
else
  echo "[INFO] curl not available, skipping healthcheck."
fi

LOG_TS_END="$(date '+%Y-%m-%d %H:%M:%S %Z')"
echo "====================================================="
echo "Beagle Tomcat RESTART Cron Job Ended at ${LOG_TS_END}"
echo "====================================================="