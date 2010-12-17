/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.member.field;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.MemberKey.brokenMemberKey;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.core.Container;
import org.o42a.core.Location;
import org.o42a.core.Scope;
import org.o42a.core.artifact.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;


public abstract class MemberField extends Member {

	private final FieldDeclaration declaration;
	private MemberKey key;
	private Visibility visibility;

	public MemberField(FieldDeclaration declaration) {
		super(declaration, declaration.distribute());
		this.declaration = declaration;
	}

	public MemberField(Container container, String name) {
		super(
				new Location(container.getContext(), null),
				declarativeDistributor(container));
		this.declaration =
			fieldDeclaration(this, distribute(), memberName(name));
	}

	private MemberField(Container container, MemberField overridden) {
		super(overridden, overridden.distributeIn(container));
		this.key = overridden.getKey();
		this.visibility = overridden.getVisibility();
		this.declaration =
			new FieldDeclaration(
					overridden,
					distribute(),
					overridden.getDeclaration())
			.override();
	}

	public final boolean isAdapter() {
		return getDeclaration().isAdapter();
	}

	@Override
	public final MemberId getId() {
		return getDeclaration().getMemberId();
	}

	public final FieldDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public final Visibility getVisibility() {
		getKey();
		return this.visibility;
	}

	@Override
	public MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		if (getDeclaration().isOverride()) {
			return this.key = overrideField();
		}
		return this.key = declareNewField();
	}

	@Override
	public final LocalScope toLocal() {
		return null;
	}

	@Override
	public final Clause toClause() {
		return null;
	}

	@Override
	public final Container getSubstance() {
		return toField().getContainer();
	}

	@Override
	public boolean isOverride() {
		return this.declaration.isOverride();
	}

	@Override
	public boolean isPropagated() {
		return false;
	}

	@Override
	public final boolean isAbstract() {
		return getDeclaration().isAbstract();
	}

	@Override
	public MemberField propagateTo(Scope scope) {
		return toField().propagateTo(scope).toMember();
	}

	@Override
	public void resolveAll() {

		final Field<?> field = toField();

		if (!field.isClone()) {
			field.getArtifact().resolveAll();
		}
	}

	@Override
	public MemberField wrap(Member inherited, Container container) {

		final ArtifactKind<?> artifactKind = toField().getArtifact().getKind();

		if (artifactKind == ArtifactKind.OBJECT) {
			return new ObjectFieldWrap(
					container,
					inherited.toField(),
					toField()).toMember();
		}
		if (artifactKind == ArtifactKind.LINK
				|| artifactKind == ArtifactKind.VARIABLE) {
			return new LinkFieldWrap(
					getContainer(),
					inherited.toField(),
					toField()).toMember();
		}
		if (artifactKind == ArtifactKind.ARRAY) {
			return new ArrayFieldWrap(
					getContainer(),
					inherited.toField(),
					toField()).toMember();
		}

		throw new IllegalStateException("Can not wrap " + this);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Scope enclosingScope = getScope();

		if (enclosingScope != getContext().getRoot().getScope()) {
			out.append(enclosingScope).append(':');
		} else {
			out.append("$$");
		}
		out.append(getDisplayName());

		final Object declaredIn;

		if (this.key != null) {

			final Scope origin = this.key.getOrigin();

			if (origin != enclosingScope) {
				declaredIn = origin;
			} else {
				declaredIn = null;
			}
		} else {
			declaredIn = getDeclaration().getDeclaredIn();
		}

		if (declaredIn != null || isPropagated()) {
			out.append('{');
			if (declaredIn != null) {
				out.append(declaredIn);
			}
			if (isPropagated()) {
				if (declaredIn != null) {
					out.append(", ");
				}
				if (toField().isClone()) {
					out.append("clone");
				} else {
					out.append("propagated");
				}
			}
			out.append('}');
		}

		return out.toString();
	}

	@Override
	protected void merge(Member member) {

		final Field<?> field = member.toField();

		if (field == null) {
			getLogger().notFieldDeclaration(member);
		} else {
			toField().merge(member.toField());
		}
	}

	private MemberKey overrideField() {

		final Obj container = getContainer().toObject();
		final Obj declaredIn = declaredIn(container);

		final Member overridden = overridden(declaredIn);

		if (overridden != null) {
			this.visibility = overridden.getVisibility();
			return overridden.getKey();
		}

		this.visibility = Visibility.PRIVATE;

		return brokenMemberKey();
	}

	private Obj declaredIn(final Obj container) {

		final StaticTypeRef declaredInRef = getDeclaration().getDeclaredIn();

		if (declaredInRef != null) {
			return declaredInRef.getType();
		}

		final Sample[] samples = container.getSamples();

		for (Sample sample : samples) {
			if (sample.isExplicit()) {
				return sample.getType();
			}
		}

		final TypeRef explicitAncestor = container.getExplicitAncestor();

		if (explicitAncestor != null) {
			return explicitAncestor.getType();
		}

		return null;
	}

	private MemberKey declareNewField() {
		this.visibility = getDeclaration().getVisibility();
		return getId().key(getScope());
	}

	private Member overridden(Obj declaredIn) {
		if (declaredIn != null) {

			final Member result =
				declaredIn.member(getId(), Accessor.INHERITANT);

			if (result == null) {
				getLogger().cantOverrideUnknown(this, getDisplayName());
			}

			return result;
		}

		for (Sample sample : getContainer().toObject().getSamples()) {
			if (sample.isExplicit()) {
				continue;
			}

			final Obj type = sample.getType();
			final Member result =
				type.member(getId(), Accessor.INHERITANT);

			if (result != null) {
				return result;
			}
		}

		getLogger().cantOverrideUnknown(this, getDisplayName());

		return null;
	}

	static final class Overridden extends MemberField {

		private final Field<?> field;

		Overridden(
				Container container,
				Field<?> field,
				MemberField overridden) {
			super(container, overridden);
			this.field = field;
		}

		@Override
		public Field<?> toField() {
			return this.field;
		}

		@Override
		public boolean isPropagated() {
			return true;
		}

	}

}
