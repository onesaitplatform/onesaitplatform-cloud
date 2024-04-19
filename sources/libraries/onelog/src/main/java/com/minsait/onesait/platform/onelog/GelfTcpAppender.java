/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.onelog;

import java.io.IOException;
import java.util.Arrays;

import javax.net.SocketFactory;

import com.minsait.onesait.platform.onelog.pool.PooledObjectConsumer;
import com.minsait.onesait.platform.onelog.pool.PooledObjectFactory;
import com.minsait.onesait.platform.onelog.pool.SimpleObjectPool;

import lombok.Getter;
import lombok.Setter;

public class GelfTcpAppender extends GelfAppender {

	 private static final int DEFAULT_CONNECT_TIMEOUT = 15000;
	 private static final int DEFAULT_RECONNECT_INTERVAL = 60;
	 private static final int DEFAULT_MAX_RETRIES = 2;
	 private static final int DEFAULT_RETRY_DELAY = 3000;
	 private static final int DEFAULT_POOL_SIZE = 2;
	 private static final int DEFAULT_POOL_MAX_WAIT_TIME = 5000;
	 
    /**
     * Maximum time (in milliseconds) to wait for establishing a connection. A value of 0 disables
     * the connect timeout. Default: 15,000 milliseconds.
     */
    @Getter
    @Setter
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    /**
     * Time interval (in seconds) after an existing connection is closed and re-opened.
     * A value of -1 disables automatic reconnects. Default: 60 seconds.
     */
    @Getter
    @Setter
    private int reconnectInterval = DEFAULT_RECONNECT_INTERVAL;

    /**
     * Number of retries. A value of 0 disables retry attempts. Default: 2.
     */
    @Getter
    @Setter
    private int maxRetries = DEFAULT_MAX_RETRIES;

    /**
     * Time (in milliseconds) between retry attempts. Ignored if maxRetries is 0.
     * Default: 3,000 milliseconds.
     */
    @Getter
    @Setter
    private int retryDelay = DEFAULT_RETRY_DELAY;

    /**
     * Number of concurrent tcp connections (minimum 1). Default: 2.
     */
    @Getter
    @Setter
    private int poolSize = DEFAULT_POOL_SIZE;

    /**
     * Maximum amount of time (in milliseconds) to wait for a connection to become
     * available from the pool. A value of -1 disables the timeout. Default: 5,000 milliseconds.
     */
    @Getter
    @Setter
    private int poolMaxWaitTime = DEFAULT_POOL_MAX_WAIT_TIME;

    private SimpleObjectPool<TcpConnection> connectionPool;

    protected void startAppender() {
        final AddressResolver addressResolver = new AddressResolver(getGraylogHost());

        connectionPool = new SimpleObjectPool<>(new PooledObjectFactory<TcpConnection>() {
            @Override
            public TcpConnection newInstance() {
                return new TcpConnection(initSocketFactory(),
                    addressResolver, getGraylogPort(), connectTimeout);
            }
        }, poolSize, poolMaxWaitTime, reconnectInterval);
    }

    protected SocketFactory initSocketFactory() {
        return SocketFactory.getDefault();
    }

    @Override
    protected void appendMessage(final byte[] messageToSend) {
        // GELF via TCP requires 0 termination
        final byte[] tcpMessage = Arrays.copyOf(messageToSend, messageToSend.length + 1);

        int openRetries = maxRetries;
        do {
            if (sendMessage(tcpMessage)) {
                // Message was sent successfully - we're done with it
                break;
            }

            if (retryDelay > 0 && openRetries > 0) {
                try {
                    Thread.sleep(retryDelay);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } while (openRetries-- > 0 && isStarted());
    }

    /**
     * Send message to socket's output stream.
     *
     * @param messageToSend message to send.
     *
     * @return {@code true} if message was sent successfully, {@code false} otherwise.
     */
    private boolean sendMessage(final byte[] messageToSend) {
        try {
            connectionPool.execute(new PooledObjectConsumer<TcpConnection>() {
                @Override
                public void accept(final TcpConnection tcpConnection) throws IOException {
                    tcpConnection.write(messageToSend);
                }
            });
        } catch (final Exception e) {
            addError(String.format("Error sending message via tcp://%s:%s",
                getGraylogHost(), getGraylogPort()), e);
            return false;
        }

        return true;
    }

    @Override
    protected void close() {
        connectionPool.close();
    }

}
