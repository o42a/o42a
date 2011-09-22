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
package org.o42a.core.artifact;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public abstract class ArtifactScope<A extends Artifact<A>>
		extends AbstractScope {

	private final CompilerContext context;
	private final Loggable loggable;
	private final Container enclosingContainer;
	private final ScopePlace place;
	private A artifact;

	private ScopeIR ir;

	public ArtifactScope(LocationInfo location, Distributor enclosing) {
		this.context = location.getContext();
		this.loggable = location.getLoggable();
		this.enclosingContainer = enclosing.getContainer();
		this.place = enclosing.getPlace();
	}

	@Override
	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	@Override
	public final ScopePlace getPlace() {
		return this.place;
	}

	@Override
	public A getArtifact() {
		assert this.artifact != null :
			"Scope " + this + " not initialized yet";
		return this.artifact;
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.enclosingContainer;
	}

	@Override
	public final Member toMember() {
		return null;
	}

	@Override
	public final Field<Obj> toField() {
		return null;
	}

	@Override
	public final ScopeIR ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = createIR(generator);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		if (this.artifact == null) {
			return super.toString();
		}
		return this.artifact.toString();
	}

	protected abstract ScopeIR createIR(Generator generator);

	void setScopeArtifact(A artifact) {
		this.artifact = artifact;
	}

}
