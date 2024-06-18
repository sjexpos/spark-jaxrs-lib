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
package io.oigres.sparkkjax.examples.awslambda;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.GsonBuilder;
import org.reflections.Reflections;

import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;

/**
 * @author Sergio Exposito
 */
public class SwaggerParser {

    public static String getSwaggerJson(String packageName) {
        Swagger swagger = getSwagger(packageName);
        return swaggerToJson(swagger);
    }

    public static Swagger getSwagger(String packageName) {
        Reflections reflections = new Reflections(packageName);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage(packageName);
        beanConfig.setScan(true);
        beanConfig.scanAndRead();
        Swagger swagger = beanConfig.getSwagger();
        Reader reader = new Reader(swagger);
        Set<Class<?>> apiClasses = reflections.getTypesAnnotatedWith(Api.class);
        Set<Class<?>> swaggerDefClasses = reflections.getTypesAnnotatedWith(SwaggerDefinition.class);
        Set<Class<?>> readingClasses = new HashSet<>();
        readingClasses.addAll(apiClasses);
        readingClasses.addAll(swaggerDefClasses);
        return reader.read(readingClasses);
    }

    public static String swaggerToJson(Swagger swagger) {
        Gson objectMapper = new GsonBuilder().setExclusionStrategies(new FieldExclusionStrategy()).create();
        return objectMapper.toJson(swagger);
    }

}
