FROM timescale/timescaledb-ha:pg14-latest

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="timescaledb"

ENV POSTGRES_USER=postgres \
	  POSTGRES_PASSWORD=password \
    PGDATA=/var/lib/postgresql/data/pgdata
    
USER root

# Create folders
RUN mkdir -p /var/lib/postgresql/data/pgdata && \
    mkdir -p /var/run/postgresql

# Give permissions
RUN chown -R postgres:postgres /var/lib/postgresql && \
    chown -R postgres:postgres /var/run/postgresql && \
    chmod -R 777 /var/lib/postgresql && \
    chmod -R 777 /var/run/postgresql

COPY postgisInstaller.sql /docker-entrypoint-initdb.d/

EXPOSE 5432

# TimescaleDB data directory
VOLUME /var/lib/postgresql/data

USER postgres
