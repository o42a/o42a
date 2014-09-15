/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.core.value;

import static org.o42a.util.fn.Init.init;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.fn.Init;


public abstract class DeferredObjectTypeParameters
		implements ObjectTypeParameters {

	private final LocationInfo location;
	private final Init<ObjectTypeParameters> parameters = init(this::resolve);

	public DeferredObjectTypeParameters(LocationInfo location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return this.location.getLocation();
	}

	@Override
	public TypeParameters<?> refine(
			Obj object,
			TypeParameters<?> defaultParameters) {
		return parameters().refine(object, defaultParameters);
	}

	@Override
	public ObjectTypeParameters prefixWith(PrefixPath prefix) {
		return parameters().prefixWith(prefix);
	}

	@Override
	public String toString() {
		if (this.location == null) {
			return super.toString();
		}
		return this.location.toString();
	}

	protected final ObjectTypeParameters parameters() {
		return this.parameters.get();
	}

	protected abstract ObjectTypeParameters resolve();

}
