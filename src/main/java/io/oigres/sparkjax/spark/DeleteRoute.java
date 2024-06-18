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
package io.oigres.sparkjax.spark;

import java.lang.reflect.Method;

import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.oigres.sparkjax.jaxrs.ParameterExtractorFactory;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;
import spark.Spark;
import spark.route.HttpMethod;

/**
 * @author Sergio Exposito
 */
public class DeleteRoute extends AbstractBodyRoute {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRoute.class);

	public DeleteRoute(String path, Object resource, Method method, Consumes defaultConsumes, Produces defaultProduces, ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider, Validator validator) {
		super(path, resource, method, defaultConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
		LOGGER.info(String.format("%s.%s DELETE %s", resource.getClass().getSimpleName(), method.getName(), path) );
		Spark.delete(getPath(), this, getResponseTransformer());
	}

	public HttpMethod getHttpMethod() {
        return HttpMethod.delete;
    }

}
