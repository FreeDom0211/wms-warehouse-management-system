@echo off
mysql -u root -p123456 --default-character-set=utf8mb4 < "d:\毕设\demo\sql\init.sql"
echo Database initialized successfully
