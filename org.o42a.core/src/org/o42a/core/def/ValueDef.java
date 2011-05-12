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
import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;

import org.o42a.codegen.code.Code;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.UserInfo;


public abstract class ValueDef extends Def<ValueDef> {

	private CondDef condition;

	public ValueDef(Obj source, LocationInfo location, Rescoper rescoper) {
		super(source, location, DefKind.PROPOSITION, rescoper);
	}

	protected ValueDef(ValueDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
	}

	public final boolean isClaim() {
		return getKind().isClaim();
	}

	public boolean isLocal() {
		return false;
	}

	public abstract ValueType<?> getValueType();

	@Override
	public boolean impliesWhenAfter(ValueDef def) {
		if (isLocal() || def.isLocal()) {
			return false;
		}
		return getPrerequisite().implies(def.getPrerequisite());
	}

	@Override
	public boolean impliesWhenBefore(ValueDef def) {
		if (isLocal() || def.isLocal()) {
			return false;
		}
		return getPrerequisite().implies(def.getPrerequisite());
	}

	@Override
	public final ValueDef toValue() {
		return this;
	}

	@Override
	public final CondDef toCondition() {
		if (this.condition != null) {
			return this.condition;
		}
		return this.condition = new ValueCondDef(this);
	}

	@Override
	public final DefValue definitionValue(Resolver resolver) {
		assertCompatible(resolver.getScope());

		final Resolver rescoped = getRescoper().rescope(resolver);

		if (hasPrerequisite()) {

			final LogicalValue prerequisite =
				getPrerequisite().logicalValue(rescoped);

			if (!prerequisite.isTrue()) {
				if (!prerequisite.isFalse()) {
					return defValue(this, getValueType().runtimeValue());
				}
				if (getPrerequisite().isFalse()) {
					return alwaysIgnoredValue(this);
				}
				return unknownValue(this);
			}
		}

		final LogicalValue precondition =
			getPrecondition().logicalValue(rescoped);

		if (!precondition.isTrue()) {
			if (!precondition.isFalse()) {
				return defValue(this, getValueType().runtimeValue());
			}
			if (getPrerequisite().isTrue() && getPrecondition().isFalse()) {
				return alwaysMeaningfulValue(this, getValueType().falseValue());
			}
			return defValue(this, getValueType().falseValue());
		}

		final Value<?> value = calculateValue(rescoped);

		if (value == null) {
			return unknownValue(this);
		}
		if (getPrerequisite().isTrue() && getPrecondition().isTrue()) {
			if (value.isUnknown()) {
				return alwaysIgnoredValue(this);
			}
			return alwaysMeaningfulValue(this, value);
		}

		return defValue(this, value);
	}

	@Override
	public final Definitions toDefinitions() {

		final ValueDef[] defs = new ValueDef[] {this};

		if (isClaim()) {
			return new Definitions(
					this,
					getScope(),
					getValueType(),
					NO_CONDITIONS,
					NO_CONDITIONS,
					defs,
					NO_VALUES);
		}

		return new Definitions(
				this,
				getScope(),
				getValueType(),
				NO_CONDITIONS,
				NO_CONDITIONS,
				NO_VALUES,
				defs);
	}

	public final void write(CodeDirs dirs, HostOp host, ValOp result) {

		final HostOp rescopedHost = getRescoper().rescope(dirs, host);

		if (hasPrerequisite()) {
			getPrerequisite().write(dirs.unknownWhenFalse(), rescopedHost);
		}

		final Code preconditionFailed;

		if (getPrecondition().isTrue()) {
			preconditionFailed = null;
		} else {

			final Code code = dirs.code();

			preconditionFailed = code.addBlock("precondition_failed");
			getPrecondition().write(
					falseWhenUnknown(code, preconditionFailed.head()),
					rescopedHost);
		}

		writeDef(dirs, rescopedHost, result);

		if (preconditionFailed != null && preconditionFailed.exists()) {
			result.storeFalse(preconditionFailed);
			dirs.goWhenFalse(preconditionFailed);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("ValueDef[");
		if (hasPrerequisite()) {
			out.append(getPrerequisite()).append("? ");
		}

		final Logical precondition = getPrecondition();

		if (!precondition.isTrue()) {
			out.append(precondition).append(", ");
		}
		out.append('=').append(getLocation());
		if (isClaim()) {
			out.append("!]");
		} else {
			out.append(".]");
		}

		return out.toString();
	}

	protected abstract Value<?> calculateValue(Resolver resolver);

	@Override
	protected final void fullyResolve() {

		final UserInfo user =
			getScope().getContainer().toObject().value();

		getPrerequisite().resolveAll(user);
		getPrecondition().resolveAll(user);
		getRescoper().resolveAll();
		fullyResolveDef(user);
	}

	protected abstract void fullyResolveDef(UserInfo user);

	protected void writeDef(
			CodeDirs dirs,
			HostOp host,
			ValOp result) {
		writeValue(dirs.falseWhenUnknown(), host, result);
	}

	protected abstract void writeValue(
			CodeDirs dirs,
			HostOp host,
			ValOp result);

}
