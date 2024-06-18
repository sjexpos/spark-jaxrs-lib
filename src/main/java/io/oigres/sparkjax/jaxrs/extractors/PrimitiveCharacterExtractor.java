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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;

/**
 * @author Sergio Exposito
 */
public class PrimitiveCharacterExtractor implements ParamValueExtractor<Object> {

    final String parameter;
    final String defaultStringValue;
    final Object defaultPrimitiveTypeValue;

    public PrimitiveCharacterExtractor(String parameter, String defaultStringValue, Object defaultPrimitiveTypeValue) {
        this.parameter = parameter;
        this.defaultStringValue = defaultStringValue;
        this.defaultPrimitiveTypeValue = defaultPrimitiveTypeValue;
    }

    @Override
    public String getName() {
        return parameter;
    }

    @Override
    public String getDefaultValueString() {
        return defaultStringValue;
    }

    @Override
    public Object extract(MultivaluedMap<String, String> parameters) {
        String v = parameters.getFirst(parameter);
        if (v != null && !v.trim().isEmpty()) {
            if (v.length() == 1) {
                return v.charAt(0);
            } else {
                throw new ProcessingException("error.parameter.invalid.char.value");
            }
        } else if (defaultStringValue != null && !defaultStringValue.trim().isEmpty()) {
            if (defaultStringValue.length() == 1) {
                return defaultStringValue.charAt(0);
            } else {
                throw new ProcessingException("error.parameter.invalid.char.value");
            }
        }

        return defaultPrimitiveTypeValue;
    }

}
