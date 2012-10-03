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
package org.o42a.core.value.link.impl;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.value.link.impl.LinkCopy.linkValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.core.value.link.TargetResolver;


public class LinkValueAdapter extends ValueAdapter {

	private final LinkValueStruct expectedStruct;

	public LinkValueAdapter(Ref adaptedRef, LinkValueStruct expectedStruct) {
		super(adaptedRef);
		assert expectedStruct != null :
			"Link value structure not specified";
		this.expectedStruct = expectedStruct;
	}

	public final LinkValueStruct getExpectedStruct() {
		return this.expectedStruct;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public final Ref toTarget() {
		return getAdaptedRef().dereference();
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return getAdaptedRef().valueStruct(scope);
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return linkValue(
				getAdaptedRef(),
				resolver,
				getExpectedStruct().getValueType());
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {

		final Obj object = getAdaptedRef().getResolution().toObject();

		object.value().getDefinitions().resolveTargets(resolver);
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Eval eval() {

		final LinkValueStruct fromStruct =
				getAdaptedRef()
				.valueStruct(getAdaptedRef().getScope())
				.toLinkStruct();

		if (getExpectedStruct().assignableFrom(fromStruct)) {
			return new RefOpEval(getAdaptedRef());
		}

		return new LinkEval(getAdaptedRef(), fromStruct);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getAdaptedRef().resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
	}

	private static final class LinkEval implements Eval {

		private final LinkValueStruct fromStruct;
		private final Ref ref;

		LinkEval(Ref ref, LinkValueStruct fromStruct) {
			this.ref = ref;
			this.fromStruct = fromStruct;
		}

		public final Ref getRef() {
			return this.ref;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs fromDirs = dirs.dirs().nested().value(
					this.fromStruct,
					TEMP_VAL_HOLDER);
			final ValOp from = getRef().op(host).writeValue(fromDirs);

			dirs.returnValue(from);
			fromDirs.done();
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

}
