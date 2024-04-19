#!/bin/bash -x

echo "Substituting environment variables in Hive properties"	

grep -rl '${HIVE_METASTORE_DB_URL}' /opt/hive/conf/hive-site.xml | xargs sed -i 's!${HIVE_METASTORE_DB_URL}!'"$HIVE_METASTORE_DB_URL"'!g'
grep -rl '${HIVE_METASTORE_DB_DRIVER}' /opt/hive/conf/hive-site.xml | xargs sed -i 's/${HIVE_METASTORE_DB_DRIVER}/'"$HIVE_METASTORE_DB_DRIVER"'/g'
grep -rl '${HIVE_METASTORE_DB_USERNAME}' /opt/hive/conf/hive-site.xml | xargs sed -i 's/${HIVE_METASTORE_DB_USERNAME}/'"$HIVE_METASTORE_DB_USERNAME"'/g'
grep -rl '${HIVE_METASTORE_DB_PASSWORD}' /opt/hive/conf/hive-site.xml | xargs sed -i 's/${HIVE_METASTORE_DB_PASSWORD}/'"$HIVE_METASTORE_DB_PASSWORD"'/g'
grep -rl '${MINIO_SERVER_ENDPOINT}' /opt/hive/conf/hive-site.xml | xargs sed -i 's!${MINIO_SERVER_ENDPOINT}!'"$MINIO_SERVER_ENDPOINT"'!g'
grep -rl '${MINIO_ROOT_USER}' /opt/hive/conf/hive-site.xml | xargs sed -i 's/${MINIO_ROOT_USER}/'"$MINIO_ROOT_USER"'/g'
grep -rl '${MINIO_ROOT_PASSWORD}' /opt/hive/conf/hive-site.xml | xargs sed -i 's/${MINIO_ROOT_PASSWORD}/'"$MINIO_ROOT_PASSWORD"'/g'

grep -rl '${MINIO_SERVER_ENDPOINT}' /opt/hadoop/etc/hadoop/core-site.xml | xargs sed -i 's!${MINIO_SERVER_ENDPOINT}!'"$MINIO_SERVER_ENDPOINT"'!g'
grep -rl '${MINIO_ROOT_USER}' /opt/hadoop/etc/hadoop/core-site.xml | xargs sed -i 's/${MINIO_ROOT_USER}/'"$MINIO_ROOT_USER"'/g'
grep -rl '${MINIO_ROOT_PASSWORD}' /opt/hadoop/etc/hadoop/core-site.xml | xargs sed -i 's/${MINIO_ROOT_PASSWORD}/'"$MINIO_ROOT_PASSWORD"'/g'

grep -rl '${MINIO_SERVER_ENDPOINT}' /opt/hadoop/etc/hadoop/hdfs-site.xml | xargs sed -i 's!${MINIO_SERVER_ENDPOINT}!'"$MINIO_SERVER_ENDPOINT"'!g'
grep -rl '${MINIO_ROOT_USER}' /opt/hadoop/etc/hadoop/hdfs-site.xml | xargs sed -i 's/${MINIO_ROOT_USER}/'"$MINIO_ROOT_USER"'/g'
grep -rl '${MINIO_ROOT_PASSWORD}' /opt/hadoop/etc/hadoop/hdfs-site.xml | xargs sed -i 's/${MINIO_ROOT_PASSWORD}/'"$MINIO_ROOT_PASSWORD"'/g'


#In case /var/lib/mysql directory is mapped from external volume
if [[ ! -e /var/lib/mysql/metastore ]]; then
   mysql_install_db

   chown -R mysql:mysql /var/lib/mysql

   /usr/bin/mysqld_safe &
   sleep 10s

   echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION; FLUSH PRIVILEGES;" | mysql
   echo "CREATE DATABASE metastore;" | mysql
   /usr/bin/mysqladmin -u root password 'root'
   /opt/hive/bin/schematool -dbType mysql -initSchema

   killall mysqld
   sleep 10s
   chown -R mysql:mysql /var/log/mysql/
   rm -rf /tmp/* /var/tmp/*
fi

for init_script in /etc/hadoop-init.d/*; do
  "${init_script}"
done

supervisord -c /etc/supervisord.conf