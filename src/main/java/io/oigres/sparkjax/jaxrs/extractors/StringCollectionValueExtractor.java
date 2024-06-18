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
package io.oigres.sparkjax.jaxrs.extractors;

import java.util.Collection;

import javax.ws.rs.core.MultivaluedMap;

import io.oigres.sparkjax.jaxrs.ParamValueExtractor;

/**
 * @author Sergio Exposito
 */
public class StringCollectionValueExtractor implements ParamValueExtractor<Collection<String>> {

	public StringCollectionValueExtractor() {
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getDefaultValueString() {
		return null;
	}

	@Override
	public Collection<String> extract(MultivaluedMap<String, String> parameters) {
		return null;
	}

}
