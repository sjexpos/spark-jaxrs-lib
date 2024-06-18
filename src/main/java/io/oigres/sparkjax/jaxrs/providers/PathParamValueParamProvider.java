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
package io.oigres.sparkjax.jaxrs.providers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedHashMap;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;
import io.oigres.sparkjax.jaxrs.ValueParamProvider;
import spark.Request;
import spark.utils.SparkUtils;

/**
 * @author Sergio Exposito
 */
public class PathParamValueParamProvider implements ValueParamProvider {
	private ParamValueExtractor<?> paramValueExtractor;

	public PathParamValueParamProvider(ParamValueExtractor<?> paramValueExtractor) {
		this.paramValueExtractor = paramValueExtractor;
	}

	private String spark2JaxRsPathParameterName(String parameterName) {
		return parameterName.replace(":", "");
	}

	private String resolveRealPathParameterName(Set<String> pathParameters, String parameterName) {
		// Spark is applying lowerCase when it processes path parameters (only for path parameters) so it's checked if spark parameter matches with reparsed url parameters.
		return pathParameters.stream().filter(p -> p.equalsIgnoreCase(parameterName)).findAny().orElse(parameterName);
	}

	@Override
	public Function<Request, ?> getValueProvider(Object parameter) {
		return request -> {
			MultivaluedHashMap<String, String> values = new MultivaluedHashMap<String, String>();
			Map<String, String> requestParams = request.params();
			// Spark is applying lowerCase when it processes path parameters (only for path parameters) on method request.params(). 
			// So path parameters are parsed again here.
			List<String> matchedList = SparkUtils.convertRouteToList(request.matchedPath());
			Set<String> pathParameters = matchedList.stream().filter(p -> SparkUtils.isParam(p)).collect(Collectors.toSet());
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				String sparkPathParameterName = entry.getKey();
				values.put(spark2JaxRsPathParameterName(resolveRealPathParameterName(pathParameters, sparkPathParameterName)), Arrays.asList( entry.getValue()));
			}
			return this.paramValueExtractor.extract( values );
		};
	}

}
