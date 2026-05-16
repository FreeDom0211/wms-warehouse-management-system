@echo off
set "PASS=$2a$10$5ZfgbeZ70SivfVb0gIq8kOVTZnBKREnlxmPLZjQhrOkGPuzPUY/PS"
mysql -u root -p123456 jd_wms -e "UPDATE user SET password='%PASS%' WHERE work_no='admin001'"
mysql -u root -p123456 jd_wms -e "UPDATE user SET password='%PASS%' WHERE work_no='admin002'"
mysql -u root -p123456 jd_wms -e "UPDATE user SET password='%PASS%' WHERE work_no='op001'"
echo Passwords updated successfully
