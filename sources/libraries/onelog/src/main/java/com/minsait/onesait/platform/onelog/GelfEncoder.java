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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Marker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.encoder.EncoderBase;
import lombok.Getter;
import lombok.Setter;


/**
 * This class is responsible for transforming a Logback log event to a GELF message.
 */

public class GelfEncoder extends EncoderBase<ILoggingEvent> {
	
	/** GELF Encoder version */
	private static final float GELF_ENCODER_VERSION = 1.0f;

    private static final Pattern VALID_ADDITIONAL_FIELD_PATTERN = Pattern.compile("^[\\w.-]*$");
    private static final String DEFAULT_SHORT_PATTERN = "%m%nopex";
    private static final String DEFAULT_FULL_PATTERN = "%m%n";
    private static final double MSEC_DIVIDER = 1000D;

    /** Jackson Serializer */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Origin hostname - will be auto detected if not specified.
     */
    @Getter
    @Setter
    private String originHost;

    /**
     * If true, the raw message (with argument placeholders) will be sent, too. Default: false.
     */
    @Getter
    @Setter
    private boolean includeRawMessage;

    /**
     * If true, logback markers will be sent, too. Default: true.
     */
    @Getter
    @Setter
    private boolean includeMarker = true;

    /**
     * If true, MDC keys/values will be sent, too. Default: true.
     */
    @Getter
    @Setter
    private boolean includeMdcData = true;

    /**
     * If true, caller data (source file-, method-, class name and line) will be sent, too.
     * Default: true.
     */
    @Getter
    @Setter
    private boolean includeCallerData = true;

    /**
     * If true, root cause exception of the exception passed with the log message will be
     * exposed in the exception field. Default: true.
     */
    @Getter
    @Setter
    private boolean includeRootCauseData = true;

    /**
     * If true, the log level name (e.g. DEBUG) will be sent, too. Default: true.
     */
    @Getter
    @Setter
    private boolean includeLevelName = true;

    /**
     * The key that should be used for the levelName.
     */
    @Getter
    @Setter
    private String levelNameKey = "level_name";

    /**
     * Short message format. Default: `"%m%nopex"`.
     */
    @Getter
    @Setter
    private PatternLayout shortPatternLayout;

    /**
     * Full message format (Stacktrace). Default: `"%m%n"`.
     */
    @Getter
    @Setter
    private PatternLayout fullPatternLayout;

    /**
     * Log numbers as String. Default: false .
     */
    @Getter
    @Setter
    private boolean numbersAsString = false;

    /**
     * Additional, static fields to send to graylog. Defaults: none.
     */
    private final Map<String, Object> staticFields = new ConcurrentHashMap<>();

    @Override
    public void start() {
        if (StringUtils.isBlank(originHost)) {
            originHost = OneLogUtils.getHostName();
        }
        if (shortPatternLayout == null) {
            shortPatternLayout = buildPattern(DEFAULT_SHORT_PATTERN);
        }
        if (fullPatternLayout == null) {
            fullPatternLayout = buildPattern(DEFAULT_FULL_PATTERN);
        }
        super.start();
    }

