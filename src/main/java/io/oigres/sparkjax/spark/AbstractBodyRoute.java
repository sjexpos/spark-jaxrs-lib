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
import java.util.List;

import javax.validation.Validator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;
import io.oigres.sparkjax.jaxrs.ParameterExtractorFactory;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;
import io.oigres.sparkjax.jaxrs.ValueParamProvider;
import io.oigres.sparkjax.jaxrs.providers.BeanParamValueParamProvider;
import io.oigres.sparkjax.jaxrs.providers.FormParamValueParamProvider;
import io.oigres.sparkjax.jaxrs.providers.QueryParamValueParamProvider;

/**
 * @author Sergio Exposito
 */
public abstract class AbstractBodyRoute extends AbstractRoute {
	public static final String BODY_NAME = "BODY_PARAMETER";

	public AbstractBodyRoute(String path, Object resource, Method method, Consumes defaultConsumes, Produces defaultProduces, ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider, Validator validator) {
		super(path, resource, method, defaultConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
	}

	@Override
	protected ValueParamProvider createValueParamProvider(Parameter parameter, Annotation[] parameterAnnotations, ParameterExtractorFactory parameterExtractorFactory) {
		ValueParamProvider provider = super.createValueParamProvider(parameter, parameterAnnotations, parameterExtractorFactory);
		if (provider == null) {
			List<Annotation> parameterAnnotationsAsList = Arrays.asList(parameterAnnotations);
			BeanParam beanParamAnnotation = parameterAnnotationsAsList
					.stream()
					.filter(annotation ->  BeanParam.class.isAssignableFrom(annotation.getClass()) )
					.map( annotation -> (BeanParam)annotation )
					.findAny()
					.orElse(null);
			if (beanParamAnnotation != null) {
				ParamValueExtractor<?> paramValueExtractor = parameterExtractorFactory.get(parameter, parameterAnnotations, BODY_NAME);
				return new BeanParamValueParamProvider(paramValueExtractor);
			} else {
				FormParam formParamAnnotation = parameterAnnotationsAsList
						.stream()
						.filter(annotation -> FormParam.class.isAssignableFrom(annotation.getClass()))
						.map(annotation -> (FormParam) annotation)
						.findAny()
						.orElse(null);
				if (formParamAnnotation != null) {
					ParamValueExtractor<?> paramValueExtractor = parameterExtractorFactory.get(parameter, parameterAnnotations, formParamAnnotation.value());
					return new FormParamValueParamProvider(paramValueExtractor);
				} else {
					QueryParam queryParamAnnotation = Arrays.asList(parameterAnnotations)
							.stream()
							.filter(annotation ->  QueryParam.class.isAssignableFrom(annotation.getClass()) )
							.map( annotation -> (QueryParam)annotation )
							.findAny()
							.orElse(null);
					if (queryParamAnnotation != null) {
						ParamValueExtractor<?> paramValueExtractor = parameterExtractorFactory.get(parameter, parameterAnnotations, queryParamAnnotation.value());
						return new QueryParamValueParamProvider(paramValueExtractor);
					}
				}
			}

		}
		return provider;
	}
	
}
