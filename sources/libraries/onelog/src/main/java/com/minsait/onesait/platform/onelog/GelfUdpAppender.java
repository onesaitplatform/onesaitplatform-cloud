/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Supplier;
import java.util.zip.DeflaterOutputStream;

import lombok.Getter;
import lombok.Setter;

public class GelfUdpAppender extends GelfAppender {

	// Maximum size of GELF chunks in bytes. Default chunk size is 508
	@Getter
	@Setter
	private Integer maxChunkSize;

	// If true, compression of GELF messages is enabled. Default: true.
	@Getter
	@Setter
	private boolean useCompression = true;

	@Getter
	@Setter
	private Supplier<Long> updIdGenerator = new UdpMessageIdGenerator();

	private UDPChannel udpChannel;

	private GelfUdpChunker chunker;

	private AddressResolver addressResolver;

	@Override
    protected void startAppender() throws IOException {
		udpChannel = new UDPChannel();
        chunker = new GelfUdpChunker(updIdGenerator, maxChunkSize);
        addressResolver = new AddressResolver(getGraylogHost());
    }
	
	@Override
	protected void appendMessage(byte[] messageToSend) throws IOException {
		final byte[] udpMessage = useCompression ? compress(messageToSend) : messageToSend;
		
		final InetSocketAddress remote = new InetSocketAddress(addressResolver.resolve(), getGraylogPort());

		for (final ByteBuffer chunk : chunker.chunks(udpMessage)) {
			while (chunk.hasRemaining()) {
				udpChannel.send(chunk, remote);
			}
		}
	}

	private static byte[] compress(final byte[] binMessage) {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(binMessage.length);
		try (OutputStream deflaterOut = new DeflaterOutputStream(bos)) {
			deflaterOut.write(binMessage);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return bos.toByteArray();
	}

	@Override
	protected void close() throws IOException {
		udpChannel.close();
	}

	private static final class UDPChannel {

		private volatile DatagramChannel channel;
		private volatile boolean stopped;

		@SuppressWarnings("unused")
		UDPChannel() throws IOException {
			this.channel = DatagramChannel.open();
		}

		void send(final ByteBuffer src, final SocketAddress target) throws IOException {
			getChannel().send(src, target);
		}

		private DatagramChannel getChannel() throws IOException {
			DatagramChannel tmp = channel;
			if (!tmp.isOpen()) {
				synchronized (this) {
					tmp = channel;
					if (!tmp.isOpen() && !stopped) {
						tmp = DatagramChannel.open();
						channel = tmp;
					}
				}
			}
			return tmp;
		}

		synchronized void close() throws IOException {
			channel.close();
			stopped = true;
		}
	}

}
