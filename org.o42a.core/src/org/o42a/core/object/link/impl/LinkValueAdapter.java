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
package org.o42a.core.object.link.impl;

import static org.o42a.core.object.link.impl.LinkCopy.linkValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.RefEval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;


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
		if (getExpectedStruct().getValueType().isRuntimeConstructed()) {
			return false;
		}
		return getAdaptedRef().isConstant();
	}

	@Override
	public final Ref toTarget() {
		return getAdaptedRef().dereference();
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
	public RefEval eval() {

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
	protected void fullyResolve(Resolver resolver) {
		getAdaptedRef().resolve(resolver).resolveValue();
	}

	private static final class LinkEval extends RefEval {

		private final LinkValueStruct fromStruct;

		LinkEval(Ref ref, LinkValueStruct fromStruct) {
			super(ref);
			this.fromStruct = fromStruct;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			getRef().op(host).writeCond(dirs);
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs fromDirs = dirs.dirs().value(this.fromStruct);
			final ValOp from = getRef().op(host).writeValue(fromDirs);

			fromDirs.done();
			dirs.returnValue(from);
		}

	}

}
