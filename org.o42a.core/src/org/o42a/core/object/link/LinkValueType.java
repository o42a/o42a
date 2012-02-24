/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.link;

import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.ValueType;


public abstract class LinkValueType extends ValueType<LinkValueStruct> {

	public static final LinkValueType LINK = new LinkValueType("link") {
		@Override
		public Path path(Intrinsics intrinsics) {

			final Obj link = intrinsics.getLink();

			return ROOT_PATH.append(link.getScope().toField().getKey());
		}
	};

	public static final LinkValueType VARIABLE = new LinkValueType("variable") {

		@Override
		public Path path(Intrinsics intrinsics) {

			final Obj variable = intrinsics.getVariable();

			return ROOT_PATH.append(variable.getScope().toField().getKey());
		}

	};

	private LinkValueType(String systemId) {
		super(systemId);
	}

	@Override
	public final boolean isVariable() {
		return this != LINK;
	}

	public final LinkValueStruct linkStruct(TypeRef typeRef) {
		return new LinkValueStruct(this, typeRef);
	}

	@Override
	public final LinkValueType toLinkType() {
		return this;
	}

}
