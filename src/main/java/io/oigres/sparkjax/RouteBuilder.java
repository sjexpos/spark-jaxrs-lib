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
package io.oigres.sparkjax;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ParamConverterProvider;

import com.google.gson.Gson;

import io.oigres.sparkjax.jaxrs.ParamConverterFactory;
import io.oigres.sparkjax.jaxrs.ParameterExtractorFactory;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;
import io.oigres.sparkjax.jaxrs.converters.BeanParamConverterProvider;
import io.oigres.sparkjax.jaxrs.converters.CharacterParamConverterProvider;
import io.oigres.sparkjax.jaxrs.converters.DateParamConverterProvider;
import io.oigres.sparkjax.jaxrs.converters.StringParamConverterProvider;
import io.oigres.sparkjax.jaxrs.converters.TypeFromStringEnumParamConverterProvider;
import io.oigres.sparkjax.jaxrs.converters.TypeFromStringParamConverterProvider;
import io.oigres.sparkjax.jaxrs.converters.TypeValueOfParamConverterProvider;
import io.oigres.sparkjax.spark.AbstractRoute;
import io.oigres.sparkjax.spark.DeleteRoute;
import io.oigres.sparkjax.spark.GetRoute;
import io.oigres.sparkjax.spark.OptionsRoute;
import io.oigres.sparkjax.spark.PatchRoute;
import io.oigres.sparkjax.spark.PostRoute;
import io.oigres.sparkjax.spark.PutRoute;

import org.apache.bval.jsr.ApacheValidationProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.route.HttpMethod;

/**
 * @author Sergio Exposito
 */
public class RouteBuilder {
	private static final Logger log = LoggerFactory.getLogger(RouteBuilder.class);
	private ParameterExtractorFactory parameterExtractorFactory;
	private ParamConverterProvider paramConverterProvider;
	private ResponseTransformerProvider responseTransformerProvider;
	private MediaType defaultConsumes = MediaType.APPLICATION_JSON_TYPE;
	private MediaType defaultProduces = MediaType.APPLICATION_JSON_TYPE;
	private ValidatorFactory validatorFactory;

	public RouteBuilder(Gson requestObjectMapper, ResponseTransformerProvider responseTransformerProvider) {
		this.paramConverterProvider = new ParamConverterFactory(
				Arrays.asList(
						new DateParamConverterProvider(),
						new TypeFromStringEnumParamConverterProvider(),
						new TypeValueOfParamConverterProvider(),
						new CharacterParamConverterProvider(),
						new TypeFromStringParamConverterProvider(),
						new StringParamConverterProvider(),
						new BeanParamConverterProvider(requestObjectMapper)
					)
				);
		this.parameterExtractorFactory = new ParameterExtractorFactory(this.paramConverterProvider);
		this.responseTransformerProvider = responseTransformerProvider;
		this.validatorFactory = Validation.byProvider(ApacheValidationProvider.class)
				.configure()
				.ignoreXmlConfiguration()
				.buildValidatorFactory();
	}

	public void setupRoutes(Set<Object> resources) {
		setupRoutes(resources, false);
	}

	public void setupRoutes(Set<Object> resources, boolean addDefaultOptionsMethod) {
		List<AbstractRoute> routes = resources.stream()
			.map( resource -> registerEndpoints(resource, this.parameterExtractorFactory, this.responseTransformerProvider) )
			.flatMap( endpoints -> endpoints.stream() )
			.collect(Collectors.toList());
		if (addDefaultOptionsMethod) {
			registerDefaultOptions(routes);
		}
	}
	
