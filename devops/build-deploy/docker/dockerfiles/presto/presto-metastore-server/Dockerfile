FROM prestodb/hive3.1-hive:10

ENV HIVE_METASTORE_DB_URL=jdbc:mysql://localhost:3306/metastore?useSSL=false \ 
    HIVE_METASTORE_DB_DRIVER=com.mysql.cj.jdbc.Driver \ 
    HIVE_METASTORE_DB_USERNAME=root \ 
    HIVE_METASTORE_DB_PASSWORD=root \ 
	MINIO_SERVER_ENDPOINT=http://minio:9000 \
	MINIO_ROOT_USER=minio \
	MINIO_ROOT_PASSWORD=minio123

#Modifica el original para crear la BD del metastore en caso de que esté vacia (Se ha externalizado a un volumen para persistencia
COPY ./entrypoint.sh /root/entrypoint.sh

COPY ./files/core-site.xml /opt/hadoop/etc/hadoop/core-site.xml
COPY ./files/hdfs-site.xml /opt/hadoop/etc/hadoop/hdfs-site.xml
COPY ./files/hive-site.xml /opt/hive/conf/hive-site.xml

RUN chmod 755 /root/entrypoint.sh
