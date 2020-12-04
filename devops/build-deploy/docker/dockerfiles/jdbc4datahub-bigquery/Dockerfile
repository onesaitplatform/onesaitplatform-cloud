FROM onesaitplatform/jdbc4datahub-baseimage:latest

RUN echo "Adding Big Query Drivers 4.2" && \
	wget -O /my-database-jars/SimbaJDBCDriverforGoogleBigQuery42_1.2.2.1004.zip https://storage.googleapis.com/simba-bq-release/jdbc/SimbaJDBCDriverforGoogleBigQuery42_1.2.2.1004.zip && \
	unzip /my-database-jars/SimbaJDBCDriverforGoogleBigQuery42_1.2.2.1004.zip -d /my-database-jars && \
	rm /my-database-jars/SimbaJDBCDriverforGoogleBigQuery42_1.2.2.1004.zip && \
	rm /my-database-jars/*.txt && \
	rm /my-database-jars/*.pdf