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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;
import io.oigres.sparkjax.jaxrs.ParameterExtractorFactory;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;
import io.oigres.sparkjax.jaxrs.ValueParamProvider;
import io.oigres.sparkjax.jaxrs.providers.CookieParamValueParamProvider;
import io.oigres.sparkjax.jaxrs.providers.HeaderParamValueParamProvider;
import io.oigres.sparkjax.jaxrs.providers.PathParamValueParamProvider;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Route;
import spark.route.HttpMethod;

/**
 * @author Sergio Exposito
 */
public abstract class AbstractRoute implements Route {
	private String path;
	private Object resource;
	private Method method;
	private Produces produces;
	private MediaType responseMediaType;
	private ResponseTransformer responseTransformer;
	private ValueParamProvider[] valueParamProviders;
	private Validator validator;

	public AbstractRoute(String path, Object resource, Method method, Consumes defaultConsumes, Produces defaultProduces, ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider, Validator validator) {
		this.path = replacePathParamToSparkFormat(path);
		this.resource = resource;
		this.method = method;
		this.produces = defaultProduces;
		this.responseMediaType = null;
		if (method.getAnnotation(Produces.class) != null) {
			this.produces = method.getAnnotation(Produces.class);
		}
		for (String mt : this.produces.value()) {
			MediaType mediaType = MediaType.valueOf(mt);
			ResponseTransformer rt = responseTransformerProvider.getTransformer(mediaType);
			if (rt != null) {
				this.responseMediaType = mediaType;
				this.responseTransformer = rt;
				break;
			}
		}
		if (this.responseTransformer == null) {
			throw new RuntimeException("Unsupported response mime type:"+this.produces);
		}
		this.valueParamProviders = createValueParameterProviders(method, defaultConsumes, parameterExtractorFactory);
		this.validator = validator;
	}

	/**
	 * Transform a path with JAX-RS parameter to Spark format.
	 * 
	 * eg.
	 *       /path/with/{parameter}/included
	 *       
	 *       /path/with/:parameter/included
	 * 
	 * @param path
	 * @return
	 */
	private String replacePathParamToSparkFormat(String path) {
		while (path.contains("{")) {
			int idxStart = path.indexOf("{");
			int idxEnd = path.indexOf("}", idxStart);
			String parameter = path.substring(idxStart, idxEnd+1);
			path = path.replace(parameter, ":"+parameter.substring(1, parameter.length()-1));
		}
		return path;
	}
	
	private ValueParamProvider[] createValueParameterProviders(Method method, Consumes defaultConsumes, ParameterExtractorFactory parameterExtractorFactory) {
		Consumes consumes = defaultConsumes;
		if (method.getAnnotation(Consumes.class) != null) {
			consumes = method.getAnnotation(Consumes.class);
		}
		Parameter[] parameters = getMethod().getParameters();
		ValueParamProvider[] providers = new ValueParamProvider[parameters.length];
		for (int i=0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			List<Annotation> parameterAnnotations = new LinkedList<Annotation>( Arrays.asList(parameter.getDeclaredAnnotations()));
			parameterAnnotations.add(consumes);
			providers[i] = createValueParamProvider(parameter, parameterAnnotations.toArray(new Annotation[] {}), parameterExtractorFactory);
		}
		return providers;
	}
	
	protected ValueParamProvider createValueParamProvider(Parameter parameter, Annotation[] parameterAnnotations, ParameterExtractorFactory parameterExtractorFactory) {
		List<Annotation> parameterAnnotationsAsList = Arrays.asList(parameterAnnotations);
		
		PathParam pathParamAnnotation = parameterAnnotationsAsList
				.stream()
				.filter(annotation ->  PathParam.class.isAssignableFrom(annotation.getClass()) )
				.map( annotation -> (PathParam)annotation )
				.findAny()
				.orElse(null);
		if (pathParamAnnotation != null) {
			ParamValueExtractor<?> paramValueExtractor = parameterExtractorFactory.get(parameter, parameterAnnotations, pathParamAnnotation.value());
			PathParamValueParamProvider provider = new PathParamValueParamProvider(paramValueExtractor);
			return provider;
		}
		CookieParam cookieParamAnnotation = parameterAnnotationsAsList
				.stream()
				.filter(annotation ->  CookieParam.class.isAssignableFrom(annotation.getClass()) )
				.map( annotation -> (CookieParam)annotation )
				.findAny()
				.orElse(null);
		if (cookieParamAnnotation != null) {
			ParamValueExtractor<?> paramValueExtractor = parameterExtractorFactory.get(parameter, parameterAnnotations, cookieParamAnnotation.value());
			CookieParamValueParamProvider provider = new CookieParamValueParamProvider(paramValueExtractor);
			return provider;
		}
		HeaderParam headerParamAnnotation = parameterAnnotationsAsList
				.stream()
				.filter(annotation ->  HeaderParam.class.isAssignableFrom(annotation.getClass()) )
				.map( annotation -> (HeaderParam)annotation )
				.findAny()
				.orElse(null);
		if (headerParamAnnotation != null) {
			// Header parameter name is transform to lower case because HTTP specification says that header comparations are in lower case
			ParamValueExtractor<?> paramValueExtractor = parameterExtractorFactory.get(parameter, parameterAnnotations, headerParamAnnotation.value().toLowerCase());
			HeaderParamValueParamProvider provider = new HeaderParamValueParamProvider(paramValueExtractor);
			return provider;
		}
		return null;
	}
	
	protected ValueParamProvider[] getValueParamProviders() {
		return this.valueParamProviders;
	}
	
	public Class<?> getResourceClass() {
		return getResource().getClass();
	}
	
	public Object getResource() {
		return this.resource;
	}

	public Method getMethod() {
		return this.method;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public abstract HttpMethod getHttpMethod();

	public MediaType getResponseMediaType() {
		return responseMediaType;
	}

	public ResponseTransformer getResponseTransformer() {
		return responseTransformer;
	}
	
	protected Object[] buildArgumentFromRequest(Request request) {
		ValueParamProvider[] valueParamProviders = getValueParamProviders();
		Object[] arguments = new Object[valueParamProviders.length];
		for (int i=0; i < valueParamProviders.length; i++) {
			ValueParamProvider provider = valueParamProviders[i];
			arguments[i] = provider.getValueProvider(null).apply(request);
		}
		return arguments;
	}
	
	protected void validateArguments(Object[] arguments) {
		ExecutableValidator executableValidator = validator.forExecutables();
		Set<ConstraintViolation<Object>> constraintViolations = executableValidator.validateParameters(this.resource, this.method, arguments);
		if (constraintViolations != null && !constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}
	
	@Override
	public Object handle(Request request, Response response) throws Exception {
		Object[] arguments = buildArgumentFromRequest(request);
		validateArguments(arguments);
		Object result = null;
		try {
			result = getMethod().invoke(getResource(), arguments);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception)e.getCause();
			}
			throw new Exception(e.getCause());
		}
		response.type(this.responseMediaType.toString());
		return result;
	}

}
