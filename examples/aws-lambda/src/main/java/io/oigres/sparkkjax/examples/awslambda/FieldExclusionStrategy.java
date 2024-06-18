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
package io.oigres.sparkkjax.examples.awslambda;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import io.swagger.models.AbstractModel;
import io.swagger.models.parameters.AbstractSerializableParameter;

/**
 * @author Sergio Exposito
 */
public class FieldExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return (fieldAttributes.getDeclaringClass() == AbstractSerializableParameter.class ||
                fieldAttributes.getDeclaringClass() == AbstractModel.class
        );
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
