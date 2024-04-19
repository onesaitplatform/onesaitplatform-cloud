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
package com.minsait.onesait.platform.persistence.services.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

public class QueryParsers {
	/**
	 * This function searches the query for the functions now (...) And replace them
	 * with the current date. The structure of the function is now ("format",
	 * "unitTime", - | + amount). format: a valid date format such as
	 * "yyyy-MM-dd'T'HH: mm: ss'Z '" unitTime: year, month, date, hour, minute,
	 * second, millisecond amount: the amount that is added or subtracted from
	 * unitTime to the current date. we can write the function: now () now
	 * ("format") now ("format", "unitTime", - | + amount) now ("unitTime", - | +
	 * amount)
	 * 
	 * 
	 */
	public static String parseFunctionNow(String query) {
		final StringBuffer stringBuffer = new StringBuffer();
		try {
			// Get all now()
			Pattern pattern = Pattern.compile("(now|NOW)\\((\\w|\\s|'|\"|\\\\|/|:|-|\\+|,)*\\)");
			Matcher matcher = pattern.matcher(query);

			// treat each occurrence
			while (matcher.find()) {

				matcher.appendReplacement(stringBuffer, parseNow(matcher.group()));
			}
			matcher.appendTail(stringBuffer);
		} catch (Exception e) {
			throw new DBPersistenceException(
					"Problem with function NOW(), can be used without parameters or NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\") or NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\",'unitTime', amount) or NOW('unitTime', amount), where 'unitTime' can be 'year', 'month', 'date', 'hour', 'minute', 'second', 'millisecond' and amount a positive or negative whole value");
		}
		return stringBuffer.toString();
	}

	public static boolean hasNowFunction(String query) {
		try {
			// Get all now()
			Pattern pattern = Pattern.compile("(now|NOW)\\((\\w|\\s|'|\"|\\\\|/|:|-|\\+|,)*\\)");
			Matcher matcher = pattern.matcher(query);

			// treat each occurrence
			return matcher.find();
		} catch (Exception e) {
			throw new DBPersistenceException(
					"Problem with function NOW(), can be used without parameters or NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\") or NOW(\"yyyy-MM-dd'T'HH:mm:ss'Z'\",'unitTime', amount) or NOW('unitTime', amount), where 'unitTime' can be 'year', 'month', 'date', 'hour', 'minute', 'second', 'millisecond' and amount a positive or negative whole value");
		}
	}

	private static String parseNow(String now) {
		String result = "";
		String parameters = now.substring(now.indexOf('(') + 1, now.lastIndexOf(')'));

		String[] parts = parameters.split(",");
		if (parts.length == 1 && parts[0].trim().length() == 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			result = sdf.format(new Date());
		} else if (parts.length == 1) {

			SimpleDateFormat sdf = new SimpleDateFormat(parseFormat(parts[0]));
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			result = sdf.format(new Date());
		} else if (parts.length == 2) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

			// UnitTime
			// amount
			Calendar c = parseUnitTime(parts[0], parts[1]);
			result = sdf.format(c.getTime());

		} else if (parts.length == 3) {
			// format

			SimpleDateFormat sdf = new SimpleDateFormat(parseFormat(parts[0]));
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

			// UnitTime
			// amount
			Calendar c = parseUnitTime(parts[1], parts[2]);
			result = sdf.format(c.getTime());

		}

		return "'" + result + "'";
	}

	private static String parseFormat(String format) {
		String result = format;

		if (result.trim().startsWith("'") || result.trim().startsWith("\"")) {
			result = result.trim().substring(1, result.trim().length() - 1);
		}
		return result;
	}

	private static Calendar parseUnitTime(String partField, String partInc) {

		Calendar c = new GregorianCalendar();
		String field = partField.trim().toLowerCase();
		int inc = Integer.parseInt(partInc.trim());
		if (field.contains("year")) {
			c.add(Calendar.YEAR, inc);
		} else if (field.contains("month")) {
			c.add(Calendar.MONTH, inc);
		} else if (field.contains("date")) {
			c.add(Calendar.DATE, inc);
		} else if (field.contains("hour")) {
			c.add(Calendar.HOUR_OF_DAY, inc);
		} else if (field.contains("minute")) {
			c.add(Calendar.MINUTE, inc);
		} else if (field.contains("second")) {
			c.add(Calendar.SECOND, inc);
		} else if (field.contains("millisecond")) {
			c.add(Calendar.MILLISECOND, inc);
		}
		return c;
	}
}
