/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import com.minsait.onesait.platform.onelog.pool.AbstractPooledObject;

public class TcpConnection extends AbstractPooledObject {

    private final AddressResolver addressResolver;
    private final SocketFactory socketFactory;
    private final int port;
    private final int connectTimeout;

    private volatile OutputStream outputStream;

    TcpConnection(final SocketFactory socketFactory,
                  final AddressResolver addressResolver, final int port, final int connectTimeout) {

        this.addressResolver = addressResolver;
        this.socketFactory = socketFactory;
        this.port = port;
        this.connectTimeout = connectTimeout;
    }

    public void write(final byte[] messageToSend) throws IOException {
        if (outputStream == null) {
            connect();
        }

        outputStream.write(messageToSend);
        outputStream.flush();
    }

    private void connect() throws IOException {
        final Socket socket = socketFactory.createSocket();
        final InetAddress ip = addressResolver.resolve();
        socket.connect(new InetSocketAddress(ip, port), connectTimeout);
        outputStream = socket.getOutputStream();
    }

    @Override
    protected void close() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
