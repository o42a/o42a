/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.def.Definitions.NO_DEFS;
import static org.o42a.core.def.LogicalDef.emptyLogicalDef;
import static org.o42a.core.def.LogicalDef.trueLogicalDef;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.RefDef.VoidDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.St;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.Loggable;


public abstract class Def extends RescopableStatement implements SourceSpec {

	public static Def voidDef(LocationInfo location, Distributor distributor) {
		return voidDef(
				location,
				distributor,
				logicalTrue(location, distributor.getScope()));
	}

	public static Def voidClaim(
			LocationInfo location,
			Distributor distributor) {
		return voidClaim(
				location,
				distributor,
				logicalTrue(location, distributor.getScope()));
	}

	public static Def voidDef(
			LocationInfo location,
			Distributor distributor,
			Logical prerequisite) {

		final Ref voidRef = voidRef(location, distributor);

		return new VoidDef(
				voidRef,
				prerequisite != null ? prerequisite.toLogicalDef()
				: trueLogicalDef(location, voidRef.getScope()));
	}

	public static Def voidClaim(
			LocationInfo location,
			Distributor distributor,
			Logical prerequisite) {
		return voidDef(location, distributor, prerequisite).claim();
	}

	static final Obj sourceOf(ScopeInfo scope) {
		return sourceOf(scope.getScope().getContainer());
	}

	static Obj sourceOf(Container container) {

		final Obj object = container.toObject();

		if (object != null) {
			return object;
		}

		final LocalScope local = container.toLocal();

		assert local != null :
			"Definition can be created only inside object or local scope";

		return local.getOwner();
	}

	private final Obj source;
	private final St statement;
	private LogicalDef prerequisite;
	private Logical fullLogical;

	public Def(
			Obj source,
			St statement,
			LogicalDef prerequisite,
			Rescoper rescoper) {
		super(rescoper);
		this.source = source;
		this.statement = statement;
		this.prerequisite = prerequisite;
		if (prerequisite != null) {
			assertSameScope(prerequisite);
		}
	}

	protected Def(Def prototype, LogicalDef prerequisite, Rescoper rescoper) {
		this(
				prototype.getSource(),
				prototype.getStatement(),
				prerequisite,
				rescoper);
	}

	@Override
	public final Loggable getLoggable() {
		return this.statement.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.statement.getContext();
	}

	@Override
	public final Obj getSource() {
		return this.source;
	}

	public abstract boolean isClaim();

	public abstract ValueType<?> getValueType();

	public final LogicalDef getPrerequisite() {
		if (this.prerequisite == null) {
			this.prerequisite = buildPrerequisite();
			assert this.prerequisite != null :
				"Definition without prerequisite";
		}
		return this.prerequisite;
	}

	public final Logical fullLogical() {
		if (this.fullLogical != null) {
			return this.fullLogical;
		}
		return this.fullLogical = new FullLogical(this);
	}

	public final Def addPrerequisite(LogicalDef rerequisite) {

		final LogicalDef oldPrerequisite = getPrerequisite();
		final LogicalDef newPrerequisite = oldPrerequisite.and(rerequisite);

		if (oldPrerequisite.sameAs(newPrerequisite)) {
			return this;
		}

		return new FilteredDef(this, newPrerequisite, isClaim());
	}

	public abstract Def and(Logical logical);

	public Def claim() {
		return new FilteredDef(this, prerequisite(), true);
	}

	public Def unclaim() {
		return new FilteredDef(this, prerequisite(), false);
	}

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
	public final Def rescope(Rescoper rescoper) {
		return (Def) super.rescope(rescoper);
	}

	@Override
	public final Def upgradeScope(Scope scope) {
		return (Def) super.upgradeScope(scope);
	}

	@Override
	public final Def rescope(Scope scope) {
		return (Def) super.rescope(scope);
	}

	public final Definitions toDefinitions() {

		final LogicalDef logicalDef = emptyLogicalDef(this, getScope());
		final Def[] defs = new Def[] {this};

		if (isClaim()) {
			return new Definitions(
					this,
					getScope(),
					getValueType(),
					logicalDef,
					logicalDef,
					defs,
					NO_DEFS);
		}

		return new Definitions(
				this,
				getScope(),
				getValueType(),
				logicalDef,
				logicalDef,
				NO_DEFS,
				defs);
	}

	@Override
	public Def reproduce(Reproducer reproducer) {
		return (Def) super.reproduce(reproducer);
	}

	public abstract void writeValue(
			Code code,
			CodePos exit,
			HostOp host,
			ValOp result);

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (isClaim()) {
			out.append("Def![");
		} else {
			out.append("Def[");
		}
		if (this.prerequisite == null) {
			out.append("? ");
		} else if (!this.prerequisite.isTrue()) {
			out.append(this.prerequisite);
			out.append("? ");
		}
		out.append(getScoped());
		out.append(']');

		return out.toString();
	}

	@Override
	protected final St getScoped() {
		return this.statement;
	}

	protected abstract LogicalDef buildPrerequisite();

	protected abstract Value<?> calculateValue(Scope scope);

	protected abstract Logical logical();

	@Override
	protected final Def create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return create(
				rescoper,
				additionalRescoper,
				this.prerequisite != null
				? this.prerequisite.rescope(rescoper) : this.prerequisite);
	}

	protected abstract Def create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite);

	protected final LogicalDef prerequisite() {
		return this.prerequisite;
	}

	@Override
	protected Def createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			St statement,
			Rescoper rescoper) {
		getScope().getLogger().notReproducible(this);
		return null;
	}

	private static class FilteredDef extends DefWrap {

		private final boolean claim;

		FilteredDef(Def def, LogicalDef prerequisite, boolean claim) {
			super(def, prerequisite, def.getRescoper());
			this.claim = claim;
		}

		private FilteredDef(
				FilteredDef prototype,
				Def wrapped,
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
		public Def claim() {
			if (isClaim()) {
				return this;
			}
			return new FilteredDef(this, prerequisite(), true);
		}

		@Override
		public Def unclaim() {
			if (!isClaim()) {
				return this;
			}
			return new FilteredDef(this, prerequisite(), false);
		}

		@Override
		protected FilteredDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper,
				Def wrapped,
				LogicalDef prerequisite) {
			return new FilteredDef(this, wrapped, prerequisite, rescoper);
		}

		@Override
		protected FilteredDef create(Def wrapped) {
			return new FilteredDef(wrapped, getPrerequisite(), isClaim());
		}

	}

	private static final class FullLogical extends Logical {

		private final Def def;

		FullLogical(Def def) {
			super(def, def.getScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return this.def.getPrerequisite().getConstantValue().and(
					this.def.logical().getConstantValue());
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return this.def.getPrerequisite().logicalValue(scope).and(
					this.def.logical().logicalValue(scope));
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			this.def.getPrerequisite().writeFullLogical(code, exit, host);
			this.def.logical().write(
					code,
					exit,
					this.def.getRescoper().rescope(code, exit, host));
		}

		@Override
		public String toString() {
			return "(" + this.def + ")?";
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {

			final Def reproducedDef = this.def.reproduce(reproducer);

			if (reproducedDef == null) {
				return null;
			}

			return reproducedDef.fullLogical();
		}

	}

}
