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

import static org.o42a.core.def.CondDef.trueCondDef;
import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Cond;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class RefDef extends Def {

	static RefDef refDef(Ref ref) {
		return new SimpleDef(ref);
	}

	private Ref rescopedRef;

	RefDef(
			Obj source,
			Ref ref,
			CondDef prerequisite,
			Rescoper rescoper) {
		super(source, ref, prerequisite, rescoper);
	}

	RefDef(RefDef prototype, CondDef prerequisite, Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
	}

	public final Ref getRef() {
		return (Ref) getScoped();
	}

	public final Ref getRescopedRef() {
		if (this.rescopedRef != null) {
			return this.rescopedRef;
		}
		return this.rescopedRef = getRef().rescope(getRescoper());
	}

	@Override
	public ValueType<?> getValueType() {
		return getRef().getValueType();
	}

	@Override
	public boolean isClaim() {
		return false;
	}

	@Override
	public Def and(Cond condition) {

		final Ref ref = getRef();
		final Ref newRef = ref.and(condition);

		if (ref == newRef) {
			return this;
		}

		return new ConditionalDef(this, newRef);
	}

	public RefOp ref(Code code, CodePos exit, HostOp host) {

		final HostOp rescopedHost = getRescoper().rescope(code, exit, host);

		return getRef().op(rescopedHost);
	}

	@Override
	public void writeValue(Code code, CodePos exit, HostOp host, ValOp result) {
		ref(code, exit, host).writeValue(code, exit, result);
	}

	@Override
	protected Cond condition() {
		return getRescopedRef().getCondition();
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {
		return getRef().value(scope);
	}

	@Override
	protected abstract RefDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			CondDef prerequisite);

	static final class VoidDef extends RefDef {

		VoidDef(Ref ref, CondDef prerequisite) {
			super(
					sourceOf(ref),
					ref,
					prerequisite,
					transparentRescoper(ref.getScope()));
		}

		VoidDef(VoidDef prototype, CondDef prerequisite, Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
		}

		@Override
		public boolean isClaim() {
			return false;
		}

		@Override
		protected CondDef buildPrerequisite() {
			return null;// never called
		}

		@Override
		protected VoidDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				CondDef prerequisite) {
			return new VoidDef(this, prerequisite, rescoper);
		}

	}

	private static final class SimpleDef extends RefDef {

		SimpleDef(Ref ref) {
			super(
					sourceOf(ref),
					ref,
					trueCondDef(ref, ref.getScope()),
					transparentRescoper(ref.getScope()));
		}

		SimpleDef(RefDef prototype, CondDef prerequisite, Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
		}

		@Override
		protected CondDef buildPrerequisite() {
			return trueCondDef(this, getScope());
		}

		@Override
		protected SimpleDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				CondDef prerequisite) {
			return new SimpleDef(this, prerequisite, rescoper);
		}

	}

	private static class ConditionalDef extends RefDef {

		private final RefDef def;

		ConditionalDef(RefDef def, Ref ref) {
			super(
					def.getSource(),
					ref,
					def.prerequisite(),
					def.getRescoper());
			this.def = def;
		}

		ConditionalDef(
				ConditionalDef prototype,
				CondDef prerequisite,
				Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
			this.def = prototype.def;
		}

		@Override
		public boolean isClaim() {
			return this.def.isClaim();
		}

		@Override
		protected CondDef buildPrerequisite() {
			return this.def.getPrerequisite();
		}

		@Override
		protected ConditionalDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				CondDef prerequisite) {
			return new ConditionalDef(this, prerequisite, rescoper);
		}

	}

}
