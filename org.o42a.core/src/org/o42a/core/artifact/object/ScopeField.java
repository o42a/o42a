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
import static org.o42a.core.artifact.object.Derivation.INHERITANCE;
import static org.o42a.core.artifact.object.Obj.SCOPE_MEMBER_ID;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.ScopeFld;
import org.o42a.core.ir.field.ScopeFldOp;
import org.o42a.core.ir.local.LclOp;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;


final class ScopeField extends ObjectField {

	private Obj owner;
	private Ref scopeRef;

	ScopeField(Obj owner) {
		super(
				fieldDeclaration(
						owner,
						owner.distributeIn(owner.getContainer()),
						SCOPE_MEMBER_ID)
				.setVisibility(Visibility.PROTECTED));
		this.owner = owner;
	}

	private ScopeField(Container enclosingContainer, ScopeField sample) {
		super(enclosingContainer, sample, true);
		updateOwner(sample);
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

		final Obj artifact = getScopeArtifact();

		if (artifact != null) {
			return artifact;
		}

		final Obj newArtifact;

		if (this.owner != null) {
			newArtifact =
				this.owner.getScope().getEnclosingContainer().toObject();
		} else {
			newArtifact = this.scopeRef.resolve(getEnclosingScope()).toObject();
		}

		setScopeArtifact(newArtifact);

		return newArtifact;
	}

	@Override
	protected ScopeField propagate(Scope enclosingScope) {
		return new ScopeField(
				enclosingScope.getContainer(),
				this);
	}

	@Override
	protected FieldIR<Obj> createIR(IRGenerator generator) {
		return new IR(generator, this);
	}

	private void updateOwner(ScopeField overridden) {

		final Obj oldOwner = overridden.getOwner();

		final boolean debug = false;
			//toString().startsWith("<expressionpropagationtest>:b");

		if (oldOwner == null) {
			// No explicit owner. Copy the scope path.
			this.scopeRef = overridden.scopeRef;
			if (debug) {
				System.err.println(
						"(!) copy scope ref(" + getKey() + " / " + this + "): "
						+ "\n    " + overridden.scopeRef);
			}
			return;
		}

		final Obj newOwner = getEnclosingScope().getContainer().toObject();

		if (newOwner.derivedFrom(oldOwner, IMPLICIT_PROPAGATION, 1)) {
			// Field declared in the scope implicitly derived from
			// the previous one. Update owner.
			this.owner = newOwner;
			if (debug) {
				System.err.println(
						"(!) new owner(" + getKey() + " / "  + this + "): "
						+ "\n    " + newOwner);
			}
			return;
		}

		if (newOwner.derivedFrom(oldOwner, INHERITANCE, 1)) {
			// Inherited scope field. Construct the scope path.

			final TypeRef ancestor = newOwner.getAncestor();
			final Rescoper ancestorRescoper =
				getEnclosingScope().getEnclosingScopePath().rescoper(
						getEnclosingScope());
			final Ref ancestorRef =
				ancestor.getRef().rescope(ancestorRescoper);

			this.scopeRef = getKey().toPath().target(
					this,
					ancestorRef.distribute(),
					ancestorRef);

			if (debug) {
				System.err.println(
						"(!) new scope ref(" + getKey() + " / "  + this + "): "
						+ "\n    " + this.scopeRef);
			}

			return;
		}

		// Field declared in explicit sample. Leave owner as is.
		this.owner = oldOwner;

		if (debug) {
			System.err.println(
					"(!) old owner(" + getKey() + " / "  + this + "): "
					+ "\n    " + oldOwner);
		}
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
