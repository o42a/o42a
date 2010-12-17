/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.core.def.CondDef.emptyCondDef;
import static org.o42a.core.def.CondDef.trueCondDef;
import static org.o42a.core.def.Definitions.NO_DEFS;
import static org.o42a.core.ref.Cond.falseCondition;
import static org.o42a.core.ref.Cond.trueCondition;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.RefDef.VoidDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Cond;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.St;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class Def extends RescopableStatement implements SourceSpec {

	public static Def voidDef(LocationSpec location, Distributor distributor) {
		return voidDef(
				location,
				distributor,
				trueCondition(location, distributor.getScope()));
	}

	public static Def falseDef(LocationSpec location, Distributor distributor) {
		return voidDef(
				location,
				distributor,
				falseCondition(location, distributor.getScope()));
	}

	public static Def voidClaim(
			LocationSpec location,
			Distributor distributor) {
		return voidClaim(
				location,
				distributor,
				trueCondition(location, distributor.getScope()));
	}

	public static Def falseClaim(
			LocationSpec location,
			Distributor distributor) {
		return voidClaim(
				location,
				distributor,
				falseCondition(location, distributor.getScope()));
	}

	public static Def voidDef(
			LocationSpec location,
			Distributor distributor,
			Cond prerequisite) {

		final Ref voidRef = voidRef(location, distributor);

		return new VoidDef(
				voidRef,
				prerequisite != null ? prerequisite.toCondDef()
				: trueCondDef(location, voidRef.getScope()));
	}

	public static Def voidClaim(
			LocationSpec location,
			Distributor distributor,
			Cond prerequisite) {
		return voidDef(location, distributor, prerequisite).claim();
	}

	static final Obj sourceOf(ScopeSpec scope) {
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
	private CondDef prerequisite;
	private Cond fullCondition;

	public Def(
			Obj source,
			St statement,
			CondDef prerequisite,
			Rescoper rescoper) {
		super(statement, rescoper);
		this.source = source;
		this.prerequisite = prerequisite;
		if (prerequisite != null) {
			assertSameScope(prerequisite);
		}
	}

	protected Def(Def prototype, CondDef prerequisite, Rescoper rescoper) {
		this(
				prototype.getSource(),
				prototype.getStatement(),
				prerequisite,
				rescoper);
	}

	@Override
	public final Obj getSource() {
		return this.source;
	}

	public abstract boolean isClaim();

	public abstract ValueType<?> getValueType();

	public final CondDef getPrerequisite() {
		if (this.prerequisite == null) {
			this.prerequisite = buildPrerequisite();
			assert this.prerequisite != null :
				"Definition without condition";
		}
		return this.prerequisite;
	}

	public final Cond fullCondition() {
		if (this.fullCondition != null) {
			return this.fullCondition;
		}
		return this.fullCondition = new FullCondition(this);
	}

	public final Def addPrerequisite(CondDef rerequisite) {

		final CondDef oldPrerequisite = getPrerequisite();
		final CondDef newPrerequisite = oldPrerequisite.and(rerequisite);

		if (oldPrerequisite.sameAs(newPrerequisite)) {
			return this;
		}

		return new FilteredDef(this, newPrerequisite, isClaim());
	}

	public abstract Def and(Cond condition);

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
			DefValue.alwaysMeaningfulValue(this, value);
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

		final CondDef postCondition = emptyCondDef(this, getScope());
		final Def[] defs = new Def[] {this};

		if (isClaim()) {
			return new Definitions(
					this,
					getScope(),
					getValueType(),
					postCondition,
					postCondition,
					defs,
					NO_DEFS);
		}

		return new Definitions(
				this,
				getScope(),
				getValueType(),
				postCondition,
				postCondition,
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

	protected abstract CondDef buildPrerequisite();

	protected abstract Value<?> calculateValue(Scope scope);

	protected abstract Cond condition();

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
			CondDef prerequisite);

	protected final CondDef prerequisite() {
		return this.prerequisite;
	}

	@Override
	protected Def createReproduction(
			Reproducer reproducer,
			St statement,
			Rescoper rescoper) {
		getScope().getLogger().notReproducible(this);
		return null;
	}

	private static class FilteredDef extends DefWrap {

		private final boolean claim;

		FilteredDef(Def def, CondDef prerequisite, boolean claim) {
			super(def, prerequisite, def.getRescoper());
			this.claim = claim;
		}

		private FilteredDef(
				FilteredDef prototype,
				Def wrapped,
				CondDef prerequisite,
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
				CondDef prerequisite) {
			return new FilteredDef(this, wrapped, prerequisite, rescoper);
		}

		@Override
		protected FilteredDef create(Def wrapped) {
			return new FilteredDef(wrapped, getPrerequisite(), isClaim());
		}

	}

	private static final class FullCondition extends Cond {

		private final Def def;

		FullCondition(Def def) {
			super(def, def.getScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return this.def.getPrerequisite().getConstantValue().and(
					this.def.condition().getConstantValue());
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return this.def.getPrerequisite().logicalValue(scope).and(
					this.def.condition().logicalValue(scope));
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			this.def.getPrerequisite().writeFullCondition(code, exit, host);
			this.def.condition().write(
					code,
					exit,
					this.def.getRescoper().rescope(code, exit, host));
		}

		@Override
		public String toString() {
			return "(" + this.def + ")?";
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {

			final Def reproducedDef = this.def.reproduce(reproducer);

			if (reproducedDef == null) {
				return null;
			}

			return reproducedDef.fullCondition();
		}

	}

}
