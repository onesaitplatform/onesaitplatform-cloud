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

import java.util.Random;
import java.util.function.Supplier;

public class UdpMessageIdGenerator implements Supplier<Long> {

	private static final Random RANDOM = new Random();
	private int idInitSeed = RANDOM.nextInt();

	@Override
	public Long get() {
		// TODO Auto-generated method stub
		// Cut the id to 8 bytes as GELF does not support 16 UUID
		return (long) idInitSeed << 32 | System.nanoTime() & 0xffffffffL;
	}

}
