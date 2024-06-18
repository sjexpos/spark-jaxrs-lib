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
package io.oigres.sparkkjax.examples.awslambda.api;

import io.oigres.sparkjax.ResponseExceptionHandler;
import spark.ResponseTransformer;

import javax.ws.rs.core.MediaType;

/**
 * @author Sergio Exposito
 */
public class GlobalExceptionHandler extends ResponseExceptionHandler {

    public GlobalExceptionHandler(ResponseTransformer responseTransformer) {
        this(responseTransformer, MediaType.APPLICATION_JSON_TYPE, false, false);
    }

    public GlobalExceptionHandler(ResponseTransformer responseTransformer, MediaType mediaType, boolean includeStacktrace, boolean includeException) {
        super(responseTransformer, mediaType, includeStacktrace, includeException);
    }

}