	private Consumes getDefaultConsume() {
		return (Consumes)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Consumes.class}, new InvocationHandler() {
			@Override 
			public Object invoke(Object proxy, Method method, Object[] args) {
				if (method == null)
					return null;
				if ("value".equals(method.getName())) {
					return new String[] {defaultConsumes.toString()};
				}
				if ("getClass".equals(method.getName())) {
					return Consumes.class;
				}
				if ("toString".equals(method.getName())) {
					return String.format("@javax.ws.rs.Consumes(value={\"%s\"})", defaultConsumes.toString());
				}
				return null;
			}
		});
	}
	
	private Produces getDefaultProduces() {
		return (Produces)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Produces.class}, new InvocationHandler() {
			@Override 
			public Object invoke(Object proxy, Method method, Object[] args) {
				if (method == null)
					return null;
				if ("value".equals(method.getName())) {
					return new String[] {defaultProduces.toString()};
				}
				if ("getClass".equals(method.getName())) {
					return Produces.class;
				}
				if ("toString".equals(method.getName())) {
					return String.format("@javax.ws.rs.Produces(value={\"%s\"})", defaultProduces.toString());
				}
				return null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
    private List<AbstractRoute> registerEndpoints(Object resource, ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider) {
		String pathPrefix = ReflectionUtils.getAllAnnotations(resource.getClass(), a -> Path.class.isAssignableFrom(a.getClass()))
    			.stream()
    			.findFirst()
    			.map( a -> ((Path)a).value())
    			.orElse("");
		Consumes consumesAnnotationsOnResource = (Consumes)ReflectionUtils.getAllAnnotations(resource.getClass(), a -> Consumes.class.isAssignableFrom(a.getClass()))
				.stream()
				.findFirst()
				.orElse(getDefaultConsume());
		Produces producesAnnotationOnResource = (Produces)ReflectionUtils.getAllAnnotations(resource.getClass(), a -> Produces.class.isAssignableFrom(a.getClass()))
				.stream()
				.findFirst()
				.orElse(getDefaultProduces());
    	Set<Method> methodsWithPathAnnotation = ReflectionUtils.getAllMethods(resource.getClass(), ReflectionUtils.withAnnotation(Path.class));
    	Set<Method> methodsWithGetAnnotation = ReflectionUtils.getAllMethods(resource.getClass(), ReflectionUtils.withAnnotation(GET.class));
    	Set<Method> methodsWithPostAnnotation = ReflectionUtils.getAllMethods(resource.getClass(), ReflectionUtils.withAnnotation(POST.class));
    	Set<Method> methodsWithPutAnnotation = ReflectionUtils.getAllMethods(resource.getClass(), ReflectionUtils.withAnnotation(PUT.class));
    	Set<Method> methodsWithOptionsAnnotation = ReflectionUtils.getAllMethods(resource.getClass(), ReflectionUtils.withAnnotation(OPTIONS.class));
    	Set<Method> methodsWithDeleteAnnotation = ReflectionUtils.getAllMethods(resource.getClass(), ReflectionUtils.withAnnotation(DELETE.class));
		Set<Method> methodsWithPatchAnnotation = ReflectionUtils.getAllMethods(resource.getClass(), ReflectionUtils.withAnnotation(PATCH.class));
    	
    	List<AbstractRoute> routes = new LinkedList<AbstractRoute>();
    	routes.addAll(processGets(pathPrefix, resource, consumesAnnotationsOnResource, producesAnnotationOnResource, methodsWithGetAnnotation, methodsWithPathAnnotation, parameterExtractorFactory, responseTransformerProvider));
    	routes.addAll(processPosts(pathPrefix, resource, consumesAnnotationsOnResource, producesAnnotationOnResource, methodsWithPostAnnotation, methodsWithPathAnnotation, parameterExtractorFactory, responseTransformerProvider));
    	routes.addAll(processPuts(pathPrefix, resource, consumesAnnotationsOnResource, producesAnnotationOnResource, methodsWithPutAnnotation, methodsWithPathAnnotation, parameterExtractorFactory, responseTransformerProvider));
    	routes.addAll(processOptions(pathPrefix, resource, consumesAnnotationsOnResource, producesAnnotationOnResource, methodsWithOptionsAnnotation, methodsWithPathAnnotation, parameterExtractorFactory, responseTransformerProvider));
    	routes.addAll(processDelete(pathPrefix, resource, consumesAnnotationsOnResource, producesAnnotationOnResource, methodsWithDeleteAnnotation, methodsWithPathAnnotation, parameterExtractorFactory, responseTransformerProvider));
		routes.addAll(processPatch(pathPrefix, resource, consumesAnnotationsOnResource, producesAnnotationOnResource, methodsWithPatchAnnotation, methodsWithPathAnnotation, parameterExtractorFactory, responseTransformerProvider));
    	return routes;
    }
    
    private List<AbstractRoute> processGets(String pathPrefix, Object resource, Consumes defautConsumes, Produces defaultProduces,
    		Set<Method> methodsWithGetAnnotation, Set<Method> methodsWithPathAnnotation, 
    		ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider) {
		List<Method> getMethodServices = new ArrayList<>(methodsWithGetAnnotation);
		Collections.sort(getMethodServices, new MethodEndpointComparator(methodsWithPathAnnotation));
    	List<AbstractRoute> routes = getMethodServices.stream().map( method -> {
    		String path = pathPrefix;
    		if (methodsWithPathAnnotation.contains(method)) {
    			Path p = method.getAnnotation(Path.class);
    			path += p.value();
    		}
    		Validator validator = this.validatorFactory.getValidator();
    		return new GetRoute(path, resource, method, defautConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
    	}).collect(Collectors.toList());
    	return routes;
    }

    private List<AbstractRoute> processPosts(String pathPrefix, Object resource, Consumes defautConsumes, Produces defaultProduces,
    		Set<Method> methodsWithPostAnnotation, Set<Method> methodsWithPathAnnotation, 
    		ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider) {
		List<Method> postMethodServices = new ArrayList<>(methodsWithPostAnnotation);
		Collections.sort(postMethodServices, new MethodEndpointComparator(methodsWithPathAnnotation));
		List<AbstractRoute> routes = postMethodServices.stream().map( method -> {
    		String path = pathPrefix;
    		if (methodsWithPathAnnotation.contains(method)) {
    			Path p = method.getAnnotation(Path.class);
    			path += p.value();
    		}
    		Validator validator = this.validatorFactory.getValidator();
    		return new PostRoute(path, resource, method, defautConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
    	}).collect(Collectors.toList());
    	return routes;
    }
    
    private List<AbstractRoute> processPuts(String pathPrefix, Object resource, Consumes defautConsumes, Produces defaultProduces,
    		Set<Method> methodsWithPutAnnotation, Set<Method> methodsWithPathAnnotation, 
    		ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider) {
		List<Method> putMethodServices = new ArrayList<>(methodsWithPutAnnotation);
		Collections.sort(putMethodServices, new MethodEndpointComparator(methodsWithPathAnnotation));
    	List<AbstractRoute> routes = putMethodServices.stream().map( method -> {
    		String path = pathPrefix;
    		if (methodsWithPathAnnotation.contains(method)) {
    			Path p = method.getAnnotation(Path.class);
    			path += p.value();
    		}
    		Validator validator = this.validatorFactory.getValidator();
    		return new PutRoute(path, resource, method, defautConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
    	}).collect(Collectors.toList());
    	return routes;
    }

	private List<AbstractRoute> processPatch(String pathPrefix, Object resource, Consumes consumesAnnotationsOnResource, Produces producesAnnotationOnResource,
									 Set<Method> methodsWithPatchAnnotation, Set<Method> methodsWithPathAnnotation,
									 ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider) {
		List<Method> patchMethodServices = new ArrayList<>(methodsWithPatchAnnotation);
		Collections.sort(patchMethodServices, new MethodEndpointComparator(methodsWithPathAnnotation));
		List<AbstractRoute> routes = patchMethodServices.stream().map( method -> {
			String path = pathPrefix;
			if (methodsWithPathAnnotation.contains(method)) {
				Path p = method.getAnnotation(Path.class);
				path += p.value();
			}
			Validator validator = this.validatorFactory.getValidator();
			return new PatchRoute(path, resource, method, consumesAnnotationsOnResource, producesAnnotationOnResource, parameterExtractorFactory, responseTransformerProvider, validator);
		}).collect(Collectors.toList());
		return routes;

	}

	private List<AbstractRoute> processOptions(String pathPrefix, Object resource, Consumes defautConsumes, Produces defaultProduces,
    		Set<Method> methodsWithOptionsAnnotation, Set<Method> methodsWithPathAnnotation, 
    		ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider) {
		List<Method> optionsMethodServices = new ArrayList<>(methodsWithOptionsAnnotation);
		Collections.sort(optionsMethodServices, new MethodEndpointComparator(methodsWithPathAnnotation));
    	List<AbstractRoute> routes = optionsMethodServices.stream().map( method -> {
    		String path = pathPrefix;
    		if (methodsWithPathAnnotation.contains(method)) {
    			Path p = method.getAnnotation(Path.class);
    			path += p.value();
    		}
    		Validator validator = this.validatorFactory.getValidator();
    		return new OptionsRoute(path, resource, method, defautConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
    	}).collect(Collectors.toList());
    	return routes;
    }
    
    private List<AbstractRoute> processDelete(String pathPrefix, Object resource, Consumes defautConsumes, Produces defaultProduces,
    		Set<Method> methodsWithDeleteAnnotation, Set<Method> methodsWithPathAnnotation, 
    		ParameterExtractorFactory parameterExtractorFactory, ResponseTransformerProvider responseTransformerProvider) {
		List<Method> deleteMethodServices = new ArrayList<>(methodsWithDeleteAnnotation);
		Collections.sort(deleteMethodServices, new MethodEndpointComparator(methodsWithPathAnnotation));
    	List<AbstractRoute> routes = deleteMethodServices.stream().map( method -> {
    		String path = pathPrefix;
    		if (methodsWithPathAnnotation.contains(method)) {
    			Path p = method.getAnnotation(Path.class);
    			path += p.value();
    		}
    		Validator validator = this.validatorFactory.getValidator();
    		return new DeleteRoute(path, resource, method, defautConsumes, defaultProduces, parameterExtractorFactory, responseTransformerProvider, validator);
    	}).collect(Collectors.toList());
    	return routes;
    }
    
	private void registerDefaultOptions(List<AbstractRoute> routes) {
		Set<String> allPaths = routes.stream()
						.map(AbstractRoute::getPath)
						.collect(Collectors.toSet());
		Set<String> optionPaths = routes.stream()
						.filter(r -> (r instanceof OptionsRoute))
						.map(AbstractRoute::getPath)
						.collect(Collectors.toSet());
		Set<String> missingOptionsPaths = allPaths.stream()
						.filter( path -> !optionPaths.contains(path) )
						.collect(Collectors.toSet());
		missingOptionsPaths.stream()
			.filter(Objects::nonNull)
			.forEach(p -> {
				Set<HttpMethod> methods = routes.stream()
						.filter(r -> r.getPath().equals(p))
						.map(AbstractRoute::getHttpMethod)
						.collect(Collectors.toSet());
				Spark.options(p, new DefaultOptionsRoute(p, methods));
			});
	}

	public static class DefaultOptionsRoute implements Route {
		private String path;
		private Set<HttpMethod> allowMethods;

		public DefaultOptionsRoute(String path, Set<HttpMethod> allowMethods) {
			this.path = path;
			this.allowMethods = Set.copyOf(allowMethods);
		}

		@Override
		public Object handle(Request request, Response response) throws Exception {
			log.debug("Default implementation for OPTIONS {}", this.path);
			response.header(HttpHeaders.ALLOW, 
							String.join(",", 
										this.allowMethods.stream()
											.map( m -> m.name().toUpperCase())
											.collect(Collectors.toList())
										)
							);
			response.header("Accept-Patch", "");
			response.body("");
			response.status(HttpStatus.OK_200);
			return null;
		}

	}

	static class MethodEndpointComparator implements Comparator<Method> {
		private final Set<Method> methodsWithPathAnnotation;
		private final PathComparator pathComparator = new PathComparator();
	
		public MethodEndpointComparator(Set<Method> methodsWithPathAnnotation) {
			this.methodsWithPathAnnotation = methodsWithPathAnnotation;
		}
	
		@Override
		public int compare(Method method1, Method method2) {
			String pathMethod1 = "";
			if (this.methodsWithPathAnnotation.contains(method1)) {
				Path p = method1.getAnnotation(Path.class);
    			pathMethod1 += p.value();
			}
			String pathMethod2 = "";
			if (this.methodsWithPathAnnotation.contains(method2)) {
				Path p = method2.getAnnotation(Path.class);
    			pathMethod2 += p.value();
			}
			String[] splittedPathMethod1 = pathMethod1.split("./");
			String[] splittedPathMethod2 = pathMethod2.split("./");
			return this.pathComparator.compare(splittedPathMethod1, splittedPathMethod2);
		}
		
	}

	static class PathComparator implements Comparator<String[]> {

		@Override
		public int compare(String[] path1, String[] path2) {
			for (int i=0; i < Math.max(path1.length, path2.length); i++) {
				if ( i >= path1.length ) {
					return -1;
				}
				if ( i >= path2.length ) {
					return 1;
				}
				if (path1[i].equalsIgnoreCase(path2[i])) {
					continue;
				}
				if (path1[i].startsWith("{") && path1[i].endsWith("}")) {
					return 1;
				}
				if (path2[i].startsWith("{") && path2[i].endsWith("}")) {
					return -1;
				}
				return path1[i].compareTo(path2[i]);
			}
			return 0;
		}

	}

	static public void main(String[] args) {
		List<String> paths = Arrays.asList(
			"/v1/brands/{brandId}",
			"/v1/brands",
			"/v1/brands/with-product-quantity",
			"/v1/brands/basic"
		);
		Collections.sort(paths, (p1, p2) -> new PathComparator().compare(p1.split("./"), p2.split("./")));
		System.out.println(paths);
	}

}
