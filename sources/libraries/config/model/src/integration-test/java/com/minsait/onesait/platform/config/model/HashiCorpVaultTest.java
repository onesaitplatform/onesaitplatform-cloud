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
package com.minsait.onesait.platform.config.model;


/*
 * Añadir la siguiente dependencia en el pom.xml
  		<dependency>
				<groupId>org.springframework.vault</groupId>
				<artifactId>spring-vault-core</artifactId>
				<version>2.3.2</version>
         </dependency>
 *
 */

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

public class HashiCorpVaultTest {


	public void testHashicorp() {
		final String alphabet = "0123456789ABCDE";
		final int N = alphabet.length();
		final Random rand = new Random();
		Map<String, String> r = null;
		final String role = "roledeprueba1" + alphabet.charAt(rand.nextInt(N));
		final String roleAdmin = "roleAdminprueba1" + alphabet.charAt(rand.nextInt(N));
		final String destino = "https://platform-vault.apps.openshift-noprod.onesaitgcp.com:443/";
		try (HashiCorpVaultAdmin vc1 = new HashiCorpVaultAdmin(destino, "7cde8723-c70b-51b6-162e-73b2605d6ac1",
				"d9a1df33-b458-8c0e-7d84-a5cd4c89c50c")){
			r = vc1.createAppRoleAppRole(roleAdmin);
			if (null == r) {
				System.out.println("AppRole: "+ roleAdmin + " already exists.");
				throw new GenericOPException("AppRole to create other AppRoles already exists.");
			}
			// FIXME: Si no está iniciado el proxy, dará error de conexión, pero no dirá que utiliza el proxy
			// De hecho, en las 4 primeras no utiliza el proxy!!!
			try (HashiCorpVaultAdmin vc = new HashiCorpVaultAdmin(destino, r)) {
				System.out.println("está inicializado: " + vc.isInitialized());
				System.out.println("está sellada: " + vc.isSealed());
				System.out.println("está ociosa: " + vc.isStandby());
				System.out.println("version: " + vc.version());
				r = vc.createAppRoleUser(role);
				if (r != null) {
					System.out.println("r=" + r.toString());
				} else {
					System.out.println("Ya existe ese rol");
					System.exit(1);
				}
				//-- listar roles existentes
				System.out.println(vc.listAppRoles());

				for (int i=1; i<50; i++) {
					try (HashiCorpVaultUser hc = new HashiCorpVaultUser(destino, r)) {
						//--usar role recién creada

						//						hc.setProxy("torresj.duckdns.org", 8082);
						System.out.println("Número aleatorio generado:" + hc.generateRandom(8192));
						final String res1 = hc.encrypt("prueba a cifrar".getBytes(StandardCharsets.UTF_8), "𝕿𝖍𝖊 𝖖𝖚𝖎𝖈𝖐 𝖇𝖗𝖔𝖜𝖓 𝖋𝖔𝖝 𝖏𝖚𝖒𝖕𝖘 𝖔𝖛𝖊𝖗 𝖙𝖍𝖊 𝖑𝖆𝖟𝖞 𝖉𝖔𝖌");
						final String res4 = hc.encrypt("prueba a cifrar".getBytes(StandardCharsets.UTF_8), "田中さんにあげて下さい");
						final byte[] res3 = hc.decrypt(res1, "𝕿𝖍𝖊 𝖖𝖚𝖎𝖈𝖐 𝖇𝖗𝖔𝖜𝖓 𝖋𝖔𝖝 𝖏𝖚𝖒𝖕𝖘 𝖔𝖛𝖊𝖗 𝖙𝖍𝖊 𝖑𝖆𝖟𝖞 𝖉𝖔𝖌");
						final byte[] res2 = hc.decrypt(res4, "田中さんにあげて下さい");  // FIXME: Habilitar esta ruta también
						System.out.println("cifrado: " + res1 + ", descifrado: " + res2 + " misma keyPath" + res3);

						hc.write("sec1", "prueba a cifrar\\u0001\\u0002");
						hc.write("sec1\\u0000&a=1", "prueba");
						final String rres1 = hc.read("sec1");
						final String rres2 = hc.read("sec1\\u0000&a=1");
						final byte[] data="\u0001\u0002".getBytes();
						hc.writeData("sec2", data);
						final byte[] rres4= hc.readData("sec2");
						System.out.println("Guardado: " + "aquí" + ", recuperado: " + rres1 + "." + rres2);
						System.out.println("recuperado Data" + String.valueOf(rres4) );
						final String firma = hc.sign("texto a firmar".getBytes(StandardCharsets.UTF_8), "clave", "sha3-256");
						System.out.println("firma de" + firma);
						System.out.println("Se valida" + hc.verify(firma, "texto a firmar".getBytes(StandardCharsets.UTF_8), "clave", "sha3-256"));
						System.out.println("claves;" + hc.listKeys());
					} catch (MalformedURLException | GenericOPException e) {
						e.printStackTrace();
					} catch (final Exception e1) {
						e1.printStackTrace();
					}
					finally {
					}
				}
				final boolean borrar=true;
				if (vc != null && borrar) {
					try {
						//-- listar roles existentes
						System.out.println("Lista de roles antes" + vc.listAppRoles());
						vc.deleteAppRole(role, r.get("role_id"));
						//-- listar roles existentes
						System.out.println("Lista de roles despues" + vc.listAppRoles());
					} catch (final GenericOPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} catch (MalformedURLException | GenericOPException e) {
				System.out.println(e.getLocalizedMessage()); // FIXME: Solo durante pruebas
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException | GenericOPException e) {
			System.out.println(e.getLocalizedMessage()); // FIXME: Solo durante pruebas
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

