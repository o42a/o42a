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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.util.log.Loggable;


public abstract class Def<D extends Def<D>>
		extends RescopableStatement
		implements SourceInfo {

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
	private final Statement statement;
	private LogicalDef prerequisite;
	private Logical fullLogical;

	public Def(
			Obj source,
			Statement statement,
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

	protected Def(D prototype, LogicalDef prerequisite, Rescoper rescoper) {
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

	public abstract DefKind getKind();

	public final boolean isValue() {
		return getKind().isValue();
	}

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

	@SuppressWarnings("unchecked")
	public final D addPrerequisite(LogicalDef rerequisite) {

		final LogicalDef oldPrerequisite = getPrerequisite();
		final LogicalDef newPrerequisite = oldPrerequisite.and(rerequisite);

		if (oldPrerequisite.sameAs(newPrerequisite)) {
			return (D) this;
		}

		return filter(newPrerequisite, getKind().isClaim());
	}

	public abstract D and(Logical logical);

	public D claim() {
		return filter(prerequisite(), true);
	}

	public D unclaim() {
		return filter(prerequisite(), false);
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

	@SuppressWarnings("unchecked")
	@Override
	public final D rescope(Rescoper rescoper) {
		return (D) super.rescope(rescoper);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final D upgradeScope(Scope scope) {
		return (D) super.upgradeScope(scope);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final D rescope(Scope scope) {
		return (D) super.rescope(scope);
	}

	public abstract ValueDef toValue();

	public abstract CondDef toCondition();

	public abstract Definitions toDefinitions();

	@SuppressWarnings("unchecked")
	@Override
	public D reproduce(Reproducer reproducer) {
		return (D) super.reproduce(reproducer);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (getKind().isClaim()) {
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
	protected final Statement getScoped() {
		return this.statement;
	}

	protected abstract LogicalDef buildPrerequisite();

	protected abstract Value<?> calculateValue(Scope scope);

	protected abstract Logical logical();

	@Override
	protected final D create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return create(
				rescoper,
				additionalRescoper,
				this.prerequisite != null
				? this.prerequisite.rescope(rescoper) : this.prerequisite);
	}

	protected final LogicalDef prerequisite() {
		return this.prerequisite;
	}

	protected abstract D create(
			Rescoper rescoper,
			Rescoper additionalRescoper,
			LogicalDef prerequisite);

	@Override
	protected D createReproduction(
			Reproducer reproducer,
			Reproducer rescopedReproducer,
			Statement statement,
			Rescoper rescoper) {
		getScope().getLogger().notReproducible(this);
		return null;
	}

	abstract D filter(LogicalDef prerequisite, boolean claim);

	private static final class FullLogical extends Logical {

		private final Def<?> def;

		FullLogical(Def<?> def) {
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

			final Def<?> reproducedDef = this.def.reproduce(reproducer);

			if (reproducedDef == null) {
				return null;
			}

			return reproducedDef.fullLogical();
		}

	}

}
