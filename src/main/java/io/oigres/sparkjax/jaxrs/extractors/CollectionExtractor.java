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
package io.oigres.sparkjax.jaxrs.extractors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ParamConverter;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;

/**
 * @author Sergio Exposito
 */
public abstract class CollectionExtractor<T> implements ParamValueExtractor<Collection<T>> {

    public static <T> CollectionExtractor getInstance(final Class<?> collectionType, final ParamConverter<T> converter, final String parameterName, final String defaultValueString) {
    	if (List.class == collectionType) {
    		return new ListValueOf<>(converter, parameterName, defaultValueString);
    	} else if (Set.class == collectionType) {
    		return new SetValueOf<>(converter, parameterName, defaultValueString);
    	} else if (SortedSet.class == collectionType) {
    		return new SortedSetValueOf<>(converter, parameterName, defaultValueString);
    	} else {
    		throw new ProcessingException("collection.extractor.type.unsupported");
    	}
    }
	
	private final ParamConverter<T> paramConverter;
    private final String parameterName;
    private final String defaultValueString;

	protected CollectionExtractor(ParamConverter<T> paramConverter, String parameterName, String defaultValueString) {
		this.paramConverter = paramConverter;
		this.parameterName = parameterName;
		this.defaultValueString = defaultValueString;
	}

	@Override
	public String getName() {
		return this.parameterName;
	}

	@Override
	public String getDefaultValueString() {
		return this.defaultValueString;
	}

    protected final T fromString(String value) {
        T result = convert(value);
        if (result == null) {
            return defaultValue();
        }
        return result;
    }

    private T convert(String value) {
    	return paramConverter.fromString(value);
    }

    protected final T defaultValue() {
    	return fromString(this.defaultValueString);
    }
    
    protected final boolean isDefaultValueRegistered() {
        return defaultValueString != null;
    }

    public Collection<T> extract(final MultivaluedMap<String, String> parameters) {
        final List<String> stringList = parameters.get(getName());

        final Collection<T> valueList = newCollection();
        if (stringList != null) {
            for (final String v : stringList) {
                valueList.add(fromString(v));
            }
        } else if (isDefaultValueRegistered()) {
            valueList.add(defaultValue());
        } else {
        	return null;
        }

        return valueList;
    }

    protected abstract Collection<T> newCollection();

    private static final class ListValueOf<T> extends CollectionExtractor<T> {

        ListValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValueString) {
            super(converter, parameter, defaultValueString);
        }

        @Override
        protected List<T> newCollection() {
            return new ArrayList<>();
        }
    }

    private static final class SetValueOf<T> extends CollectionExtractor<T> {

        SetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValueString) {
            super(converter, parameter, defaultValueString);
        }

        @Override
        protected Set<T> newCollection() {
            return new HashSet<>();
        }
    }

    private static final class SortedSetValueOf<T> extends CollectionExtractor<T> {

        SortedSetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValueString) {
            super(converter, parameter, defaultValueString);
        }

        @Override
        protected SortedSet<T> newCollection() {
            return new TreeSet<>();
        }
    }

}
