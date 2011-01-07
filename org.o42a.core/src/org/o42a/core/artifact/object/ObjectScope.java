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
package org.o42a.core.artifact.object;

import org.o42a.ast.Node;
import org.o42a.core.*;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;


public abstract class ObjectScope extends AbstractScope {

	private final CompilerContext context;
	private final Node node;
	private final Container enclosingContainer;
	private final ScopePlace place;
	private Path enclosingScopePath;
	private Obj object;

	private ScopeIR ir;

	protected ObjectScope(LocationSpec location, Distributor enclosing) {
		this.context = location.getContext();
		this.node = location.getNode();
		this.enclosingContainer = enclosing.getContainer();
		this.place = enclosing.getPlace();
	}

	@Override
	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public final Node getNode() {
		return this.node;
	}

	@Override
	public ScopePlace getPlace() {
		return this.place;
	}

	@Override
	public final Obj getContainer() {
		assert this.object != null :
			"Scope " + this + " not initialized yet";
		return this.object;
	}

	@Override
	public Container getEnclosingContainer() {
		return this.enclosingContainer;
	}

	@Override
	public Field<Obj> toField() {
		return null;
	}

	@Override
	public Path getEnclosingScopePath() {
		if (this.enclosingContainer.getScope().isTopScope()) {
			return null;
		}
		this.enclosingScopePath = this.object.scopePath();
		return this.enclosingScopePath;
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		final Obj otherObject = other.getContainer().toObject();

		if (otherObject == null) {
			return false;
		}

		return getContainer().toObject().derivedFrom(otherObject);
	}

	@Override
	public final ScopeIR ir(IRGenerator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = createIR(generator);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		return (this.object != null
				? this.object.toString() : "ObjectScope");
	}

	protected abstract ScopeIR createIR(IRGenerator generator);

	void setScopeObject(Obj object) {
		this.object = object;
	}

}
