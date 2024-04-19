FROM atmoz/sftp:latest
COPY ssh_host_ed25519_key /etc/ssh/ssh_host_ed25519_key
COPY ssh_host_rsa_key /etc/ssh/ssh_host_rsa_key
ENV SFTP_USERS=sftp-user:changeit:::upload
