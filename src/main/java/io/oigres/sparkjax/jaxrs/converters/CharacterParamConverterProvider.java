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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * @author Sergio Exposito
 */
public class CharacterParamConverterProvider implements ParamConverterProvider {

	public CharacterParamConverterProvider() {
	}

    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                              final Type genericType,
                                              final Annotation[] annotations) {
        if (rawType.equals(Character.class)) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(String value) {
                    if (value == null || value.isEmpty()) {
                        return null;
                    }
                    if (value.length() == 1) {
                        return rawType.cast(value.charAt(0));
                    }
                    throw new ProcessingException("error.parameter.invalid.char.value");
                }

                @Override
                public String toString(T value) {
                    if (value == null) {
                        throw new IllegalArgumentException("method.parameter.cannot.be.null");
                    }
                    return value.toString();
                }
            };
        }

        return null;
    }
}
