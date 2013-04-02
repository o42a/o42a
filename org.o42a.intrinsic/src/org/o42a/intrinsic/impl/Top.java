/*
    Intrinsics
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.intrinsic.impl;

import static java.util.Collections.emptyList;

import java.util.Collection;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.util.string.ID;


public final class Top extends AbstractScope implements MemberContainer {

	private final CompilerContext context;
	private final ID id;
	private TopIR ir;

	public Top(CompilerContext context) {
		this.context = context;
		this.id = ID.id("TOP");
	}

	@Override
	public final Location getLocation() {
		return this.context.getLocation();
	}

	@Override
	public MemberContainer getContainer() {
		return this;
	}

	@Override
	public Container getParentContainer() {
		return null;
	}

	@Override
	public ID getId() {
		return this.id;
	}

	@Override
	public boolean isTopScope() {
		return true;
	}

	@Override
	public Container getEnclosingContainer() {
		return null;
	}

	@Override
	public Path getEnclosingScopePath() {
		return null;
	}

	@Override
	public Scope getFirstDeclaration() {
		return this;
	}

	@Override
	public Scope getLastDefinition() {
		return this;
	}

	@Override
	public Collection<? extends Member> getMembers() {
		return emptyList();
	}

	@Override
	public Member toMember() {
		return null;
	}

	@Override
	public Obj toObject() {
		return null;
	}

	@Override
	public Clause toClause() {
		return null;
	}

	@Override
	public Namespace toNamespace() {
		return null;
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return ConstructionMode.FULL_CONSTRUCTION;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return null;
	}

	@Override
	public MemberPath member(
			Access access,
			MemberId memberId,
			Obj declaredIn) {
		return null;
	}

	@Override
	public MemberPath findMember(
			Access access,
			MemberId memberId,
			Obj declaredIn) {
		return null;
	}

	@Override
	public Prediction predict(Prediction enclosing) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean derivedFrom(Scope other) {
		return is(other);
	}

	@Override
	public ScopeIR ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = new TopIR(generator, this);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		return "TOP";
	}

}
