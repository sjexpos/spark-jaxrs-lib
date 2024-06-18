/**********
 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the
 Free Software Foundation; either version 3.0 of the License, or (at your
 option) any later version. (See <https://www.gnu.org/licenses/gpl-3.0.html>.)

 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 more details.

 You should have received a copy of the GNU General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 **********/
// Copyright (c) 2020-2024 Sergio Exposito.  All rights reserved.
package io.oigres.sparkjax.jaxrs.converters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * @author Sergio Exposito
 */
public class DateParamConverterProvider implements ParamConverterProvider {
    private static final String RFC1123_DATE_FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String RFC1036_DATE_FORMAT_PATTERN = "EEEE, dd-MMM-yy HH:mm:ss zzz";
    private static final String ANSI_C_ASCTIME_DATE_FORMAT_PATTERN = "EEE MMM d HH:mm:ss yyyy";
    private static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    private static final ThreadLocal<List<SimpleDateFormat>> dateFormats = new ThreadLocal<List<SimpleDateFormat>>() {

        @Override
        protected synchronized List<SimpleDateFormat> initialValue() {
            return createDateFormats();
        }
    };

    private static List<SimpleDateFormat> createDateFormats() {
        final SimpleDateFormat[] formats = new SimpleDateFormat[]{
            new SimpleDateFormat(RFC1123_DATE_FORMAT_PATTERN, Locale.US),
            new SimpleDateFormat(RFC1036_DATE_FORMAT_PATTERN, Locale.US),
            new SimpleDateFormat(ANSI_C_ASCTIME_DATE_FORMAT_PATTERN, Locale.US)
        };
        for (SimpleDateFormat sdf : formats) {
        	sdf.setTimeZone(GMT_TIME_ZONE);
        }
        return Collections.unmodifiableList(Arrays.asList(formats));
    }
	
	public DateParamConverterProvider() {
	}

	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        return (rawType != Date.class) ? null : new ParamConverter<T>() {

            @Override
            public T fromString(final String value) {
                if (value == null) {
                    throw new IllegalArgumentException("method.parameter.cannot.be.null");
                }
                try {
                	
                    return rawType.cast(readDate(value));
                } catch (final ParseException ex) {
                    throw new ProcessingException(ex);
                }
            }

            @Override
            public String toString(final T value) throws IllegalArgumentException {
                if (value == null) {
                    throw new IllegalArgumentException("method.parameter.cannot.be.null");
                }
                return value.toString();
            }
        };
	}

    private static Date readDate(final String date) throws ParseException {
        ParseException pe = null;
        for (final SimpleDateFormat f : dateFormats.get()) {
            try {
                Date result = f.parse(date);
                // parse can change time zone -> set it back to GMT
                f.setTimeZone(GMT_TIME_ZONE);
                return result;
            } catch (final ParseException e) {
                pe = (pe == null) ? e : pe;
            }
        }

        throw pe;
    }
}
