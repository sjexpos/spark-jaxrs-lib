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

import javax.ws.rs.core.MultivaluedMap;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;

/**
 * @author Sergio Exposito
 */
abstract public class StringCollectionExtractor implements ParamValueExtractor<Collection<String>> {

    public static StringCollectionExtractor getInstance(Class<?> collectionType, String parameterName, String defaultValue) {
        if (List.class == collectionType) {
            return new ListString(parameterName, defaultValue);
        } else if (Set.class == collectionType) {
            return new SetString(parameterName, defaultValue);
        } else if (SortedSet.class == collectionType) {
            return new SortedSetString(parameterName, defaultValue);
        } else {
            throw new RuntimeException("Unsupported collection type: " + collectionType.getName());
        }
    }
	
    private final String parameterName;
    private final String defaultValueString;

    protected StringCollectionExtractor(String parameterName, String defaultValueString) {
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

	@Override
	public Collection<String> extract(MultivaluedMap<String, String> parameters) {
		List<String> stringList = parameters.get(getName());
        final Collection<String> collection = newCollection();
        if (stringList != null) {
            collection.addAll(stringList);
        } else if (getDefaultValueString() != null) {
            collection.add(getDefaultValueString());
        } else {
        	return null;
        }
        return collection;
	}

	protected abstract Collection<String> newCollection();
	
	
    private static final class ListString extends StringCollectionExtractor {

        public ListString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected List<String> newCollection() {
            return new ArrayList<String>();
        }
    }

    private static final class SetString extends StringCollectionExtractor {

        public SetString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected Set<String> newCollection() {
            return new HashSet<String>();
        }
    }

    private static final class SortedSetString extends StringCollectionExtractor {

        public SortedSetString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected SortedSet<String> newCollection() {
            return new TreeSet<String>();
        }
    }
	
}
