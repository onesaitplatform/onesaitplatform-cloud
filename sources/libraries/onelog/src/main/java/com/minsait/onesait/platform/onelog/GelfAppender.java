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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import lombok.Getter;
import lombok.Setter;


public abstract class GelfAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    /**
     * IP or hostname of graylog server.
     */
    @Getter
    @Setter
    private String graylogHost;

    /**
     * Port of graylog server. Default: 12201.
     */
    @Getter
    @Setter
    private int graylogPort = 12201;

    @Getter
    @Setter
    private GelfEncoder encoder;

    @Override
    public final void start() {

        if (graylogHost == null) {
            addError("No graylogHost configured");
            return;
        }

        if (encoder == null) {
            encoder = new GelfEncoder();
            encoder.setContext(getContext());
            encoder.start();
        }

        try {
            startAppender();

            super.start();
        } catch (final Exception e) {
            addError("Couldn't start appender", e);
        }
    }

    protected void startAppender() throws IOException {
    }

    @Override
    protected void append(final ILoggingEvent event) {
        final byte[] binMessage = encoder.encode(event);

        try {
            appendMessage(binMessage);
        } catch (final Exception e) {
            // Could be IOException or some kind of RuntimeException
            addError("Error sending GELF message", e);
        }
    }

    protected abstract void appendMessage(byte[] messageToSend) throws IOException;

    @Override
    public void stop() {
        super.stop();
        try {
            close();
        } catch (final IOException e) {
            addError("Couldn't close appender", e);
        }
    }

    protected abstract void close() throws IOException;

}
