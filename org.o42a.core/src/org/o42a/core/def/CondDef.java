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

import static org.o42a.core.ref.Cond.falseCondition;
import static org.o42a.core.ref.Cond.runtimeCondition;
import static org.o42a.core.ref.Cond.trueCondition;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Cond;
import org.o42a.core.value.LogicalValue;


public abstract class CondDef extends Rescopable implements SourceSpec {

	public static CondDef emptyCondDef(LocationSpec location, Scope scope) {
		return new EmptyCondDef(location, scope);
	}

	public static CondDef trueCondDef(LocationSpec location, Scope scope) {
		return trueCondition(location, scope).toCondDef();
	}

	public static CondDef falseCondDef(LocationSpec location, Scope scope) {
		return falseCondition(location, scope).toCondDef();
	}

	static CondDef runtimeCondDef(
			LocationSpec location,
			Scope scope,
			Obj source) {
		return runtimeCondition(location, scope).toCondDef();
	}

	private final Obj source;
	private final SingleCondDef[] requirements;
	private Cond fullCondition;

	CondDef(
			Obj source,
			Cond condition,
			Rescoper rescoper,
			SingleCondDef[] requirements) {
		super(condition, rescoper);
		this.source = source;
		this.requirements = requirements;
	}

	CondDef(
			Obj source,
			Cond condition,
			Rescoper rescoper) {
		super(condition, rescoper);
		this.source = source;
		this.requirements = new SingleCondDef[] {(SingleCondDef) this};
	}

	public final boolean isEmpty() {
		return this.requirements.length == 0;
	}

	@Override
	public final Obj getSource() {
		return this.source;
	}

	public final Cond fullCondition() {
		if (this.fullCondition != null) {
			return this.fullCondition;
		}

		final Cond condition = condition();

		if (condition.isTrue()) {
			return this.fullCondition = trueCondition(this, getScope());
		}
		if (condition.isFalse()) {
			return this.fullCondition = falseCondition(this, getScope());
		}

		final Cond fullCondition = createFullCondition();

		assertSameScope(fullCondition);

		return this.fullCondition = fullCondition;
	}

	public final LogicalValue getConstantValue() {
		return fullCondition().getConstantValue();
	}

	public final LogicalValue logicalValue(Scope scope) {
		return fullCondition().logicalValue(scope);
	}

	public final boolean isDefinite() {
		return fullCondition().isConstant();
	}

	public final boolean isTrue() {
		return fullCondition().isTrue();
	}

	public final boolean isFalse() {
		return fullCondition().isFalse();
	}

	public final CondDef[] getRequirements() {
		return this.requirements;
	}

	@Override
	public CondDef rescope(Rescoper rescoper) {
		return (CondDef) super.rescope(rescoper);
	}

	@Override
	public final CondDef upgradeScope(Scope scope) {
		return (CondDef) super.upgradeScope(scope);
	}

	@Override
	public final CondDef rescope(Scope scope) {
		return (CondDef) super.rescope(scope);
	}

	public final CondDef and(Cond requirement) {
		if (requirement == null) {
			return this;
		}
		return and(requirement.toCondDef());
	}

	public final CondDef and(CondDef requirement) {
		if (requirement == null) {
			return this;
		}

		assertSameScope(requirement);

		if (requirement.isEmpty()) {
			return this;
		}

		final Cond fullCondition1 = fullCondition();
		final Cond fullCondition2 = requirement.fullCondition();
		final Cond fullCondition = fullCondition1.and(fullCondition2);

		if (fullCondition == fullCondition1) {
			return this;
		}
		if (fullCondition == fullCondition2) {
			return requirement;
		}

		return conjunctionWith(requirement);
	}

	public boolean sameAs(CondDef other) {
		if (other.isEmpty()) {
			return false;
		}
		return fullCondition().sameAs(other.fullCondition());
	}

	public boolean hasDefinitionsFrom(Obj source) {
		for (SingleCondDef cond : this.requirements) {
			if (cond.getSource() == source) {
				return true;
			}
		}
		return false;
	}

	public void writeCondition(Code code, CodePos exit, HostOp host) {

		final HostOp rescopedHost =
			getRescoper().rescope(code, exit, host);

		condition().write(code, exit, rescopedHost);
	}

	public abstract void writeFullCondition(
			Code code,
			CodePos exit,
			HostOp host);

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeSpec other) {
		Scoped.assertCompatibleScope(this, other);
	}

	protected abstract Cond createFullCondition();

	protected abstract CondDef conjunctionWith(CondDef requirement);

	final Cond condition() {
		return (Cond) getScoped();
	}

	final SingleCondDef[] requirements() {
		return this.requirements;
	}

}
