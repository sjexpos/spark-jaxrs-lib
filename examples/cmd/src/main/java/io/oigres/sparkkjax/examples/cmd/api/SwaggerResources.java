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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.oigres.sparkkjax.examples.cmd.SwaggerParser;
import io.swagger.annotations.ApiOperation;

/**
 * @author Sergio Exposito
 */
@Path("/v2/api-docs")
public class SwaggerResources {
    private final Package resourcesPackage;

    public SwaggerResources() {
        this.resourcesPackage = SwaggerInfo.class.getPackage();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "The swagger definition in either JSON or YAML", hidden = true)
    public String getListing() throws JsonProcessingException {
        return SwaggerParser.getSwaggerJson(this.resourcesPackage.getName());
    }
}
