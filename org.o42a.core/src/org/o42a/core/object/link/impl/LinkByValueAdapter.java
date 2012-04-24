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

import static org.o42a.core.object.link.impl.TargetLink.linkByValue;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.RefEval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;


public class LinkByValueAdapter extends ValueAdapter {

	private final LinkValueStruct expectedStruct;

	public LinkByValueAdapter(Ref adaptedRef, LinkValueStruct expectedStruct) {
		super(adaptedRef);
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
	public Ref toTarget() {
		return getAdaptedRef();
	}

	@Override
	public Value<?> value(Resolver resolver) {

		final Value<?> value =
				linkByValue(getAdaptedRef(), getExpectedStruct());

		return value.prefixWith(
				upgradePrefix(getAdaptedRef(), resolver.getScope()));
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
		resolver.resolveTarget(getAdaptedRef().getResolution().toObject());
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public RefEval eval() {
		return new LinkByValueEval(getAdaptedRef());
	}

	@Override
	public String toString() {
		return "`" + super.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		getAdaptedRef().resolve(resolver).resolveTarget();
	}

	private static final class LinkByValueEval extends RefEval {

		LinkByValueEval(Ref ref) {
			super(ref);
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			getRef().op(host).writeCond(dirs);
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Block code = dirs.code();
			final ObjectOp target =
					getRef().op(host)
					.target(dirs.dirs())
					.materialize(dirs.dirs());

			dirs.returnValue(dirs.value().store(code, target.toAny(code)));
		}

	}

}
