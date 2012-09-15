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
package org.o42a.core.object.state;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueStruct;


final class KeeperDef extends Def {

	private final KeeperObject keeperObject;
	private Ref object;

	KeeperDef(KeeperObject source) {
		super(source, source, noScopeUpgrade(source.getScope()));
		this.keeperObject = source;
	}

	private KeeperDef(KeeperDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.keeperObject = prototype.keeperObject;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		return this.keeperObject.value()
				.getValueStruct()
				.upgradeScope(getScope());
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.keeperObject.getKeeper().getValue().normalize(
				normalizer.getAnalyzer());
	}

	@Override
	public Eval eval() {
		return new KeeperDefEval(this);
	}

	@Override
	public String toString() {
		if (this.keeperObject == null) {
			return super.toString();
		}
		return "=" + this.keeperObject.getKeeper();
	}

	@Override
	protected Def create(ScopeUpgrade upgrade, ScopeUpgrade additionalUpgrade) {
		return new KeeperDef(this, upgrade);
	}

	@Override
	protected boolean hasConstantValue() {
		return getValue().isConstant();
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return defValue(getValue().value(resolver));
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getValue().resolveAll(resolver);

		final FullResolver keeperResolver =
				object()
				.resolveAll(resolver.setRefUsage(CONTAINER_REF_USAGE))
				.getScope()
				.resolver()
				.fullResolver(resolver, VALUE_REF_USAGE);

		this.keeperObject.getKeeper().getValue().resolveAll(keeperResolver);
	}

	private Ref getValue() {
		return this.keeperObject.getValue();
	}

	private Ref object() {
		if (this.object != null) {
			return this.object;
		}

		final Scope scope = getScope();

		return this.object =
				scope.getEnclosingScopePath()
				.bind(this, scope)
				.target(scope.distribute());
	}

	private static final class KeeperDefEval implements Eval {

		private final KeeperDef def;

		KeeperDefEval(KeeperDef def) {
			this.def = def;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ObjectOp object =
					this.def.object().op(host)
					.target(dirs.dirs())
					.materialize(
							dirs.dirs(),
							tempObjHolder(dirs.getAllocator()));
			final KeeperOp keeper = object.keeper(
					dirs.dirs(),
					this.def.keeperObject.getKeeper());

			dirs.returnValue(keeper.writeValue(dirs.valDirs()));
		}

		@Override
		public String toString() {
			if (this.def == null) {
				return super.toString();
			}
			return this.def.toString();
		}
	}

}
