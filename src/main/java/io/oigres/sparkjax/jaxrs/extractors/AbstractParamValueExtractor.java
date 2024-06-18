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

import javax.ws.rs.ext.ParamConverter;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;

/**
 * @author Sergio Exposito
 */
public abstract class AbstractParamValueExtractor<T> implements ParamValueExtractor<T> {
    private final ParamConverter<T> paramConverter;
    private final String parameterName;
    private final String defaultValueString;

    protected AbstractParamValueExtractor(ParamConverter<T> paramConverter, String parameterName,
			String defaultValueString) {
		super();
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
        if (result == null && isDefaultValueRegistered()) {
            return defaultValue();
        }
        return result;
    }

    private T convert(String value) {
    	return paramConverter.fromString(value);
    }

    protected final T defaultValue() {
    	return convert(this.defaultValueString);
    }
    
    protected final boolean isDefaultValueRegistered() {
        return defaultValueString != null;
    }

}
