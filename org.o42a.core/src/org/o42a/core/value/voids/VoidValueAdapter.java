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
package org.o42a.core.value.voids;

import static org.o42a.core.ref.RefUsage.CONDITION_REF_USAGE;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;


public class VoidValueAdapter extends ValueAdapter {

	public VoidValueAdapter(Ref adaptedRef) {
		super(adaptedRef);
	}

	@Override
	public boolean isConstant() {
		return getAdaptedRef().isConstant();
	}

	@Override
	public Ref toTarget() {
		return null;
	}

	@Override
	public Value<?> value(Resolver resolver) {

		final Value<?> value = getAdaptedRef().value(resolver);

		return value.getKnowledge().getCondition().toValue(ValueStruct.VOID);
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {

		final InlineValue value = getAdaptedRef().inline(normalizer, origin);

		if (value == null) {
			return null;
		}

		return new InlineToVoid(value);
	}

	@Override
	public Eval eval() {
		return new EvalToVoid(getAdaptedRef());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getAdaptedRef().resolveAll(resolver.setRefUsage(CONDITION_REF_USAGE));
	}

	private static final class InlineToVoid extends InlineValue {

		private final InlineValue value;

		InlineToVoid(InlineValue value) {
			super(null);
			this.value = value;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.value.writeCond(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			this.value.writeCond(dirs.dirs(), host);
			return voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return "(void) " + this.value;
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class EvalToVoid implements Eval {

		private final Ref ref;

		EvalToVoid(Ref ref) {
			this.ref = ref;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.ref.op(host).writeCond(dirs.dirs());
			dirs.returnValue(voidValue().op(dirs.getBuilder(), dirs.code()));
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return "(void) " + this.ref;
		}

	}

}
