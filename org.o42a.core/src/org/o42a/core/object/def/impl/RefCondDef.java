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

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.object.def.CondDef;
import org.o42a.core.ref.*;


public final class RefCondDef extends CondDef {

	private final Ref ref;
	private InlineCond inline;

	public RefCondDef(Ref ref) {
		super(sourceOf(ref), ref, noScopeUpgrade(ref.getScope()));
		this.ref = ref;
	}

	RefCondDef(RefCondDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.ref = prototype.ref;
	}

	@Override
	public void normalize(Normalizer normalizer) {
		this.ref.normalize(normalizer.getAnalyzer());
		this.inline = inline(normalizer);
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return this.ref.getLogical();
	}

	@Override
	protected RefCondDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new RefCondDef(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.ref.resolve(resolver).resolveLogical();
	}

	@Override
	protected InlineCond inlineDef(Normalizer normalizer) {
		return this.ref.inline(normalizer, getScope());
	}

	@Override
	protected void writeDef(CodeDirs dirs, HostOp host) {

		final InlineCond inline = this.inline;

		if (inline != null) {
			inline.writeCond(dirs, host);
			return;
		}

		super.writeDef(dirs, host);
	}

}
