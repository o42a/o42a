/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.def.Definitions.*;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.impl.InlineCondDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Condition;
import org.o42a.core.value.LogicalValue;


public abstract class CondDef extends Def<CondDef> {

	private ValueDef value;

	public CondDef(
			Obj source,
			LocationInfo location,
			ScopeUpgrade scopeUpgrade) {
		super(source, location, DefKind.CONDITION, scopeUpgrade);
	}

	protected CondDef(CondDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
	}

	public final boolean isRequirement() {
		return getKind().isClaim();
	}

	public final boolean isConstant() {
		return getLogical().getConstantValue().isConstant();
	}

	public final Condition getConstantValue() {
		if (hasPrerequisite()) {

			final Logical prerequisite = getPrerequisite();

			if (!prerequisite.isTrue()) {
				if (prerequisite.isFalse()) {
					return Condition.UNKNOWN;
				}
				return Condition.RUNTIME;
			}
		}

		return getLogical().getConstantValue().toCondition();
	}

	@Override
	public final ValueDef toValue() {
		if (this.value != null) {
			return this.value;
		}
		return this.value = new CondValueDef(this);
	}

	@Override
	public final CondDef toCondition() {
		return this;
	}

	@Override
	public boolean impliesWhenAfter(CondDef def) {
		if (!hasPrerequisite()) {
			return false;
		}
		return getPrerequisite().implies(def.getPrerequisite());
	}

	@Override
	public boolean impliesWhenBefore(CondDef def) {
		if (!def.hasPrerequisite()) {
			return false;
		}
		return getPrerequisite().implies(def.getPrerequisite());
	}

	public final Condition condition(Resolver resolver) {
		assertCompatible(resolver.getScope());

		final Resolver rescoped = getScopeUpgrade().rescope(resolver);

		if (hasPrerequisite()) {

			final LogicalValue prerequisite =
					getPrerequisite().logicalValue(rescoped);

			if (!prerequisite.isTrue()) {
				if (prerequisite.isFalse()) {
					return Condition.UNKNOWN;
				}
				return Condition.RUNTIME;
			}
		}

		return getLogical().logicalValue(rescoped).toCondition();
	}

	@Override
	public Definitions toDefinitions() {
		if (isRequirement()) {
			return new Definitions(
					this,
					getScope(),
					null,
					new CondDefs(DefKind.CLAIM, this),
					NO_CONDITIONS,
					NO_CLAIMS,
					NO_PROPOSITIONS);
		}

		return new Definitions(
				this,
				getScope(),
				null,
				NO_REQUIREMENTS,
				new CondDefs(DefKind.CONDITION, this),
				NO_CLAIMS,
				NO_PROPOSITIONS);
	}

	public final void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();
		if (hasPrerequisite()) {
			getPrerequisite().write(dirs.unknownWhenFalse(), host);
		}
		getLogical().write(dirs.falseWhenUnknown(), host);
	}

	@Override
	protected String name() {
		return "CondDef";
	}

	@Override
	protected final void fullyResolve(Resolver resolver) {
		getPrerequisite().resolveAll(resolver);
		getLogical().resolveAll(resolver);
		fullyResolveDef(resolver);
	}

	protected abstract void fullyResolveDef(Resolver resolver);

	protected InlineCond inlineDef(Normalizer normailzer) {
		// TODO In-line CondDef.
		return null;
	}

	final InlineCond inline(Normalizer normalizer) {

		final InlineCond prerequisite;

		if (!hasPrerequisite()) {
			prerequisite = null;
		} else {
			prerequisite = getPrerequisite().inline(normalizer);
			if (prerequisite == null) {
				return null;
			}
		}

		final InlineCond precondition = getPrecondition().inline(normalizer);

		if (precondition == null) {
			return null;
		}

		final InlineCond def = inlineDef(normalizer);

		if (def == null) {
			return null;
		}

		return new InlineCondDef(prerequisite, precondition, def);
	}

}
