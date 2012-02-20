/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.object.def.impl;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueStruct;



final class LocalLogical extends Logical {

	private final LocalDef def;

	LocalLogical(LocalDef def) {
		super(def, def.getOwnerScope());
		this.def = def;
	}

	@Override
	public LogicalValue getConstantValue() {
		return LogicalValue.RUNTIME;
	}

	@Override
	public LogicalValue logicalValue(Resolver resolver) {
		assertCompatible(resolver.getScope());

		final Resolver localResolver =
				this.def.localPrefix.rescope(resolver);
		final LocalScope local = localResolver.getScope().toLocal();

		assert local != null :
			"Not a local scope: " + resolver;

		final Action action = this.def.definer.initialLogicalValue(
				local.walkingResolver(resolver));

		return action.getLogicalValue();
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		getLogger().notReproducible(this);
		return null;
	}

	@Override
	public InlineCond inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final CodeDirs subDirs =
				dirs.begin("local_logical", "Local logical: " + this);

		final Obj owner = this.def.getOwnerScope().toObject();
		final ValueStruct<?, ?> valueStruct =
				owner.value().getValueStruct();
		final ValDirs valDirs = subDirs.value(valueStruct, "local_val");

		this.def.writeValue(valDirs, host);
		valDirs.done();

		subDirs.end();
	}

	@Override
	public String toString() {
		return this.def + "?";
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.def.resolveAll(resolver);
	}

}