    private PatternLayout buildPattern(final String pattern) {
        final PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(getContext());
        patternLayout.setPattern(pattern);
        patternLayout.start();
        return patternLayout;
    }


    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] encode(final ILoggingEvent event) {
        final String shortMessage = shortPatternLayout.doLayout(event);
        final String fullMessage = fullPatternLayout.doLayout(event);
        final double timestamp = event.getTimeStamp() / MSEC_DIVIDER;
        final Map<String, Object> additionalFields = mapAdditionalFields(event);

        final Map<String, Object> message = new HashMap<>();
        message.put("version", GELF_ENCODER_VERSION);
        message.put("host", originHost);
        message.put("short_message", shortMessage);
        message.put("full_messsage", fullMessage);
        message.put("timestamp", OneLogUtils.formatToUSDecimalFormat(timestamp));
        message.put("level", LevelToSyslogSeverity.convert(event));
        for (final Map.Entry<String, Object> entry : additionalFields.entrySet()) {
        	message.put("_".concat(entry.getKey()), entry.getValue());
        }
        return serializeAsBinaryJSON(message);
    }

	private byte[] serializeAsBinaryJSON(final Map<String, Object> gelfMessage) {
		try {
			return mapper.writeValueAsBytes(gelfMessage);
		} catch (JsonProcessingException exception) {
			addError(exception.getMessage());
		}
		return new byte[0];
	}

    @Override
    public byte[] footerBytes() {
        return null;
    }

    private Map<String, Object> mapAdditionalFields(final ILoggingEvent event) {
        final Map<String, Object> additionalFields = new HashMap<>(staticFields);
        additionalFields.put("logger_name", event.getLoggerName());
        additionalFields.put("thread_name", event.getThreadName());

        if (includeRawMessage) {
            additionalFields.put("raw_message", event.getMessage());
        }

        if (includeMarker) {
            final Marker marker = event.getMarker();
            if (marker != null) {
                additionalFields.put("marker", marker.getName());
            }
        }

        if (includeLevelName) {
            additionalFields.put(levelNameKey, event.getLevel().levelStr);
        }

        if (includeMdcData) {
            additionalFields.putAll(buildMdcData(event.getMDCPropertyMap()));
        }

        if (includeCallerData) {
            additionalFields.putAll(buildCallerData(event.getCallerData()));
        }

        if (includeRootCauseData) {
            additionalFields.putAll(buildRootExceptionData(event.getThrowableProxy()));
        }
        return additionalFields;
    }

    private Map<String, Object> buildMdcData(final Map<String, String> mdcProperties) {
        if (mdcProperties == null || mdcProperties.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, Object> additionalFields = new HashMap<>();
        for (final Map.Entry<String, String> entry : mdcProperties.entrySet()) {
            addField(additionalFields, entry.getKey(), entry.getValue());
        }

        return additionalFields;
    }
    
    public void addStaticField(final String staticField) {
        final String[] split = staticField.split(":", 2);
        if (split.length == 2) {
            addField(staticFields, split[0].trim(), split[1].trim());
        } else {
            addWarn("staticField must be in format key:value - rejecting '" + staticField + "'");
        }
    }
    
    private void addField(final Map<String, Object> dst, final String key, final String value) {
        if (key.isEmpty()) {
            addWarn("staticField key must not be empty");
        } else if ("id".equalsIgnoreCase(key)) {
            addWarn("staticField key name 'id' is prohibited");
        } else if (dst.containsKey(key)) {
            addWarn("additional field with key '" + key + "' is already set");
        } else if (!VALID_ADDITIONAL_FIELD_PATTERN.matcher(key).matches()) {
            addWarn("staticField key '" + key + "' is illegal. "
                + "Keys must apply to regex ^[\\w.-]*$");
        } else {
            if (value != null) {
                dst.put(key, processValue(value));
            }
        }
    }
    
    private Object processValue(final String value) {
        if (!numbersAsString) {
            try {
                return Double.valueOf(value);
            } catch (final NumberFormatException e) {
                return value;
            }
        }
        return value;
    }

    private Map<String, Object> buildCallerData(final StackTraceElement[] callerData) {
        if (callerData == null || callerData.length == 0) {
            return Collections.emptyMap();
        }

        final StackTraceElement first = callerData[0];

        final Map<String, Object> callerDataMap = new HashMap<>(4);
        callerDataMap.put("source_file_name", first.getFileName());
        callerDataMap.put("source_method_name", first.getMethodName());
        callerDataMap.put("source_class_name", first.getClassName());
        callerDataMap.put("source_line_number", first.getLineNumber());

        return callerDataMap;
    }

    private Map<String, Object> buildRootExceptionData(final IThrowableProxy throwableProxy) {
        final Optional<IThrowableProxy> rootException = getRootException(throwableProxy);
        if (rootException.isPresent()) {
        	final IThrowableProxy exception = rootException.get();
        	final Map<String, Object> exceptionDataMap = new HashMap<>(2);
            exceptionDataMap.put("root_cause_class_name", exception.getClassName());
            exceptionDataMap.put("root_cause_message", exception.getMessage());
            return exceptionDataMap;
        }
        return Collections.emptyMap();
    }
    
    private Optional<IThrowableProxy> getRootException(final IThrowableProxy throwableProxy) {
        if (throwableProxy == null) {
            return Optional.empty();
        }
        IThrowableProxy rootCause = throwableProxy;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return Optional.of(rootCause);
    }

}
