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
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


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

	public abstract ValueType<?> getValueType();

	@Override
	public final ValueDef toValue() {
		return this;
	}

	@Override
	public CondDef toCondition() {
		if (this.condition != null) {
			return this.condition;
		}
		return this.condition = new ValueCondDef(this);
	}

	@Override
	public final DefValue definitionValue(Scope scope) {
		assertCompatible(scope);

		final Scope rescoped = getRescoper().rescope(scope);
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

	public final void writePrerequisite(CodeDirs dirs, HostOp host) {
		host = getRescoper().rescope(dirs, host);
		getPrerequisite().write(dirs, host);
	}

	public abstract void writeValue(CodeDirs dirs, HostOp host, ValOp result);

	protected abstract Value<?> calculateValue(Scope scope);

}
