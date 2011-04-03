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

import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.log.Loggable;


public abstract class Def<D extends Def<D>>
		extends Rescopable<D>
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
	private final LocationInfo location;
	private Logical prerequisite;
	private Logical fullLogical;

	public Def(
			Obj source,
			LocationInfo location,
			Logical prerequisite,
			Rescoper rescoper) {
		super(rescoper);
		this.location = location;
		this.source = source;
		this.prerequisite = prerequisite;
		if (prerequisite != null) {
			assertSameScope(prerequisite);
		}
	}

	protected Def(D prototype, Logical prerequisite, Rescoper rescoper) {
		super(rescoper);
		this.location = prototype.location;
		this.source = prototype.source;
		this.prerequisite = prerequisite;
	}

	@Override
	public final Loggable getLoggable() {
		return this.location.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.location.getContext();
	}

	@Override
	public final Obj getSource() {
		return this.source;
	}

	public abstract DefKind getKind();

	public final boolean isValue() {
		return getKind().isValue();
	}

	public abstract boolean hasPrerequisite();

	public final Logical fullLogical() {
		if (this.fullLogical != null) {
			return this.fullLogical;
		}
		return this.fullLogical = new FullLogical(this);
	}

	public final D addPrerequisite(Logical prerequisite) {

		final Logical oldPrerequisite = getPrerequisite();
		final Logical newPrerequisite = oldPrerequisite.and(prerequisite);

		if (oldPrerequisite.sameAs(newPrerequisite)) {
			return self();
		}

		return filter(newPrerequisite, true, getKind().isClaim());
	}

	public abstract D and(Logical logical);

	public D claim() {
		return filter(getPrerequisite(), hasPrerequisite(), true);
	}

	public D unclaim() {
		return filter(getPrerequisite(), hasPrerequisite(), false);
	}

	public abstract DefValue definitionValue(Scope scope);

	public abstract ValueDef toValue();

	public abstract CondDef toCondition();

	public abstract Definitions toDefinitions();

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		if (getKind().isValue()) {
			out.append("ValueDef[");
		} else {
			out.append("CondDef[");
		}
		if (hasPrerequisite()) {
			if (this.prerequisite == null) {
				out.append("_? ");
			} else {
				out.append(this.prerequisite).append("? ");
			}
		}
		out.append(this.location);
		if (getKind().isClaim()) {
			out.append("!]");
		} else {
			out.append(".]");
		}

		return out.toString();
	}

	protected final Logical getPrerequisite() {
		if (this.prerequisite != null) {
			return this.prerequisite;
		}
		if (!hasPrerequisite()) {
			return this.prerequisite = logicalTrue(this, getScope());
		}
		this.prerequisite = buildPrerequisite();
		assert this.prerequisite != null :
			"Definition without prerequisite";
		return this.prerequisite;
	}

	protected abstract Logical buildPrerequisite();

	protected abstract Logical getLogical();

	abstract D filter(
			Logical prerequisite,
			boolean hasPrerequisite,
			boolean claim);

	private static final class FullLogical extends Logical {

		private final Def<?> def;

		FullLogical(Def<?> def) {
			super(def, def.getScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return this.def.getPrerequisite().getConstantValue().and(
					this.def.getLogical().getConstantValue());
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			scope = this.def.getRescoper().rescope(scope);
			return this.def.getPrerequisite().logicalValue(scope).and(
					this.def.getLogical().logicalValue(scope));
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			host = this.def.getRescoper().rescope(code, exit, host);
			this.def.getPrerequisite().write(code, exit, host);
			this.def.getLogical().write(code, exit, host);
		}

		@Override
		public String toString() {
			return "(" + this.def + ")?";
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

	}

}
