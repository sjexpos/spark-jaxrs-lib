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

import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;

/**
 * @author Sergio Exposito
 */
public abstract class AbstractStringReader<T> implements ParamConverter<T> {

    @Override
    public T fromString(final String value) {
        if (value == null) {
        	return null;
//            throw new IllegalArgumentException("method.parameter.cannot.be.null");
        }
        try {
            return _fromString(value);
        } catch (final InvocationTargetException ex) {
            // if the value is an empty string, return null
            if (value.isEmpty()) {
                return null;
            }
            final Throwable cause = ex.getCause();
            if (cause instanceof WebApplicationException) {
                throw (WebApplicationException) cause;
            } else {
                throw new ProcessingException(cause);
            }
        } catch (final Exception ex) {
            throw new ProcessingException(ex);
        }
    }

    protected abstract T _fromString(String value) throws Exception;

    @Override
    public String toString(final T value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("method.parameter.cannot.be.null");
        }
        return value.toString();
    }

}
