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

import javax.ws.rs.core.MultivaluedHashMap;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;
import io.oigres.sparkjax.jaxrs.ValueParamProvider;
import spark.Request;

/**
 * @author Sergio Exposito
 */
public class HeaderParamValueParamProvider implements ValueParamProvider {
	private ParamValueExtractor<?> paramValueExtractor;

	public HeaderParamValueParamProvider(ParamValueExtractor<?> paramValueExtractor) {
		this.paramValueExtractor = paramValueExtractor;
	}

	@Override
	public Function<Request, ?> getValueProvider(Object parameter) {
		return request -> {
			MultivaluedHashMap<String, String> values = new MultivaluedHashMap<String, String>();
			for (String headerName : request.headers()) {
				String headerValue = request.headers(headerName);
				// Header name is transform to lower case because HTTP specification says that header comparations are in lower case
				values.put(headerName.toLowerCase(), Arrays.asList( headerValue ));
			}
			return this.paramValueExtractor.extract( values );
		};
	}

}
