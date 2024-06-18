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
import java.util.function.Function;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;
import io.oigres.sparkjax.jaxrs.ValueParamProvider;
import spark.Request;

/**
 * @author Sergio Exposito
 */
public class FormParamValueParamProvider implements ValueParamProvider {
	private ParamValueExtractor<?> paramValueExtractor;

	public FormParamValueParamProvider(ParamValueExtractor<?> paramValueExtractor) {
		super();
		this.paramValueExtractor = paramValueExtractor;
	}

	@Override
	public Function<Request, ?> getValueProvider(Object param) {
		return request -> {
			String contentType = request.contentType();
			MediaType mediaType = new MediaType(contentType.substring(0, contentType.indexOf("/")), contentType.substring(contentType.indexOf("/")+1));
			if (mediaType == null || mediaType.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
				throw new RuntimeException("Unsupported body media type: "+contentType);
			}
			MultivaluedHashMap<String, String> values = new MultivaluedHashMap<String, String>();
			String body = request.body();
			String[] parameters = body.split("&");
			for (String parameter : parameters) {
				String[] pair = parameter.split("=");
				if (pair.length != 2) {
					throw new RuntimeException();
				}
				String parameterName = pair[0];
				String parameterValue = pair[1];
				values.put(parameterName, Arrays.asList(parameterValue));
			}
			return this.paramValueExtractor.extract( values );
		};
	}

}
