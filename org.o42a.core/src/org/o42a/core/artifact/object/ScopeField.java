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

import static org.o42a.core.artifact.object.Derivation.IMPLICIT_PROPAGATION;
import static org.o42a.core.artifact.object.Obj.SCOPE_MEMBER_ID;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.ScopeFld;
import org.o42a.core.ir.field.ScopeFldOp;
import org.o42a.core.ir.local.LclOp;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;


final class ScopeField extends ObjectField {

	private final Obj owner;

	ScopeField(Obj owner) {
		super(
				fieldDeclaration(
						owner,
						owner.distributeIn(owner.getContainer()),
						SCOPE_MEMBER_ID)
				.setVisibility(Visibility.PROTECTED));
		this.owner = owner;
		setScopeArtifact(
				owner.getScope().getEnclosingContainer().toObject());
	}

	private ScopeField(
			Container enclosingContainer,
			ScopeField sample,
			Obj owner) {
		super(enclosingContainer, sample, true);
		this.owner = owner;
		setScopeArtifact(
				owner.getScope().getEnclosingContainer().toObject());
	}

	public final Obj getOwner() {
		return this.owner;
	}

	@Override
	public Path getEnclosingScopePath() {
		return null;
	}

	@Override
	public Obj getArtifact() {
		return getScopeArtifact();
	}

	@Override
	protected ScopeField propagate(Scope enclosingScope) {
		return new ScopeField(
				enclosingScope.getContainer(),
				this,
				updateOwner(enclosingScope));
	}

	@Override
	protected FieldIR<Obj> createIR(IRGenerator generator) {
		return new IR(generator, this);
	}

	private Obj updateOwner(Scope enclosingScope) {

		final Obj oldOwner = getOwner();
		final Obj newOwner = enclosingScope.getContainer().toObject();

		if (newOwner.derivedFrom(oldOwner, IMPLICIT_PROPAGATION)) {
			// Field declared in the scope implicitly derived from
			// the previous one. Update owner
			return newOwner;
		}

		final MemberKey key = getKey();

		// Find if the same field present in implicit samples.
		for (Sample sample : newOwner.getAscendants().getDiscardedSamples()) {
			if (sample.isExplicit()) {
				// Skip discarded explicit samples.
				continue;
			}

			final org.o42a.core.member.Member found =
				sample.getType().member(key);

			if (found == null) {
				continue;
			}

			// The same field is present in implicit sample.
			// Update owner.

			return newOwner;
		}

		// Field declared in the new scope. Don't change it's owner.

		return oldOwner;
	}

	private static final class IR extends FieldIR<Obj> {

		IR(IRGenerator generator, Field<Obj> field) {
			super(generator, field);
		}

		@Override
		public ScopeFldOp field(Code code, ObjOp host) {
			return (ScopeFldOp) super.field(code, host);
		}

		@Override
		protected ScopeFld declare(SubData<?> data, ObjectBodyIR bodyIR) {

			final ScopeFld fld = new ScopeFld(bodyIR, getField());

			fld.declare(
					data,
					getField().getArtifact().toObject().ir(
							getGenerator()).getMainBodyIR());

			return fld;
		}

		@Override
		protected LclOp allocateLocal(LocalBuilder builder, Code code) {
			throw new UnsupportedOperationException();
		}

	}

}
