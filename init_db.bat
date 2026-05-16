@echo off
cd /d d:\毕设\demo
mysql -u root -p123456 < sql\init_simple.sql
echo Database initialized successfully
pause