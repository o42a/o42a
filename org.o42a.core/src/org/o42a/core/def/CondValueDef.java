/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.def;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class CondValueDef extends ValueDef {

	private final CondDef def;

	CondValueDef(CondDef def) {
		super(def.getSource(), def, def.getPrerequisite(), def.getRescoper());
		this.def = def;
	}

	private CondValueDef(
			CondValueDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
		this.def = prototype.def;
	}

	@Override
	public DefKind getKind() {
		if (this.def.isRequirement()) {
			return DefKind.CLAIM;
		}
		return DefKind.PROPOSITION;
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public void writeValue(
			Code code,
			CodePos exit,
			HostOp host,
			ValOp result) {

		final CodeBlk condFalse = code.addBlock("false");

		this.def.writeLogicalValue(code, condFalse.head(), host);
		result.storeVoid(code);

		if (condFalse.exists()) {
			result.storeFalse(condFalse);
			condFalse.go(exit);
		}
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {
		return this.def.getLogical().logicalValue(
				getRescoper().rescope(scope)).toValue();
	}

	@Override
	protected ValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite) {
		return new CondValueDef(this, prerequisite, rescoper);
	}

	@Override
	public ValueDef and(Logical logical) {
		return this.def.and(logical).toValue();
	}

	@Override
	protected LogicalDef buildPrerequisite() {
		return this.def.getPrerequisite();
	}

	@Override
	protected Logical getLogical() {
		return this.def.getLogical();
	}

}
