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

import static org.o42a.core.def.Definitions.NO_CONDITIONS;
import static org.o42a.core.def.Definitions.NO_VALUES;
import static org.o42a.core.def.LogicalDef.trueLogicalDef;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.RefValueDef.VoidDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ValueDef extends Def<ValueDef> {

	public static ValueDef voidDef(
			LocationInfo location,
			Distributor distributor) {
		return voidDef(
				location,
				distributor,
				logicalTrue(location, distributor.getScope()));
	}

	public static ValueDef voidClaim(
			LocationInfo location,
			Distributor distributor) {
		return voidClaim(
				location,
				distributor,
				logicalTrue(location, distributor.getScope()));
	}

	public static ValueDef voidDef(
			LocationInfo location,
			Distributor distributor,
			Logical prerequisite) {

		final Ref voidRef = voidRef(location, distributor);

		return new VoidDef(
				voidRef,
				prerequisite != null ? prerequisite.toLogicalDef()
				: trueLogicalDef(location, voidRef.getScope()));
	}

	public static ValueDef voidClaim(
			LocationInfo location,
			Distributor distributor,
			Logical prerequisite) {
		return voidDef(location, distributor, prerequisite).claim();
	}

	private CondDef condition;

	public ValueDef(
			Obj source,
			LocationInfo location,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(source, location, prerequisite, rescoper);
	}

	protected ValueDef(
			ValueDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
	}

	public final boolean isClaim() {
		return getKind().isClaim();
	}

	public abstract ValueType<?> getValueType();

	@Override
	public final boolean hasPrerequisite() {
		return true;
	}

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

		final LogicalValue logicalValue = getPrerequisite().logicalValue(scope);

		if (logicalValue.isFalse()) {
			if (getPrerequisite().isFalse()) {
				return DefValue.alwaysIgnoredValue(this);
			}
			return DefValue.unknownValue(this);
		}

		final Value<?> value = calculateValue(getRescoper().rescope(scope));

		if (value == null) {
			return DefValue.unknownValue(this);
		}

		if (getPrerequisite().isTrue()) {
			return DefValue.alwaysMeaningfulValue(this, value);
		}

		return DefValue.value(
				this,
				value.require(logicalValue));
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

	public abstract void writeValue(
			Code code,
			CodePos exit,
			HostOp host,
			ValOp result);

	protected abstract Value<?> calculateValue(Scope scope);

	@Override
	protected abstract ValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite);

	@Override
	final ValueDef filter(LogicalDef prerequisite, boolean hasPrerequisite, boolean claim) {
		return new FilteredValueDef(this, prerequisite, claim);
	}

}
