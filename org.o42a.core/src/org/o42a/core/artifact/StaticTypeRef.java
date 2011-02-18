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
package org.o42a.core.artifact;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;


public final class StaticTypeRef extends TypeRef {

	StaticTypeRef(Ref ref, Rescoper rescoper) {
		super(ref.fixScope(), rescoper);
	}

	@Override
	public StaticTypeRef toStatic() {
		return this;
	}

	@Override
	public final StaticTypeRef rescope(Rescoper rescoper) {
		return (StaticTypeRef) super.rescope(rescoper);
	}

	@Override
	public final StaticTypeRef rescope(Scope scope) {
		return (StaticTypeRef) super.rescope(scope);
	}

	@Override
	public StaticTypeRef upgradeScope(Scope scope) {
		return (StaticTypeRef) super.upgradeScope(scope);
	}

	@Override
	public StaticTypeRef reproduce(Reproducer reproducer) {
		return (StaticTypeRef) super.reproduce(reproducer);
	}

	@Override
	public RefOp op(Code code, CodePos exit, HostOp host) {
		return getType().selfRef().op(host);
	}

	public final TypeRelation relationTo(StaticTypeRef other) {
		return relationTo(other, true);
	}

	public TypeRelation relationTo(
			StaticTypeRef other,
			boolean reportIncompatibility) {
		assertSameScope(other);
		if (!other.validate()) {
			return TypeRelation.PREFERRED;
		}
		if (!validate()) {
			return TypeRelation.INVALID;
		}

		final Obj type1 = getType();
		final Obj type2 = other.getType();

		if (type1 == type2) {
			return TypeRelation.SAME;
		}
		if (type2.derivedFrom(type1)) {
			return TypeRelation.ASCENDANT;
		}
		if (type1.derivedFrom(type2)) {
			return TypeRelation.DERIVATIVE;
		}
		if (reportIncompatibility) {
			getLogger().incompatible(other, this);
		}
		return TypeRelation.INCOMPATIBLE;
	}

	@Override
	protected StaticTypeRef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new StaticTypeRef(getRef(), rescoper);
	}

	@Override
	protected StaticTypeRef createReproduction(
			Reproducer reproducer,
			Ref reproducedRef,
			Rescoper rescoper) {
		return new StaticTypeRef(reproducedRef, rescoper);
	}

}
