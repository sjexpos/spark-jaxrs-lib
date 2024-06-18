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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;
import io.oigres.sparkjax.jaxrs.ParameterExtractorFactory;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;
import io.oigres.sparkjax.jaxrs.ValueParamProvider;
import io.oigres.sparkjax.jaxrs.providers.QueryParamValueParamProvider;
import spark.Spark;
import spark.route.HttpMethod;

/**
 * @author Sergio Exposito
 */
public class GetRoute extends AbstractRoute {
	private static final Logger LOGGER = LoggerFactory.getLogger(GetRoute.class);

	public GetRoute(String path, Object resource, Method method, Consumes defautConsumes, Produces defaultProduces, ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider, Validator validator) {
		super(path, resource, method, defautConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
		LOGGER.info(String.format("%s.%s GET %s", resource.getClass().getSimpleName(), method.getName(), path) );
		Spark.get(getPath(), this, getResponseTransformer());
	}

	public HttpMethod getHttpMethod() {
        return HttpMethod.get;
    }

	@Override
	protected ValueParamProvider createValueParamProvider(Parameter parameter, Annotation[] parameterAnnotations, ParameterExtractorFactory parameterExtractorFactory) {
		ValueParamProvider provider = super.createValueParamProvider(parameter, parameterAnnotations, parameterExtractorFactory);
		if (provider == null) {
			QueryParam queryParamAnnotation = Arrays.asList(parameterAnnotations)
					.stream()
					.filter(annotation ->  QueryParam.class.isAssignableFrom(annotation.getClass()) )
					.map( annotation -> (QueryParam)annotation )
					.findAny()
					.orElse(null);
			if (queryParamAnnotation != null) {
				ParamValueExtractor<?> paramValueExtractor = parameterExtractorFactory.get(parameter, parameterAnnotations, queryParamAnnotation.value());
				provider = new QueryParamValueParamProvider(paramValueExtractor);
			}
		}
		return provider;
	}
	
}
