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

import static org.o42a.core.def.LogicalDef.trueLogicalDef;
import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class RefValueDef extends ValueDef {

	static RefValueDef refValueDef(Ref ref) {
		return new SimpleDef(ref);
	}

	private final Ref ref;
	private Ref rescopedRef;

	RefValueDef(
			Obj source,
			Ref ref,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(source, ref, prerequisite, rescoper);
		this.ref = ref;
	}

	RefValueDef(
			RefValueDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
		this.ref = prototype.ref;
	}

	public final Ref getRescopedRef() {
		if (this.rescopedRef != null) {
			return this.rescopedRef;
		}
		return this.rescopedRef = this.ref.rescope(getRescoper());
	}

	@Override
	public ValueType<?> getValueType() {
		return this.ref.getValueType();
	}

	@Override
	public DefKind getKind() {
		return DefKind.PROPOSITION;
	}

	@Override
	public RefValueDef and(Logical logical) {

		final Ref newRef = this.ref.and(logical);

		if (this.ref == newRef) {
			return this;
		}

		return new ConditionalDef(this, newRef);
	}

	public RefOp ref(Code code, CodePos exit, HostOp host) {

		final HostOp rescopedHost = getRescoper().rescope(code, exit, host);

		return this.ref.op(rescopedHost);
	}

	@Override
	public void writeValue(Code code, CodePos exit, HostOp host, ValOp result) {
		ref(code, exit, host).writeValue(code, exit, result);
	}

	@Override
	protected Logical getLogical() {
		return this.ref.getLogical();
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {
		return this.ref.value(getRescoper().rescope(scope));
	}

	@Override
	protected abstract RefValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite);

	static final class VoidDef extends RefValueDef {

		VoidDef(Ref ref, LogicalDef prerequisite) {
			super(
					sourceOf(ref),
					ref,
					prerequisite,
					transparentRescoper(ref.getScope()));
		}

		VoidDef(VoidDef prototype, LogicalDef prerequisite, Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
		}

		@Override
		protected LogicalDef buildPrerequisite() {
			return null;// never called
		}

		@Override
		protected VoidDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				LogicalDef prerequisite) {
			return new VoidDef(this, prerequisite, rescoper);
		}

	}

	private static final class SimpleDef extends RefValueDef {

		SimpleDef(Ref ref) {
			super(
					sourceOf(ref),
					ref,
					trueLogicalDef(ref, ref.getScope()),
					transparentRescoper(ref.getScope()));
		}

		SimpleDef(
				RefValueDef prototype,
				LogicalDef prerequisite,
				Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
		}

		@Override
		protected LogicalDef buildPrerequisite() {
			return trueLogicalDef(this, getScope());
		}

		@Override
		protected SimpleDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				LogicalDef prerequisite) {
			return new SimpleDef(this, prerequisite, rescoper);
		}

	}

	private static class ConditionalDef extends RefValueDef {

		private final RefValueDef def;

		ConditionalDef(RefValueDef def, Ref ref) {
			super(
					def.getSource(),
					ref,
					def.prerequisite(),
					def.getRescoper());
			this.def = def;
		}

		ConditionalDef(
				ConditionalDef prototype,
				LogicalDef prerequisite,
				Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
			this.def = prototype.def;
		}

		@Override
		public DefKind getKind() {
			return this.def.getKind();
		}

		@Override
		protected LogicalDef buildPrerequisite() {
			return this.def.getPrerequisite();
		}

		@Override
		protected ConditionalDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				LogicalDef prerequisite) {
			return new ConditionalDef(this, prerequisite, rescoper);
		}

	}

}
