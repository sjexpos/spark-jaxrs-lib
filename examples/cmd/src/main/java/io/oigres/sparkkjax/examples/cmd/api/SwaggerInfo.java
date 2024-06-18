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
package io.oigres.sparkkjax.examples.cmd.api;

import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * @author Sergio Exposito
 */
@SwaggerDefinition(
        info = @Info(
                title = "Sample API",
                description = "",
                version = "1.0-SNAPSHOT"
        ),
        schemes = {
                SwaggerDefinition.Scheme.HTTP
        },
        consumes = { "application/json" },
        produces = { "application/json" })
public interface SwaggerInfo {
}
