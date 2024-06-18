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
package io.oigres.sparkjax.jaxrs;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author Sergio Exposito
 */
public class RuntimeDelegateImpl extends RuntimeDelegate {

	public RuntimeDelegateImpl() {
	}

	@Override
	public UriBuilder createUriBuilder() {
		return null;
	}

	@Override
	public ResponseBuilder createResponseBuilder() {
		return null;
	}

	@Override
	public VariantListBuilder createVariantListBuilder() {
		return null;
	}

	@Override
	public <T> T createEndpoint(Application application, Class<T> endpointType) throws IllegalArgumentException, UnsupportedOperationException {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
		if (MediaType.class.equals(type) || MediaType.class.isAssignableFrom(type.getClass())) {
			return (HeaderDelegate<T>)new HeaderDelegate<MediaType>() {
				@Override
				public MediaType fromString(String value) {
					return value != null ? new MediaType(value.substring(0, value.indexOf("/")), value.substring(value.indexOf("/")+1)) : null;
				}
				@Override
				public String toString(MediaType value) {
					return value != null ? value.getType()+"/"+value.getSubtype() : null;
				}
			};
		}
		return null;
	}

	@Override
	public Builder createLinkBuilder() {
		return null;
	}

}
