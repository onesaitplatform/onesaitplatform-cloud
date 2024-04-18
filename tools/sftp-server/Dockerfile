FROM atmoz/sftp:latest
COPY ssh_host_ed25519_key /etc/ssh/ssh_host_ed25519_key
COPY ssh_host_rsa_key /etc/ssh/ssh_host_rsa_key
COPY cron.sh /tmp/cron.sh
COPY entrypoint.sh entrypointc.sh
COPY sshd_config /etc/ssh/sshd_config
COPY monitor_ssh_logs.sh monitor_ssh_logs.sh
RUN chmod +x /tmp/cron.sh
RUN chmod +x entrypointc.sh
RUN chmod +x monitor_ssh_logs.sh
ENV SFTP_USERS=sftp-user:changeit:::upload
ENTRYPOINT ["/entrypointc.sh"]
