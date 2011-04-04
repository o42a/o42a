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

import static org.o42a.core.def.Rescoper.transparentRescoper;
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
	private DefKind kind;
	private boolean hasPrerequisite;
	private Logical precondition;
	private Logical prerequisite;
	private Logical logical;

	Def(
			Obj source,
			LocationInfo location,
			DefKind kind,
			Rescoper rescoper) {
		super(rescoper);
		this.location = location;
		this.source = source;
		this.kind = kind;
		this.hasPrerequisite = kind.isValue();
	}

	Def(D prototype, Rescoper rescoper) {
		super(rescoper);
		this.source = prototype.source;
		this.location = prototype.location;
		this.kind = prototype.kind;
		this.hasPrerequisite = prototype.hasPrerequisite;
		this.prerequisite = prototype.prerequisite;
		this.precondition = prototype.precondition;
		this.logical = prototype.logical;
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

	public final DefKind getKind() {
		return this.kind;
	}

	public final boolean isValue() {
		return getKind().isValue();
	}

	public final boolean hasPrerequisite() {
		return this.hasPrerequisite;
	}

	public final Logical fullLogical() {
		return new FullLogical(this);
	}

	public final D addPrerequisite(Logical prerequisite) {

		final Logical oldPrerequisite = getPrerequisite();
		final Logical newPrerequisite = oldPrerequisite.and(prerequisite);

		if (oldPrerequisite.sameAs(newPrerequisite)) {
			return self();
		}

		final D copy = copy();

		copy.hasPrerequisite = true;
		copy.prerequisite = newPrerequisite;

		return copy;
	}

	public final D addPrecondition(Logical precondition) {

		final Logical oldPrecondition = getPrecondition();
		final Logical newPrecondition =
			Logical.and(oldPrecondition, precondition);

		if (newPrecondition == oldPrecondition) {
			return self();
		}

		final D copy = copy();

		copy.precondition = newPrecondition;
		copy.logical = null;

		return copy;
	}

	public final D claim() {
		if (getKind().isClaim()) {
			return self();
		}

		final D copy = copy();

		copy.kind = this.kind.claim();

		return copy;
	}

	public final D unclaim() {
		if (!getKind().isClaim()) {
			return self();
		}

		final D copy = copy();

		copy.kind = this.kind.unclaim();

		return copy;
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
		if (this.logical != null) {
			out.append(this.logical).append(", ");
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

	protected final Logical getPrecondition() {
		if (this.precondition != null) {
			return this.precondition;
		}
		return this.precondition = buildPrecondition();
	}

	protected abstract Logical buildPrecondition();

	protected final Logical getLogical() {
		if (this.logical != null) {
			return this.logical;
		}
		return this.logical = getPrecondition().and(buildLogical());
	}

	protected abstract Logical buildLogical();

	final LocationInfo getLocation() {
		return this.location;
	}

	final void update(DefKind kind, boolean hasPrerequisite) {
		this.kind = kind;
		this.hasPrerequisite = hasPrerequisite;
	}

	private final D copy() {
		return create(getRescoper(), transparentRescoper(getScope()));
	}

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
			assertCompatible(scope);

			final Scope rescoped = this.def.getRescoper().rescope(scope);

			return this.def.getPrerequisite().logicalValue(rescoped).and(
					this.def.getLogical().logicalValue(rescoped));
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
