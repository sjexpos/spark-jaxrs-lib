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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sergio Exposito
 */
public class ReflectionHelper {

    private static final TypeVisitor<Class> eraser = new TypeVisitor<Class>() {
        @Override
        protected Class onClass(final Class clazz) {
            return clazz;
        }

        @Override
        protected Class onParameterizedType(final ParameterizedType type) {
            return visit(type.getRawType());
        }

        @Override
        protected Class onGenericArray(final GenericArrayType type) {
            return Array.newInstance(visit(type.getGenericComponentType()), 0).getClass();
        }

        @Override
        protected Class onVariable(final TypeVariable type) {
            return visit(type.getBounds()[0]);
        }

        @Override
        protected Class onWildcard(final WildcardType type) {
            return visit(type.getUpperBounds()[0]);
        }

        @Override
        protected RuntimeException createError(final Type type) {
            return new IllegalArgumentException("type.to.class.conversion.not.supported");
        }
    };
	
	private ReflectionHelper() {
	}

    public static PrivilegedAction<Constructor<?>> getStringConstructorPA(final Class<?> clazz) {
        return new PrivilegedAction<Constructor<?>>() {
            @Override
            public Constructor<?> run() {
                try {
                    return clazz.getConstructor(String.class);
                } catch (final SecurityException e) {
                    throw e;
                } catch (final Exception e) {
                    return null;
                }
            }
        };
    }

    public static Type[] getTypeArguments(final Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        return ((ParameterizedType) type).getActualTypeArguments();
    }
    
    public static <T> Class<T> erasure(final Type type) {
        return eraser.visit(type);
    }
    
    public static List<ClassTypePair> getTypeArgumentAndClass(final Type type) throws IllegalArgumentException {
        final Type[] types = getTypeArguments(type);
        if (types == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(types)
                     .map(type1 -> ClassTypePair.of(erasure(type1), type1))
                     .collect(Collectors.toList());
    }
    
    private static PrivilegedAction<Method> getStringToObjectMethodPA(final Class<?> clazz, final String methodName) {
        return new PrivilegedAction<Method>() {
            @Override
            public Method run() {
                try {
                    final Method method = clazz.getDeclaredMethod(methodName, String.class);
                    if (Modifier.isStatic(method.getModifiers()) && method.getReturnType() == clazz) {
                        return method;
                    }
                    return null;
                } catch (final NoSuchMethodException nsme) {
                    return null;
                }
            }
        };
    }

    public static PrivilegedAction<Method> getValueOfStringMethodPA(final Class<?> clazz) {
        return getStringToObjectMethodPA(clazz, "valueOf");
    }

    public static PrivilegedAction<Method> getFromStringStringMethodPA(final Class<?> clazz) {
        return getStringToObjectMethodPA(clazz, "fromString");
    }

}
