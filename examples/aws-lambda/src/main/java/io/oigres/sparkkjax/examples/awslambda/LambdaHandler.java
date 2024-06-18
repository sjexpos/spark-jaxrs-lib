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

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.oigres.sparkkjax.examples.awslambda.api.InfoResources;
import io.oigres.sparkkjax.examples.awslambda.api.GlobalExceptionHandler;
import io.oigres.sparkkjax.examples.awslambda.api.SwaggerResources;
import io.oigres.sparkjax.JsonTransformer;
import io.oigres.sparkjax.ResponseExceptionHandler;
import io.oigres.sparkjax.RouteBuilder;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author Sergio Exposito
 */
public class LambdaHandler implements RequestStreamHandler {

    private SparkLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    public LambdaHandler() {
        try {
            handler = SparkLambdaContainerHandler.getAwsProxyHandler();
            defineResources();
            Spark.awaitInitialization();
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spark container", e);
        }
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

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }

}