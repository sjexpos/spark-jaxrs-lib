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
package io.oigres.sparkjax.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.WeakHashMap;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import io.oigres.sparkjax.jaxrs.extractors.CollectionExtractor;
import io.oigres.sparkjax.jaxrs.extractors.PrimitiveCharacterExtractor;
import io.oigres.sparkjax.jaxrs.extractors.PrimitiveValueOfExtractor;
import io.oigres.sparkjax.jaxrs.extractors.SingleStringValueExtractor;
import io.oigres.sparkjax.jaxrs.extractors.SingleValueExtractor;
import io.oigres.sparkjax.jaxrs.extractors.StringCollectionExtractor;
import io.oigres.sparkjax.jaxrs.util.ClassTypePair;
import io.oigres.sparkjax.jaxrs.util.ReflectionHelper;

/**
 * @author Sergio Exposito
 */
public class ParameterExtractorFactory {
	public static final Map<Class<?>, Class<?>> primitiveToClassMap = getPrimitiveToClassMap();
	public static final Map<Class<?>, Object> primitiveToDefaultValueMap = getPrimitiveToDefaultValueMap();

	private static Map<Class<?>, Class<?>> getPrimitiveToClassMap() {
		Map<Class<?>, Class<?>> m = new WeakHashMap<>();
		// Put all primitive to wrapper class mappings except
		// that for Character
		m.put(Boolean.TYPE, Boolean.class);
		m.put(Byte.TYPE, Byte.class);
		m.put(Character.TYPE, Character.class);
		m.put(Short.TYPE, Short.class);
		m.put(Integer.TYPE, Integer.class);
		m.put(Long.TYPE, Long.class);
		m.put(Float.TYPE, Float.class);
		m.put(Double.TYPE, Double.class);
		return Collections.unmodifiableMap(m);
	}

	private static Map<Class<?>, Object> getPrimitiveToDefaultValueMap() {
		Map<Class<?>, Object> m = new WeakHashMap<>();
		m.put(Boolean.class, Boolean.valueOf(false));
		m.put(Byte.class, Byte.valueOf((byte) 0));
		m.put(Character.class, Character.valueOf((char) 0x00));
		m.put(Short.class, Short.valueOf((short) 0));
		m.put(Integer.class, Integer.valueOf(0));
		m.put(Long.class, Long.valueOf(0L));
		m.put(Float.class, Float.valueOf(0.0f));
		m.put(Double.class, Double.valueOf(0.0d));
		return Collections.unmodifiableMap(m);
	}

	private ParamConverterProvider paramConverterProvider;

	public ParameterExtractorFactory(ParamConverterProvider paramConverterProvider) {
		this.paramConverterProvider = paramConverterProvider;
	}

	public ParamValueExtractor<?> get(final Parameter parameter, final Annotation[] parameterAnnotations, final String parameterName) {
		DefaultValue defaultValueAnnotation = Arrays.asList(parameterAnnotations)
				.stream()
				.filter(annotation -> DefaultValue.class.isAssignableFrom(annotation.getClass()))
				.map(annotation -> (DefaultValue) annotation)
				.findAny()
				.orElse(null);
		String defaultValue = null;
		if (defaultValueAnnotation != null) {
			defaultValue = defaultValueAnnotation.value();
		}
		return process(this.paramConverterProvider, defaultValue, parameter.getType(), parameter.getParameterizedType(),
				parameterAnnotations, parameterName);
	}

	private ParamValueExtractor<?> process(final ParamConverterProvider paramConverterProvider, final String defaultValue, final Class<?> rawType, final Type type, final Annotation[] parameterAnnotations, final String parameterName) {
		ParamConverter<?> converter = paramConverterProvider.getConverter(rawType, type, parameterAnnotations);
		if (converter != null) {
			return new SingleValueExtractor<>(converter, parameterName, defaultValue);
		}

		if (rawType == List.class || rawType == Set.class || rawType == SortedSet.class) {
            final List<ClassTypePair> typePairs = ReflectionHelper.getTypeArgumentAndClass(type);
            final ClassTypePair typePair = (typePairs.size() == 1) ? typePairs.get(0) : null;

            if (typePair == null || typePair.rawClass() == String.class) {
				return StringCollectionExtractor.getInstance(rawType, parameterName, defaultValue);
			}
			converter = paramConverterProvider.getConverter(typePair.rawClass(), typePair.type(), parameterAnnotations);
			if (converter == null) {
				return null;
			}
			return CollectionExtractor.getInstance(rawType, converter, parameterName, defaultValue);
		}
		if (rawType == String.class) {
			return new SingleStringValueExtractor(parameterName, defaultValue);
		} else if (rawType == Character.class) {
			return new PrimitiveCharacterExtractor(parameterName, defaultValue, primitiveToDefaultValueMap.get(rawType));
		} else if (rawType.isPrimitive()) {
			final Class<?> wrappedRaw = primitiveToClassMap.get(rawType);
			if (wrappedRaw == null) {
				return null;
			}
			if (wrappedRaw == Character.class) {
				return new PrimitiveCharacterExtractor(parameterName, defaultValue, primitiveToDefaultValueMap.get(wrappedRaw));
			}
			final Method valueOf = AccessController.doPrivileged(ReflectionHelper.getValueOfStringMethodPA(wrappedRaw));
			if (valueOf != null) {
				return new PrimitiveValueOfExtractor(valueOf, parameterName, defaultValue, primitiveToDefaultValueMap.get(wrappedRaw));
			}
		}
		return null;
	}

}
