/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.operator;

import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


final class ValueObject extends Obj {

	private final ValueOf valueOf;

	ValueObject(ValueOf valueOf) {
		super(valueOf, valueOf.distributeIn(valueOf.getContainer()));
		this.valueOf = valueOf;
		setValueType(operand().getValueType());
	}

	@Override
	public String toString() {
		if (this.valueOf == null) {
			return super.toString();
		}
		return this.valueOf.toString();
	}

	@Override
	protected Nesting createNesting() {
		return this.valueOf.getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {

		final TypeRef ancestor = this.valueOf.getValueTypeInterface();

		return new Ascendants(this)
		.setAncestor(ancestor)
		.setParameters(
				ancestor.copyParameters()
				.rescope(getScope())
				.toObjectTypeParameters());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return operand().toDefinitions(definitionEnv());
	}

	private Ref operand() {
		return this.valueOf.operand().rescope(getScope());
	}

}
