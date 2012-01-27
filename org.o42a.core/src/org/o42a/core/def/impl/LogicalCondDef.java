/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.def.impl;

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.def.CondDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.*;


public final class LogicalCondDef extends CondDef {

	private final Common common;

	public LogicalCondDef(Logical logical) {
		super(
				sourceOf(logical),
				logical,
				noScopeUpgrade(logical.getScope()));
		this.common = new Common(logical);
	}

	private LogicalCondDef(
			LogicalCondDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.common = prototype.common;
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.common.logical.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.common.logical.getScope());
	}

	@Override
	protected final Logical buildLogical() {
		return this.common.logical;
	}

	@Override
	protected CondDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new LogicalCondDef(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.common.logical.resolveAll(resolver);
	}

	@Override
	protected InlineCond inlineDef(Normalizer normalizer) {
		return this.common.logical.inline(normalizer, getScope());
	}

	@Override
	protected void normalizeDef(Normalizer normalizer) {
		this.common.inline = inline(normalizer);
	}

	@Override
	protected void writeDef(CodeDirs dirs, HostOp host) {

		final InlineCond inline = this.common.inline;

		if (inline != null) {
			inline.writeCond(dirs, host);
			return;
		}

		super.writeDef(dirs, host);
	}

	private static final class Common {

		private final Logical logical;
		private InlineCond inline;

		Common(Logical logical) {
			this.logical = logical;
		}

		@Override
		public String toString() {
			if (this.logical == null) {
				return "null";
			}
			return this.logical.toString();
		}

	}

}
