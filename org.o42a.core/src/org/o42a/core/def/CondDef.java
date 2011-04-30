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

import static org.o42a.core.def.DefValue.*;
import static org.o42a.core.def.Definitions.NO_CONDITIONS;
import static org.o42a.core.def.Definitions.NO_VALUES;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.LogicalValue;


public abstract class CondDef extends Def<CondDef> {

	private ValueDef value;

	public CondDef(Obj source, LocationInfo location, Rescoper rescoper) {
		super(source, location, DefKind.CONDITION, rescoper);
	}

	protected CondDef(CondDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
	}

	public final boolean isRequirement() {
		return getKind().isClaim();
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

	public final LogicalValue getConstantValue() {
		return getLogical().getConstantValue();
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

	@Override
	public final DefValue definitionValue(Scope scope) {
		assertCompatible(scope);

		final Scope rescoped = getRescoper().rescope(scope);

		if (!hasPrerequisite() || getPrerequisite().isTrue()) {

			final LogicalValue logicalValue =
				getLogical().logicalValue(rescoped);

			alwaysMeaningfulCondition(this, logicalValue);
		}

		final LogicalValue prerequisite =
			getPrerequisite().logicalValue(rescoped);

		if (!prerequisite.isTrue()) {
			if (!prerequisite.isFalse()) {
				return defCondition(this, LogicalValue.RUNTIME);
			}
			if (getPrerequisite().isFalse()) {
				return alwaysIgnoredValue(this);
			}
			return unknownValue(this);
		}

		return defCondition(this, getLogical().logicalValue(rescoped));
	}

	@Override
	public Definitions toDefinitions() {

		final CondDef[] defs = new CondDef[] {this};

		if (isRequirement()) {
			return new Definitions(
					this,
					getScope(),
					null,
					defs,
					NO_CONDITIONS,
					NO_VALUES,
					NO_VALUES);
		}

		return new Definitions(
				this,
				getScope(),
				null,
				NO_CONDITIONS,
				defs,
				NO_VALUES,
				NO_VALUES);
	}

	public final void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final HostOp rescopedHost = getRescoper().rescope(dirs, host);

		if (hasPrerequisite()) {
			getPrerequisite().write(dirs.unknownWhenFalse(), rescopedHost);
		}
		getLogical().write(dirs.falseWhenUnknown(), rescopedHost);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("CondDef[");
		if (hasPrerequisite()) {
			out.append(getPrerequisite()).append("? ");
		}

		final Logical precondition = getPrecondition();

		if (!precondition.isTrue()) {
			out.append(precondition).append(", ");
		}
		out.append(getLocation());
		if (isRequirement()) {
			out.append("!]");
		} else {
			out.append(".]");
		}

		return out.toString();
	}

	@Override
	protected final void fullyResolve() {
		getPrerequisite().resolveAll();
		getLogical().resolveAll();
		getRescoper().resolveAll();
		fullyResolveDef();
	}

	protected abstract void fullyResolveDef();

}
