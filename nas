NAS Migration from IV1 to IV2

Overview

This document describes the procedure followed to migrate Beagle data from the legacy NAS storage (IV1) to the new NAS storage (IV2) while maintaining application compatibility through a symbolic link.

Objective

Migrate Beagle data from the old NAS mount to the new IV2 storage location without requiring any application configuration changes.

Existing Location (IV1)

/data/beagle/beagle_data

New Location (IV2)

/mnt/beagle_share/beagle_data

---

Prerequisites

1. Ensure the new IV2 NAS mount is available and accessible.
2. Verify sufficient storage space on IV2.
3. Schedule a maintenance window.
4. Stop all applications/services accessing the storage location.

---

Migration Steps

1. Verify Existing Data

Check the current contents and size of the IV1 location.

cd /data/beagle/beagle_data
ls -ltr

du -sh /data/beagle/beagle_data

---

2. Copy Data to IV2

Copy all data from IV1 to IV2.

cp -rp /data/beagle/beagle_data/* /mnt/beagle_share/beagle_data/

Verify the copied data.

ls -ltr /mnt/beagle_share/beagle_data

du -sh /mnt/beagle_share/beagle_data

---

3. Stop Beagle Services

Stop all Tomcat instances using the storage.

cd /data/tomcat_Beagle/scripts
./tomcat.sh stop

cd /data/tomcat_Beagle_Support/scripts
./tomcat.sh stop

Verify that no Java processes are accessing the mount.

sudo fuser -vm /data/beagle/beagle_data

---

4. Remove Old Mount Configuration

Open the fstab file.

sudo vi /etc/fstab

Remove or comment out the old NAS mount entry corresponding to:

/data/beagle/beagle_data

Save the file.

Validate the configuration:

sudo mount -a

No errors should be returned.

---

5. Unmount IV1 NAS

Unmount the old NAS.

sudo umount /data/beagle/beagle_data

Verify that the mount is removed.

mount | grep beagle_data

df -h | grep beagle_data

No active mount should be displayed.

---

6. Backup Existing Directory

Rename the existing directory for rollback purposes.

mv /data/beagle/beagle_data /data/beagle/beagle_data_bkp

Verify:

ls -ltr /data/beagle

---

7. Create Symbolic Link

Navigate to the parent directory.

cd /data/beagle

Create the symbolic link.

ln -s /mnt/beagle_share/beagle_data beagle_data

Verify the symbolic link.

ls -l /data/beagle

readlink -f /data/beagle/beagle_data

Expected output:

/mnt/beagle_share/beagle_data

---

8. Restart Services

Restart Beagle services.

cd /data/tomcat_Beagle/scripts
./tomcat.sh start

cd /data/tomcat_Beagle_Support/scripts
./tomcat.sh start

---

9. Validation

Verify:

1. Application starts successfully.
2. Documents are accessible.
3. New files are written successfully.
4. The symbolic link resolves correctly.

Commands:

readlink -f /data/beagle/beagle_data

ls -ltr /data/beagle/beagle_data

---

Rollback Procedure

If rollback is required:

1. Stop Tomcat services.
2. Remove symbolic link.

rm /data/beagle/beagle_data

3. Restore original directory.

mv /data/beagle/beagle_data_bkp /data/beagle/beagle_data

4. Restore the old NAS mount entry in "/etc/fstab".
5. Mount the filesystem.

mount -a

6. Start Tomcat services.

---

Final Architecture

Before Migration

Beagle
   ↓
/data/beagle/beagle_data
   ↓
IV1 NAS

After Migration

Beagle
   ↓
/data/beagle/beagle_data (symlink)
   ↓
/mnt/beagle_share/beagle_data
   ↓
IV2 NAS