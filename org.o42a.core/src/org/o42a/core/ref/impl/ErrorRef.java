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
package org.o42a.core.ref.impl;

import static org.o42a.core.ref.Logical.logicalFalse;

import org.o42a.core.Distributor;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;


public final class ErrorRef extends Ref {

	public ErrorRef(LocationInfo location, Distributor distributor) {
		super(
				location,
				distributor,
				logicalFalse(location, distributor.getScope()));
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return toTypeRef();
	}

	@Override
	public Resolution resolve(Resolver resolver) {
		return resolver.noResolution(this);
	}

	@Override
	public Definer define(StatementEnv env) {
		return new ErrorRefDefiner(this, env);
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		return errorRef(this, reproducer.distribute());
	}

	@Override
	public String toString() {
		return "ERROR";
	}

	@Override
	protected FieldDefinition createFieldDefinition() {
		return defaultFieldDefinition();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
	}

	@Override
	protected RefOp createOp(HostOp host) {
		throw new UnsupportedOperationException(
				"Can not generate IR for ERROR");
	}

}
