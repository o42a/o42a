/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.object.common;

import static org.o42a.core.ref.impl.prediction.ObjectPrediction.predictObject;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectScope;
import org.o42a.core.ref.Prediction;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public abstract class StandaloneObjectScope extends ObjectScope {

	private final CompilerContext context;
	private final Loggable loggable;
	private final Container enclosingContainer;
	private final ScopePlace place;

	private ScopeIR ir;

	protected StandaloneObjectScope(
			LocationInfo location,
			Distributor enclosing) {
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
	public StandaloneObjectScope getFirstDeclaration() {

		final Obj propagatedFrom = toObject().getPropagatedFrom();

		if (propagatedFrom == null) {
			return this;
		}

		return (StandaloneObjectScope) propagatedFrom.getScope()
				.getFirstDeclaration();
	}

	@Override
	public StandaloneObjectScope getLastDefinition() {
		return getFirstDeclaration();
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
	public final Field toField() {
		return null;
	}

	@Override
	public Obj toObject() {

		final Obj object = getScopeObject();

		assert object != null :
			"Scope " + this + " not initialized yet";

		return object;
	}

	@Override
	public final Prediction predict(Prediction enclosing) {
		return predictObject(enclosing, toObject());
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (is(other)) {
			return true;
		}

		final Obj otherObject = other.toObject();

		if (otherObject == null) {
			return false;
		}

		return toObject().type().derivedFrom(otherObject.type());
	}

	@Override
	public final ScopeIR ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = createIR(generator);
		}
		return this.ir;
	}

	protected abstract ScopeIR createIR(Generator generator);

}
