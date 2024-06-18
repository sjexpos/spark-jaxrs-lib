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

import java.lang.reflect.Type;

/**
 * A pair of raw class and the related type.
 *
 * @author Sergio Exposito
 */
public class ClassTypePair {

    private final Type type;
    private final Class<?> rawClass;

    private ClassTypePair(Class<?> c, Type t) {
        this.type = t;
        this.rawClass = c;
    }

    /**
     * Get the raw class of the {@link #type() type}.
     *
     * @return raw class of the type.
     */
    public Class<?> rawClass() {
        return rawClass;
    }

    /**
     * Get the actual type behind the {@link #rawClass() raw class}.
     *
     * @return the actual type behind the raw class.
     */
    public Type type() {
        return type;
    }

    /**
     * Create new type-class pair for a non-generic class.
     *
     * @param rawClass (raw) class representing the non-generic type.
     *
     * @return new non-generic type-class pair.
     */
    public static ClassTypePair of(Class<?> rawClass) {
        return new ClassTypePair(rawClass, rawClass);
    }

    /**
     * Create new type-class pair.
     *
     * @param rawClass raw class representing the type.
     * @param type type behind the class.
     *
     * @return new type-class pair.
     */
    public static ClassTypePair of(Class<?> rawClass, Type type) {
        return new ClassTypePair(rawClass, type);
    }
}
