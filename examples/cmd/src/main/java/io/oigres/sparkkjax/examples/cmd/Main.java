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
package io.oigres.sparkkjax.examples.cmd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.oigres.sparkkjax.examples.cmd.api.GlobalExceptionHandler;
import io.oigres.sparkkjax.examples.cmd.api.InfoResources;
import io.oigres.sparkkjax.examples.cmd.api.SwaggerResources;
import io.oigres.sparkjax.JsonTransformer;
import io.oigres.sparkjax.ResponseExceptionHandler;
import io.oigres.sparkjax.RouteBuilder;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author Sergio Exposito
 */
public class Main {

    public Main() {
        Spark.staticFileLocation("webapp");
        Spark.port(8080);
        defineResources();
    }

    private void defineResources() {
        Gson requestObjectMapper = new GsonBuilder().create();
        JsonTransformer jsonTransformer = new JsonTransformer(requestObjectMapper);
        ResponseExceptionHandler exceptionHandler = new GlobalExceptionHandler(jsonTransformer);
        RouteBuilder routeBuilder = new RouteBuilder(requestObjectMapper, new ResponseTransformerProvider() {
            @Override
            public ResponseTransformer getTransformer(MediaType mediaType) {
                if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
                    return jsonTransformer;
                }
                return null;
            }
        });
        routeBuilder.setupRoutes(
                Set.of(
                        new InfoResources(),
                        new SwaggerResources()
                )
        );
        Method[] methods = exceptionHandler.getClass().getMethods();
        for (Method m : methods) {
            if (Modifier.isPublic(m.getModifiers()) && m.getParameterCount() == 3) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (Exception.class.isAssignableFrom(parameterTypes[0]) &&
                        Request.class.isAssignableFrom(parameterTypes[1]) &&
                        Response.class.isAssignableFrom(parameterTypes[2])) {
                    final Method methodHandler = m;
                    Spark.exception((Class<Exception>)parameterTypes[0], (exception, request, response) -> errorHandlerInvoke(exception, request, response, exceptionHandler, methodHandler) );
                }
            }
        }
    }

    private void errorHandlerInvoke(Exception exception, Request request, Response response, Object errorHandler, Method methodErrorHandler) {
        Object[] parameters = new Object[3];
        parameters[0] = exception;
        parameters[1] = request;
        parameters[2] = response;
        try {
            methodErrorHandler.invoke(errorHandler, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //log.error("", e);
        }
    }

    public void start() {
        Spark.awaitInitialization();
        Spark.awaitStop();
    }

    static public void main(String[] args) {
        Main main = new Main();
        main.start();
    }

}