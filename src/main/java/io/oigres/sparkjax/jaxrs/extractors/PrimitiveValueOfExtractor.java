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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;

/**
 * @author Sergio Exposito
 */
public class PrimitiveValueOfExtractor implements ParamValueExtractor<Object> {
    private final Method valueOf;
    private final String parameter;
    private final String defaultStringValue;
    private final Object defaultValue;
    private final Object defaultPrimitiveTypeValue;

    public PrimitiveValueOfExtractor(Method valueOf, String parameter,
                                     String defaultStringValue, Object defaultPrimitiveTypeValue) {
        this.valueOf = valueOf;
        this.parameter = parameter;
        this.defaultStringValue = defaultStringValue;
        this.defaultValue = (defaultStringValue != null)
                ? getValue(defaultStringValue) : null;
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

    private Object getValue(String v) {
        try {
            return valueOf.invoke(null, v);
        } catch (InvocationTargetException ex) {
            Throwable target = ex.getTargetException();
            if (target instanceof WebApplicationException) {
                throw (WebApplicationException) target;
            } else {
                throw new ProcessingException(target);
            }
        } catch (Exception ex) {
            throw new ProcessingException(ex);
        }
    }

    @Override
    public Object extract(MultivaluedMap<String, String> parameters) {
        String v = parameters.getFirst(parameter);
        if (v != null && !v.trim().isEmpty()) {
            return getValue(v);
        } else if (defaultValue != null) {
            return defaultValue;
        }

        return defaultPrimitiveTypeValue;
    }

}
