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

import static org.o42a.core.ref.Logical.logicalFalse;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.CompilerContext;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.log.Loggable;


public abstract class LogicalDef extends Rescopable implements SourceSpec {

	public static LogicalDef emptyLogicalDef(
			LocationSpec location,
			Scope scope) {
		return new EmptyLogicalDef(location, scope);
	}

	public static LogicalDef trueLogicalDef(
			LocationSpec location,
			Scope scope) {
		return logicalTrue(location, scope).toLogicalDef();
	}

	public static LogicalDef falseLogicalDef(
			LocationSpec location,
			Scope scope) {
		return logicalFalse(location, scope).toLogicalDef();
	}

	private final Obj source;
	private final Logical logical;
	private final SingleLogicalDef[] requirements;
	private Logical fullLogical;

	LogicalDef(
			Obj source,
			Logical logical,
			Rescoper rescoper,
			SingleLogicalDef[] requirements) {
		super(rescoper);
		this.source = source;
		this.logical = logical;
		this.requirements = requirements;
		assert requirements.length != 0 || getClass() == EmptyLogicalDef.class :
			"At least one requirement expected";
	}

	LogicalDef(
			Obj source,
			Logical logical,
			Rescoper rescoper) {
		super(rescoper);
		this.source = source;
		this.logical = logical;
		this.requirements = new SingleLogicalDef[] {(SingleLogicalDef) this};
	}

	@Override
	public final CompilerContext getContext() {
		return this.logical.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.logical.getLoggable();
	}

	public final boolean isEmpty() {
		return this.requirements.length == 0;
	}

	@Override
	public final Obj getSource() {
		return this.source;
	}

	public final Logical fullLogical() {
		if (this.fullLogical != null) {
			return this.fullLogical;
		}

		if (this.logical.isTrue()) {
			return this.fullLogical = logicalTrue(this, getScope());
		}
		if (this.logical.isFalse()) {
			return this.fullLogical = logicalFalse(this, getScope());
		}

		final Logical fullLogical = createFullLogical();

		assertSameScope(fullLogical);

		return this.fullLogical = fullLogical;
	}

	public final LogicalValue getConstantValue() {
		return fullLogical().getConstantValue();
	}

	public final LogicalValue logicalValue(Scope scope) {
		return fullLogical().logicalValue(scope);
	}

	public final boolean isDefinite() {
		return fullLogical().isConstant();
	}

	public final boolean isTrue() {
		return fullLogical().isTrue();
	}

	public final boolean isFalse() {
		return fullLogical().isFalse();
	}

	public final LogicalDef[] getRequirements() {
		return this.requirements;
	}

	@Override
	public LogicalDef rescope(Rescoper rescoper) {
		return (LogicalDef) super.rescope(rescoper);
	}

	@Override
	public final LogicalDef upgradeScope(Scope scope) {
		return (LogicalDef) super.upgradeScope(scope);
	}

	@Override
	public final LogicalDef rescope(Scope scope) {
		return (LogicalDef) super.rescope(scope);
	}

	public final LogicalDef and(Logical requirement) {
		if (requirement == null) {
			return this;
		}
		return and(requirement.toLogicalDef());
	}

	public final LogicalDef and(LogicalDef requirement) {
		if (requirement == null) {
			return this;
		}

		assertSameScope(requirement);

		if (requirement.isEmpty()) {
			return this;
		}

		final Logical fullLogical1 = fullLogical();
		final Logical fullLogical2 = requirement.fullLogical();
		final Logical fullLogical = fullLogical1.and(fullLogical2);

		if (fullLogical == fullLogical1) {
			return this;
		}
		if (fullLogical == fullLogical2) {
			return requirement;
		}

		return conjunctionWith(requirement);
	}

	public boolean sameAs(LogicalDef other) {
		if (other.isEmpty()) {
			return false;
		}
		return fullLogical().sameAs(other.fullLogical());
	}

	public boolean hasDefinitionsFrom(Obj source) {
		for (SingleLogicalDef requirement : this.requirements) {
			if (requirement.getSource() == source) {
				return true;
			}
		}
		return false;
	}

	public void writeLogical(Code code, CodePos exit, HostOp host) {

		final HostOp rescopedHost =
			getRescoper().rescope(code, exit, host);

		this.logical.write(code, exit, rescopedHost);
	}

	public abstract void writeFullLogical(
			Code code,
			CodePos exit,
			HostOp host);

	protected abstract Logical createFullLogical();

	protected abstract LogicalDef conjunctionWith(LogicalDef requirement);

	@Override
	protected final Logical getScoped() {
		return this.logical;
	}

	final SingleLogicalDef[] requirements() {
		return this.requirements;
	}

}
