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
package io.oigres.sparkjax.jaxrs.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Type visitor contract.
 *
 * @author Sergio Exposito
 */
abstract class TypeVisitor<T> {

    /**
     * Visit the type and a given parameter.
     *
     * @param type visited type.
     * @return visiting result.
     */
    public final T visit(final Type type) {
        assert type != null;

        if (type instanceof Class) {
            return onClass((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return onParameterizedType((ParameterizedType) type);
        }
        if (type instanceof GenericArrayType) {
            return onGenericArray((GenericArrayType) type);
        }
        if (type instanceof WildcardType) {
            return onWildcard((WildcardType) type);
        }
        if (type instanceof TypeVariable) {
            return onVariable((TypeVariable<?>) type);
        }

        // covered all the cases
        assert false;

        throw createError(type);
    }

    /**
     * Visit class.
     *
     * @param clazz visited class.
     * @return visit result.
     */
    protected abstract T onClass(Class<?> clazz);

    /**
     * Visit parameterized type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onParameterizedType(ParameterizedType type);

    /**
     * Visit generic array type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onGenericArray(GenericArrayType type);

    /**
     * Visit type variable.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onVariable(TypeVariable<?> type);

    /**
     * Visit wildcard type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onWildcard(WildcardType type);

    /**
     * Create visiting error (in case the visitor could not recognize the visit type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected RuntimeException createError(final Type type) {
        throw new IllegalArgumentException();
    }
}
