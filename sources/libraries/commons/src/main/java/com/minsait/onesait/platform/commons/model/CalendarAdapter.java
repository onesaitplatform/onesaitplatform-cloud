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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 * 
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.commons.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;

public final class CalendarAdapter {

	private static final Pattern TZ_REGEX = Pattern.compile("([+-][0-9][0-9]):?([0-9][0-9])$");

	// To format ISO Dates.Offsets not supported.
	private static final DateTimeFormatter isoDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	// To parse ISO dates
	private static final DateTimeFormatter isoDateParser = ISODateTimeFormat.dateTimeParser();

	private static final DateTimeFormatter localDateFormatter = DateTimeFormat.forStyle("MM")
			.withLocale(LocaleContextHolder.getLocale());

	public static SimpleDateFormat getFormat() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	}

	/**
	 * Añade el offset local del servidor a una fecha
	 * 
	 * @param date
	 * @return
	 */
	public static DateTime addLocalOffset(DateTime date) {
		long offsetInMilliseconds = getLocalOffset();
		return new DateTime(date.getMillis() + offsetInMilliseconds, date.getZone());
	}

	/**
	 * Formatea una fecha de joda-time a formato ISO
	 * 
	 * @param date
	 * @return
	 */
	public static String formatToIsoDate(DateTime date) {
		return isoDateFormatter.print(date);
	}

	/**
	 * Formatea una fecha Java a formato ISO
	 * 
	 * @param date
	 * @return
	 */
	public static String formatToIsoDate(Date date) {
		return formatToIsoDate(new DateTime(date));
	}

	/**
	 * Formatea una fecha de joda-time al formato del sistema.
	 * 
	 * @param date
	 * @return
	 */
	public static String formatToLocalDate(DateTime date) {
		return localDateFormatter.print(date);
	}

	/**
	 * Devuelve el offset en milisegundos del servidor con respecto a UTC
	 * 
	 * @return
	 */
	public static long getLocalOffset() {
		DateTimeZone tz = DateTimeZone.getDefault();
		Long instant = new DateTime().getMillis();
		return tz.getOffset(instant);
	}

	/**
	 * Devuelve la fecha actual del servidor
	 * 
	 * @return
	 */
	public static Calendar getLocalTime() {
		return Calendar.getInstance();
	}

	/**
	 * Convierte una fecha en milisegundos desde la época a un
	 * 
	 * @param millis
	 * @return
	 */
	public static Calendar epochToCalendar(long millis) {
		Calendar c = CalendarAdapter.getLocalTime();
		c.setTimeInMillis(millis);
		return c;
	}

	/**
	 * Devuelve la fecha actual de una zona horaria
	 * 
	 * @param tzCode
	 * @return
	 * @throws ParseException
	 */
	public static Calendar getLocalTime(String tzCode) {
		TimeZone timezone;
		if (tzCode == null) {
			timezone = TimeZone.getDefault();
		} else {
			timezone = TimeZone.getTimeZone(tzCode);
		}
		return Calendar.getInstance(timezone);
	}

	/**
	 * Devuelve la fecha local del servidor en milisegundos.
	 * 
	 * @return
	 */
	public static long getLocalTimeInMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * Formatea una fecha en formato ISO.
	 * 
	 * @param arg0
	 * @return
	 */
	public static String marshal(Calendar arg0) {
		return getFormat().format(arg0.getTime());
	}

	/**
	 * Devuelve la fecha actual del servidor en formato ISO
	 * 
	 * @return
	 */
	public static String marshalCurrentDate() {
		return isoDateFormatter.print(System.currentTimeMillis());
	}

	/**
	 * Devuelve la fecha actual UTC en formato ISO
	 * 
	 * @return
	 */
	public static String marshalUtcDate() {
		return new DateTime(DateTimeZone.UTC).toString();
	}

	/**
	 * Devuelve la fecha actual UTC
	 * 
	 * @return
	 */
	public static DateTime getUtcDate() {
		return new DateTime(DateTimeZone.UTC);
	}

	/**
	 * Devuelve la fecha actual en La zona Horaria indicada
	 * 
	 * @return
	 */
	public static DateTime getTimezoneDate(String timezoneId) {
		return new DateTime(DateTimeZone.forID(timezoneId));
	}

	/**
	 * Redondea una fecha a la baja con la unidad que se le pasa como argumento (por
	 * ejemplo, la fecha 2015-01-03T12:13:42 redondeada a la hora sería
	 * 2015-01-03T12:00:00).
	 * 
	 * @param date
	 * @param unit
	 * @return
	 */
	public static DateTime roundDate(DateTime date, TIME_UNIT unit) {
		int year = date.getYear();
		int month = date.getMonthOfYear();
		int dayOfMonth = date.getDayOfMonth();
		int hours = date.getHourOfDay();
		int minutes = date.getMinuteOfHour();
		int seconds = date.getSecondOfMinute();
		switch (unit) {
		case SECONDS:
			return new DateTime(year, month, dayOfMonth, hours, minutes, seconds, 0, date.getZone());
		case MINUTES:
			return new DateTime(year, month, dayOfMonth, hours, minutes, 0, 0, date.getZone());
		case DAYS:
			return new DateTime(year, month, dayOfMonth, 0, 0, 0, 0, date.getZone());
		case HOURS:
			return new DateTime(year, month, dayOfMonth, hours, 0, 0, 0, date.getZone());
		case WEEKS:
			return new DateTime(year, month, dayOfMonth, 0, 0, 0, 0, date.getZone()).dayOfWeek()
					.setCopy(DateTimeConstants.MONDAY);
		case MONTHS:
			return new DateTime(year, month, 1, 0, 0, 0, 0, date.getZone());
		case YEARS:
			return new DateTime(year, 1, 1, 0, 0, 0, 0, date.getZone());
		default: 
			DateTime dateRoundedToDay = roundDate(date, TIME_UNIT.DAYS);
			return substractPeriod(dateRoundedToDay, dateRoundedToDay.getDayOfWeek() - 1, TIME_UNIT.DAYS);
		
		}
	}

	/**
	 * Redondea una fecha al cuarto de hora al que pertenece (por ejemplo, la fecha
	 * 2015-01-03T12:33:42 redondeada a la hora sería 2015-01-03T12:30:00).
	 * 
	 * @param date
	 * @return
	 */
	public static DateTime roundDateToQuarter(DateTime date) {
		int min = date.getMinuteOfHour();

		if (min < 15) {
			return roundDate(date.minuteOfHour().setCopy(0), TIME_UNIT.MINUTES);
		} else if (min < 30) {
			return roundDate(date.minuteOfHour().setCopy(15), TIME_UNIT.MINUTES);
		} else if (min < 45) {
			return roundDate(date.minuteOfHour().setCopy(30), TIME_UNIT.MINUTES);
		}
		return roundDate(date.minuteOfHour().setCopy(45), TIME_UNIT.MINUTES);
	}

	/**
	 * Resta un periodo de tiempo a una fecha.
	 * 
	 * @param date
	 * @param period
	 * @param unit
	 * @return
	 */
	public static DateTime substractPeriod(DateTime date, int period, TIME_UNIT unit) {
		int offset = 0;
		switch (unit) {
		case SECONDS:
			offset = 1000;
			break;
		case MINUTES:
			offset = 60 * 1000;
			break;
		case HOURS:
			offset = 60 * 60 * 1000;
			break;
		case DAYS:
			offset = 24 * 60 * 60 * 1000;
			break;
		case WEEKS:
			offset = 7 * 24 * 60 * 60 * 1000;
			break;
		case MONTHS:
			offset = 30 * 7 * 24 * 60 * 60 * 1000;
			break;
		case YEARS:
			offset = 365 * 7 * 24 * 60 * 60 * 1000;
			break;
		}
		return new DateTime(date.getMillis() - period * offset, date.getZone());
	}

	/**
	 * Parsea una fecha en formato ISO
	 * 
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDateTime(String s) throws ParseException {

		Matcher mat = TZ_REGEX.matcher(s);
		TimeZone tz = null;
		if (mat.find()) {
			String tzCode = "GMT" + mat.group(1) + mat.group(2);
			// "GMT+0100"
			tz = TimeZone.getTimeZone(tzCode);
		}
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		if (tz != null) {
			formatter.setTimeZone(tz);
		}

		return formatter.parse(s);
	}

	/**
	 * Parsea una fecha ISO y la convierte a la hora local del servidor.
	 * 
	 * @param date
	 * @return
	 */
	public static DateTime parseIsoDate(String date) {
		return isoDateParser.parseDateTime(date);
	}

	/**
	 * Parsea una fecha ISO y la convierte a la zona horaria especificada como
	 * argumento.
	 * 
	 * @param date
	 * @param sourceTimeZone
	 * @return
	 */
	public static DateTime parseSofia2IsoDate(String date, String sourceTimeZone) {
		long timeInMillis = isoDateParser.parseDateTime(date).getMillis();
		DateTimeZone localTimezone = DateTimeZone.forID(sourceTimeZone);
		long utcOffset = localTimezone.getOffset(new DateTime(localTimezone));
		return new DateTime(timeInMillis - utcOffset, localTimezone);
	}

	/**
	 * Devuelve la "fecha Sofia2" para la zona horaria que se le pasa como
	 * argumento.
	 * 
	 * @param timezoneId
	 * @return
	 */
	public static DateTime getCurrentSofia2Date(String timezoneId) {
		DateTimeZone localTimezone = DateTimeZone.forID(timezoneId);
		long utcOffset = localTimezone.getOffset(new DateTime(localTimezone));
		return new DateTime(System.currentTimeMillis() + utcOffset, DateTimeZone.UTC);
	}

	/**
	 * Resta el offset local a una fecha
	 * 
	 * @param date
	 * @return
	 */
	public static DateTime substractLocalOffset(DateTime date) {
		long offsetInMilliseconds = getLocalOffset();
		return new DateTime(date.getMillis() - offsetInMilliseconds, date.getZone());
	}

	/**
	 * Parsea una fecha en formato ISO
	 * 
	 * @param arg0
	 * @return
	 * @throws ParseException
	 */
	public static Calendar unmarshal(String arg0) throws ParseException {
		Date d = parseDateTime(arg0);
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		return cal;
	}

	/**
	 * Tipo enumerado para representar una unidad temporal
	 *
	 */
	public enum TIME_UNIT {
		SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS;
	}

	/**
	 * Resta dos fechas, devolviendo la diferencia en la unidad temporal
	 * especificada.
	 * 
	 * @param startDate
	 * @param endDate
	 * @param unit
	 * @return
	 */
	public static int substractDates(Calendar startDate, Calendar endDate, TIME_UNIT unit) {
		DateTime initialDate = new DateTime(startDate.getTimeInMillis());
		DateTime finalDate = new DateTime(endDate.getTimeInMillis());
		return substractDates(initialDate, finalDate, unit);
	}

	/**
	 * Resta dos fechas, devolviendo la diferencia en la unidad temporal
	 * especificada.
	 * 
	 * @param startDate
	 * @param endDate
	 * @param unit
	 * @return
	 */
	public static int substractDates(DateTime initialDate, DateTime finalDate, TIME_UNIT unit) {
		switch (unit) {
		case SECONDS:
			return Seconds.secondsBetween(initialDate, finalDate).getSeconds();
		case MINUTES:
			return Minutes.minutesBetween(initialDate, finalDate).getMinutes();
		case HOURS:
			return Hours.hoursBetween(initialDate, finalDate).getHours();
		case DAYS:
			return Days.daysBetween(initialDate, finalDate).getDays();
		case WEEKS:
			return Weeks.weeksBetween(initialDate, finalDate).getWeeks();
		case MONTHS:
			return Months.monthsBetween(initialDate, finalDate).getMonths();
		default:
			return Years.yearsBetween(initialDate, finalDate).getYears();
		}
	}

	public static boolean isValidTimezoneId(String timezoneId) {
		try {
			DateTimeZone.forID(timezoneId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String getServerTimezoneId() {
		return DateTimeZone.getDefault().getID();
	}

}