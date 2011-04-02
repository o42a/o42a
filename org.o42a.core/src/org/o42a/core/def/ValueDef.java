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

import static org.o42a.core.def.Definitions.NO_VALUES;
import static org.o42a.core.def.LogicalDef.emptyLogicalDef;
import static org.o42a.core.def.LogicalDef.trueLogicalDef;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.RefValueDef.VoidDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Statement;
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

	public ValueDef(
			Obj source,
			Statement statement,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(source, statement, prerequisite, rescoper);
	}

	protected ValueDef(
			ValueDef prototype,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
	}

	@Override
	public final boolean isValue() {
		return true;
	}

	public abstract ValueType<?> getValueType();

	@Override
	public final ValueDef toValue() {
		return this;
	}

	@Override
	public final Definitions toDefinitions() {

		final LogicalDef logicalDef = emptyLogicalDef(this, getScope());
		final ValueDef[] defs = new ValueDef[] {this};

		if (isClaim()) {
			return new Definitions(
					this,
					getScope(),
					getValueType(),
					logicalDef,
					logicalDef,
					defs,
					NO_VALUES);
		}

		return new Definitions(
				this,
				getScope(),
				getValueType(),
				logicalDef,
				logicalDef,
				NO_VALUES,
				defs);
	}

	public abstract void writeValue(
			Code code,
			CodePos exit,
			HostOp host,
			ValOp result);

	@Override
	public abstract ValueDef and(Logical logical);

	@Override
	protected abstract ValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite);

	@Override
	final ValueDef filter(LogicalDef prerequisite, boolean claim) {
		return new FilteredDef(this, prerequisite, claim);
	}

	private static class FilteredDef extends ValueDefWrap {

		private final boolean claim;

		FilteredDef(ValueDef def, LogicalDef prerequisite, boolean claim) {
			super(def, prerequisite, def.getRescoper());
			this.claim = claim;
		}

		private FilteredDef(
				FilteredDef prototype,
				ValueDef wrapped,
				LogicalDef prerequisite,
				Rescoper rescoper) {
			super(prototype, wrapped, prerequisite, rescoper);
			this.claim = prototype.claim;
		}

		@Override
		public boolean isClaim() {
			return this.claim;
		}

		@Override
		public ValueDef claim() {
			if (isClaim()) {
				return this;
			}
			return new FilteredDef(this, prerequisite(), true);
		}

		@Override
		public ValueDef unclaim() {
			if (!isClaim()) {
				return this;
			}
			return new FilteredDef(this, prerequisite(), false);
		}

		@Override
		protected FilteredDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				ValueDef wrapped,
				LogicalDef prerequisite) {
			return new FilteredDef(this, wrapped, prerequisite, rescoper);
		}

		@Override
		protected FilteredDef create(ValueDef wrapped) {
			return new FilteredDef(wrapped, getPrerequisite(), isClaim());
		}

	}

}
