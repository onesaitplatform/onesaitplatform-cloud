/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.router.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

	public static Date getToday() {
		return new Date();
	}

	public static Date dateRelativeTo(int relativeTo) {

		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.DAY_OF_MONTH, relativeTo);
		return calendar.getTime();
	}

	public static Date dateRelativeTo(Date date, int relativeTo) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.add(Calendar.DAY_OF_MONTH, relativeTo);
		return calendar.getTime();
	}

	public static boolean nullEmpty(List l) {
		if (l == null)
			return true;
		else
			return l.isEmpty();
	}

	public static boolean nullString(String l) {
		if (l == null)
			return true;
		else
			return l.equalsIgnoreCase("");
	}

	public static boolean equalsString(String a, String b) {
		// if a or b are null, then return false in any case
		if ((a == null) || (b == null))
			return false;
		else
			return a.equalsIgnoreCase(b);
	}

	public static String toListString(List<String> ids) {

		List<String> names = ids.stream().map(x -> ("\"" + x + "\"")).collect(Collectors.toList());

		return names.stream().collect(Collectors.joining(","));
	}

	public static String toListObjectIdString(List<String> ids) {

		List<String> names = ids.stream().map(x -> ("ObjectId(\"" + x + "\")")).collect(Collectors.toList());

		return names.stream().collect(Collectors.joining(","));
	}

	public static List<String> filterListNotIn(List<String> total, List<String> filter) {

		TreeSet<String> set = new TreeSet(Arrays.asList(total));
		set.removeAll(Arrays.asList(filter));
		return new ArrayList<>(set);
	}

	public static String findStringInList(List<String> list, String str) {
		return list.stream().filter(x -> str.equalsIgnoreCase(x)).findAny().orElse(null);
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static InputStream getResourceFromFile(File resourcePath) {
		return getInputStream(resourcePath);

	}

	public static byte[] getBytes(InputStream is) throws IOException {

		int len;
		int size = 1024;
		byte[] buf;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			buf = bos.toByteArray();
		}
		return buf;
	}

	public static InputStream getInputStream(File f) {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			return is;
		}
		return is;
	}

	public static InputStream getInputStream(String cadena) {
		return new ByteArrayInputStream(cadena.getBytes(StandardCharsets.UTF_8));
	}

	public static String getString(InputStream is) {

		StringBuilder sb = new StringBuilder();

		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
		    log.error("" + e);
		}

		return sb.toString();

	}

	public static File storeFile(byte[] data, String path, String fileName, float compressionQuality)
			throws IOException {

		File realPath = new File(path + "/" + fileName);

		new File(path).mkdirs();

		InputStream in = new ByteArrayInputStream(data);
		BufferedImage image = ImageIO.read(in);

		File outputfile = realPath;
		OutputStream os = new FileOutputStream(outputfile);

		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		ImageWriter writer = writers.next();

		ImageOutputStream ios = ImageIO.createImageOutputStream(os);
		writer.setOutput(ios);

		ImageWriteParam param = writer.getDefaultWriteParam();

		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(compressionQuality); // Change the quality value you prefer
		writer.write(null, new IIOImage(image, null, null), param);

		os.close();
		ios.close();
		writer.dispose();

		return realPath;

	}

	public static File storeFile(byte[] data, String path, String fileName) throws IOException {
		return storeFile(data, path, fileName, 1f);
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

	public static int randInt(int min, int max) {
		// Usually this can be a field rather than a method variable
		Random rand = new Random();
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		return rand.nextInt((max - min) + 1) + min;
	}

	public static boolean even(int num) {
		return (num % 2 == 0);
	}

}
