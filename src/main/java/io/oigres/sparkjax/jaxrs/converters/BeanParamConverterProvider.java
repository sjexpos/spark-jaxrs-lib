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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import com.google.gson.Gson;

/**
 * @author Sergio Exposito
 */
public class BeanParamConverterProvider implements ParamConverterProvider {
	private Gson objectMapper;

	public BeanParamConverterProvider(Gson objectMapper) {
		this.objectMapper = objectMapper;
	}

	private boolean isCompatibleToAny(String[] mediaTypes) {
		for (String mediaType : mediaTypes) {
			if (MediaType.valueOf(mediaType).equals(MediaType.APPLICATION_JSON_TYPE)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType, final Annotation[] annotations) {
		Consumes consumes = (Consumes)Arrays.asList(annotations).stream().filter( a -> { return Consumes.class.isAssignableFrom(a.getClass()); } ).findFirst().orElse(null);
		if (consumes == null || !isCompatibleToAny(consumes.value()) || 
			rawType == List.class || rawType == Set.class || rawType == SortedSet.class || 
			rawType == String.class || rawType == Character.class || rawType.isPrimitive()) {
			return null;
		}
        return new AbstractStringReader<T>() {
        	private Class<T> currentRawType = rawType;
            @Override
            protected T _fromString(final String value) throws Exception {
            	if (value == null || "".equals(value.trim())) {
            		return null;
            	}
				return objectMapper.fromJson(value, this.currentRawType);
            }
        };
	}

}
