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
package org.o42a.core.value.link;

import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.link.impl.LinkValueStructIR;
import org.o42a.core.value.link.impl.VariableValueStructIR;


public abstract class LinkValueType extends ValueType<LinkValueStruct> {

	public static final LinkValueType LINK = new LinkValueType("link") {

		@Override
		public Obj typeObject(Intrinsics intrinsics) {
			return intrinsics.getLink();
		}

		@Override
		LinkValueStructIR structIR(
				Generator generator,
				LinkValueStruct linkStruct) {
			return new LinkValueStructIR(generator, linkStruct);
		}

	};

	public static final LinkValueType VARIABLE = new LinkValueType("variable") {

		@Override
		public Obj typeObject(Intrinsics intrinsics) {
			return intrinsics.getVariable();
		}

		@Override
		VariableValueStructIR structIR(
				Generator generator,
				LinkValueStruct linkStruct) {
			return new VariableValueStructIR(generator, linkStruct);
		}

	};

	private LinkValueType(String systemId) {
		super(systemId);
	}

	@Override
	public boolean isStateful() {
		return isVariable();
	}

	@Override
	public final boolean isVariable() {
		return is(VARIABLE);
	}

	@Override
	public Path path(Intrinsics intrinsics) {

		final Obj link = typeObject(intrinsics);

		return ROOT_PATH.append(link.getScope().toField().getKey());
	}

	public final LinkValueStruct linkStruct(TypeRef typeRef) {
		return new LinkValueStruct(this, typeRef);
	}

	@Override
	public final LinkValueType toLinkType() {
		return this;
	}

	@Override
	public final ArrayValueType toArrayType() {
		return null;
	}

	abstract ValueStructIR<LinkValueStruct, KnownLink> structIR(
			Generator generator,
			LinkValueStruct linkStruct);

}
