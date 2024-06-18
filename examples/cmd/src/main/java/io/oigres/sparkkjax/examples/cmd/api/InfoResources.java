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

import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.oigres.sparkkjax.examples.cmd.api.model.InfoResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Sergio Exposito
 */
@Api( tags = "Info", description = " ")
@Path("/api/info")
public class InfoResources {

    @ApiOperation(value = "Retrieve app information", notes = " ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = InfoResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public InfoResponse getDefaultInfo()  {
        return new InfoResponse("Hello World! ") ;
    }


    @ApiOperation(value = "Retrieve app information", notes = " ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = InfoResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public InfoResponse getInfoWithParameter(
            @ApiParam(value = "Application identifier", required=true)
            @Min(1)
            @PathParam("id") Long id
    )  {
        return new InfoResponse("Hello World! " + id) ;
    }

}
