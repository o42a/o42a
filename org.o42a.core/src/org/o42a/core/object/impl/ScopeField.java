/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.object.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.scope.ScopeFld;
import org.o42a.core.ir.field.scope.ScopeFldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.common.ObjectField;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.Step;
import org.o42a.core.ref.path.impl.member.AbstractMemberStep;
import org.o42a.core.ref.type.TypeRef;


public final class ScopeField extends ObjectField {

	public static Path reusedScopePath(Obj object) {

		final Scope enclosing = object.getScope().getEnclosingScope();

		assert enclosing.toObject() != null :
			"No enclosing object found";

		final Obj propagatedFrom = object.getPropagatedFrom();

		if (propagatedFrom != null) {
			// Reuse the enclosing scope path from the object
			// this one is propagated from.
			return propagatedFrom.getScope().getEnclosingScopePath();
		}

		return reusedFromSample(object);
	}

	public static ScopeField scopeFieldFor(Obj owner) {
		if (owner.getScope().getEnclosingScope().toObject() == null) {
			// Only object members may have an enclosing scope path.
			return null;
		}

		final Path enclosingScopePath =
				owner.getScope().getEnclosingScopePath();

		if (enclosingScopePath == null) {
			// Enclosing scope path not defined.
			return null;
		}

		final MemberKey scopeFieldKey = scopeFieldKey(enclosingScopePath);

		if (!scopeFieldKey.getOrigin().is(owner.getScope())) {
			// Enclosing scope field is derived from overridden object.
			return null;
		}

		return new ScopeField(owner, scopeFieldKey.getMemberId());
	}

	static Obj objectScope(Obj object, MemberKey scopeFieldKey) {

		final ObjectType newOwnerType = object.type();
		final Obj ancestor = newOwnerType.getAncestor().getType();
		final Member ancestorMember = ancestor.member(scopeFieldKey);

		if (ancestorMember != null) {
			// Scope field is present in ancestor.
			// Preserve an ancestor`s scope.
			return ancestorMember.substance(dummyUser()).toObject();
		}

		final Sample sample = newOwnerType.getSample();

		if (sample != null
				&& sample.getObject().member(scopeFieldKey) != null) {
			// Scope field is declared in implicit sample.
			// Update the owner with an actual one.
			return object.getScope().getEnclosingScope().toObject();
		}

		throw new IllegalStateException();
	}

	static MemberKey scopeFieldKey(Path enclosingScopePath) {

		final Step[] steps = enclosingScopePath.getSteps();

		assert steps.length == 1 :
			"Enclosing path scope should contain exactly one step";

		final AbstractMemberStep step = (AbstractMemberStep) steps[0];

		return step.getMemberKey();
	}

	private static Path reusedFromSample(Obj object) {

		final Ascendants ascendants = object.type().getAscendants();
		final Sample sample = ascendants.getSample();

		if (sample == null) {
			return null;
		}

		final TypeRef ancestor = ascendants.getExplicitAncestor();

		if (ancestor == null) {
			return null;
		}

		final Path path = sample.getObject().getScope().getEnclosingScopePath();
		final MemberKey scopeFieldKey = scopeFieldKey(path);

		if (ancestor.getType().member(scopeFieldKey) != null) {
			// Scope field is overridden in ancestor.
			// Can not reuse it.
			return null;
		}

		assert objectScope(object, scopeFieldKey)
				.getScope()
				.is(object.getScope().getEnclosingScope()) :
			"Wrong enclosing scope path";

		return path;
	}

	private final ScopeField overridden;

	private ScopeField(Obj owner, MemberId memberId) {
		super(
				owner,
				fieldDeclaration(
						owner,
						owner.distributeIn(owner),
						memberId)
				.setVisibility(Visibility.PROTECTED));
		this.overridden = null;
		setScopeObject(owner.getScope().getEnclosingContainer().toObject());
	}

	private ScopeField(MemberField member, ScopeField overridden) {
		super(member);
		this.overridden = overridden;
	}

	@Override
	public final boolean isScopeField() {
		return true;
	}

	@Override
	public Path getEnclosingScopePath() {
		return null;
	}

	@Override
	protected Obj createObject() {

		final Obj objectScope =
				objectScope(getEnclosingContainer().toObject(), getKey());
		// Preserve an old scope by default.
		final Obj object =
				objectScope != null ? objectScope : this.overridden.toObject();

		setScopeObject(object);

		return object;
	}

	@Override
	protected ObjectField propagate(MemberField member) {
		return new ScopeField(member, this);
	}

	@Override
	protected FieldIR createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends FieldIR {

		IR(Generator generator, Field field) {
			super(generator, field);
		}

		@Override
		public ScopeFldOp field(Code code, ObjOp host) {
			return (ScopeFldOp) super.field(code, host);
		}

		@Override
		protected ScopeFld declare(ObjectIRBodyData data) {

			final ScopeFld fld = new ScopeFld(getField());
			final Obj target = getField().toObject();

			fld.declare(data, target);

			return fld;
		}

	}

}
