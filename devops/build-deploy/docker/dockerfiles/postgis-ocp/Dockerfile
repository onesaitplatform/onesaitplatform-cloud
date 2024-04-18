FROM kartoza/postgis:13-3.1

# Create directories
RUN mkdir -p /settings && \
    mkdir -p /ssl_certificates && \
    mkdir -p /var/run/postgresql && \
    mkdir -p /var/lib/postgresql/13 && \
    mkdir -p /opt/archivedir && \
    mkdir /postgis

USER root

# Move the scripts and config templates
ADD setup-ssl.sh /scripts/setup-ssl.sh
ADD setup-database.sh /scripts/setup-database.sh
ADD docker-entrypoint.sh /scripts/docker-entrypoint.sh
ADD pwfile /scripts/pwfile
ADD pg_hba.conf.template /etc/postgresql/13/main/pg_hba.conf.template
ADD postgresql.conf /var/lib/postgresql/13/main/postgresql.conf
ADD env-data.sh /scripts/env-data.sh
ADD setup-user.sh /scripts/setup-user.sh

# Give permissions to scripts and templates
RUN chmod +x /scripts/setup-ssl.sh && \
    chmod +x /scripts/setup-database.sh && \
    chmod +x /scripts/docker-entrypoint.sh && \
    chmod +x /scripts/env-data.sh && \
    chmod +x /scripts/setup-user.sh && \
    chmod 777 /var/lib/postgresql/13/main/postgresql.conf && \
    chmod 777 /etc/postgresql/13/main/pg_hba.conf.template

# Change de owner and group
RUN chown -R postgres:postgres /settings && \
    chown -R postgres:postgres /etc/postgresql/13/main && \
    chown -R postgres:root /etc/ssl && \
    chown -R postgres:postgres /ssl_certificates && \
    chown -R postgres:postgres /usr/lib/postgresql/13/bin && \
    chown -R postgres:postgres /opt/archivedir

# Create a sysctl.conf
RUN touch /etc/sysctl.conf

# Give permissions
RUN chmod -R 777 /settings && \
    chmod -R 777 /etc/postgresql/13/main && \
    chmod -R 700 /ssl_certificates && \
    chmod -R 777 /etc/ssl && \
    chmod 777 /etc/sysctl.conf && \
    chmod -R 777 /var/run/postgresql && \
    chmod -R 777 /var/lib/postgresql && \
    chmod 777 /scripts/pwfile && \
    chmod -R 777 /usr/lib/postgresql/13/bin && \
    chmod -R 777 /opt/archivedir && \
    chmod -R 777 /postgis

# Part from script
RUN cp -r /etc/ssl /tmp/ssl-copy/ && \
    chmod -R 0700 /etc/ssl && \
    chown -R postgres /tmp/ssl-copy && \
    rm -r /etc/ssl && \
    mv /tmp/ssl-copy /etc/ssl && \
    chmod 600 /etc/ssl/private/ssl-cert-snakeoil.key

USER postgres

EXPOSE 5432
