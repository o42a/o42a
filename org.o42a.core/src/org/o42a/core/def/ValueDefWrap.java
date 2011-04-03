/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ValueDefWrap extends ValueDef {

	private final ValueDef wrapped;

	public ValueDefWrap(
			ValueDef wrapped,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(wrapped.getSource(), wrapped, prerequisite, rescoper);
		this.wrapped = wrapped;
	}

	protected ValueDefWrap(
			ValueDefWrap prototype,
			ValueDef wrapped,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
		this.wrapped = prototype.wrapped;
	}

	@Override
	public DefKind getKind() {
		return this.wrapped.getKind();
	}

	@Override
	public final ValueType<?> getValueType() {
		return this.wrapped.getValueType();
	}

	@Override
	public final ValueDef and(Logical logical) {

		final ValueDef newDef = this.wrapped.and(logical);

		if (newDef == this.wrapped) {
			return this;
		}

		return create(newDef);
	}

	@Override
	public final void writeValue(
			Code code,
			CodePos exit,
			HostOp host,
			ValOp result) {
		this.wrapped.writeValue(code, exit, host, result);
	}

	@Override
	public String toString() {
		return this.wrapped.toString();
	}

	@Override
	protected final LogicalDef buildPrerequisite() {
		return this.wrapped.getPrerequisite();
	}

	@Override
	protected Logical getLogical() {
		return this.wrapped.getLogical();
	}

	@Override
	protected final Value<?> calculateValue(Scope scope) {
		return this.wrapped.calculateValue(scope);
	}

	protected abstract ValueDefWrap create(ValueDef wrapped);

	@Override
	protected final ValueDefWrap create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite) {

		final ValueDef newWrapped = this.wrapped.rescope(additionalRescoper);

		return create(rescoper, additionalRescoper, newWrapped, prerequisite);
	}

	protected abstract ValueDefWrap create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			ValueDef wrapped,
			LogicalDef prerequisite);

}
