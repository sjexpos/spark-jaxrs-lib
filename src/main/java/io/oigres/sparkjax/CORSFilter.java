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

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * @author Sergio Exposito
 */
public class CORSFilter implements Filter {
	private static final String HEADER_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	private static final String HEADER_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	private static final String HEADER_REQUEST_METHODS = "Access-Control-Request-Methods";
	private static final String WILDCARD_VALUE = "*";

	@Override
	public void handle(Request request, Response response) throws Exception {
		response.header(HEADER_ALLOW_ORIGIN, WILDCARD_VALUE);
		response.header(HEADER_REQUEST_METHODS, WILDCARD_VALUE);
		response.header(HEADER_ALLOW_HEADERS, WILDCARD_VALUE);
	}

}
