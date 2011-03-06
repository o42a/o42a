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
package org.o42a.core.member.field;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.Path;
import org.o42a.util.log.Loggable;


public abstract class Field<A extends Artifact<A>> extends AbstractScope {

	private final MemberField member;
	private A artifact;
	private Path enclosingScopePath;
	private Field<A>[] overridden;

	private Container container;
	private FieldIR<A> ir;

	public Field(MemberField member) {
		this.member = member;
	}

	protected Field(Container enclosingContainer, Field<A> overridden) {
		this(enclosingContainer, overridden, true);
		setScopeArtifact(propagateArtifact(overridden));
	}

	protected Field(
			Container enclosingContainer,
			Field<A> overridden,
			boolean propagate) {
		this.member = new MemberField.Overridden(
				enclosingContainer,
				this,
				overridden.toMember(),
				propagate);
	}

	@Override
	public final CompilerContext getContext() {
		return this.member.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.member.getLoggable();
	}

	@Override
	public final ScopePlace getPlace() {
		return this.member.getPlace();
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.member.getContainer();
	}

	@Override
	public Path getEnclosingScopePath() {
		if (this.enclosingScopePath == null) {
			if (getEnclosingContainer().getScope().isTopScope()) {
				return null;
			}

			final Obj object = getScopeArtifact().toObject();

			if (object == null) {
				return null;
			}

			this.enclosingScopePath = object.scopePath();
		}

		return this.enclosingScopePath;
	}

	@Override
	public final Container getContainer() {
		if (this.container != null) {
			return this.container;
		}

		final Obj object = getArtifact().toObject();

		if (object != null) {
			return this.container = object;
		}

		return this.container = new FieldContainer(this);
	}

	public final boolean isLocal() {
		return getEnclosingContainer().toLocal() != null;
	}

	public final boolean isPropagated() {
		return this.member.isPropagated();
	}

	public final MemberKey getKey() {
		return this.member.getKey();
	}

	public ArtifactKind<A> getArtifactKind() {
		return getArtifact().getKind();
	}

	/**
	 * The first field declaration.
	 *
	 * @return field, first declared by <code>:=</code>.
	 */
	public Field<A> getOriginal() {

		final MemberKey key = getKey();
		final Member member = key.getOrigin().getContainer().member(key);
		final Field<A> original = member.toField().toKind(getArtifactKind());

		assert original.getArtifact().getKind() == getArtifact().getKind() :
			"Wrong " + this + " artifact kind: " + getArtifact().getKind()
			+ ", while original had " + original.getArtifact().getKind();

		return original;
	}

	/**
	 * The scope this field's definition were assigned in.
	 *
	 * @return the {@link #getLastDefinition() last definition} scope.
	 */
	public final Scope getDefinedIn() {
		return this.member.getDefinedIn();
	}

	/**
	 * The last definition of this field.
	 *
	 * @return the last field's explicit definition or implicit definition
	 * with multiple inheritance.
	 */
	@SuppressWarnings("unchecked")
	public final Field<A> getLastDefinition() {
		return (Field<A>) this.member.getLastDefinition().toField();
	}

	/**
	 * Checks whether this field is a clone.
	 *
	 * <p>Field is clone if it is not explicitly defined and overrides
	 * only one field.</p>
	 *
	 * @return <code>true</code> if field is a clone or <code>false</code>
	 * otherwise.
	 *
	 * @see #getLastDefinition()
	 */
	public final boolean isClone() {
		return getLastDefinition() != this;
	}

	public Field<A>[] getOverridden() {
		if (this.overridden == null) {
			this.overridden = overriddenFields();
		}
		return this.overridden;
	}

	public abstract A getArtifact();

	@Override
	public final MemberField toMember() {
		return this.member;
	}

	@Override
	public final Field<A> toField() {
		return this;
	}

	@SuppressWarnings("unchecked")
	public final <K extends Artifact<K>> Field<K> toKind(ArtifactKind<K> kind) {
		if (getArtifactKind().is(kind)) {
			return (Field<K>) this;
		}
		return null;
	}

	public String getDisplayName() {
		return this.member.getDisplayName();
	}

	public final FieldDeclaration getDeclaration() {
		return this.member.getDeclaration();
	}

	public final Visibility getVisibility() {
		return this.member.getVisibility();
	}

	public final boolean isAdapter() {
		return getDeclaration().isAdapter();
	}

	public final boolean isAbstract() {
		return this.member.isAbstract();
	}

	public final boolean isPrototype() {
		return getDeclaration().isPrototype();
	}

	public final boolean isOverride() {
		return this.member.isOverride();
	}

	public final Field<A> propagateTo(Scope scope) {
		if (getEnclosingScope() == scope) {
			return this;
		}
		scope.assertDerivedFrom(getEnclosingScope());
		return propagate(scope);
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		final Obj object1 = getContainer().toObject();

		if (object1 != null) {

			final Obj object2 = other.getContainer().toObject();

			if (object2 != null) {
				return object1.derivedFrom(object2);
			}
		}

		final Field<?> field2 = other.toField();

		if (field2 == null) {
			return false;
		}

		return getKey().equals(field2.getKey());
	}

	@Override
	public final FieldIR<A> ir(Generator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = createIR(generator);
		}
		return this.ir;
	}

	@Override
	public String toString() {
		return this.member.toString();
	}

	protected void setScopeArtifact(A artifact) {
		this.artifact = artifact;
	}

	protected A getScopeArtifact() {
		return this.artifact;
	}

	protected abstract Field<A> propagate(Scope enclosingScope);

	protected abstract A propagateArtifact(Field<A> overridden);

	protected void merge(Field<?> field) {
		throw new UnsupportedOperationException(
				"Field " + this + " can not have variants");
	}

	protected FieldIR<A> createIR(Generator generator) {
		return getArtifact().getKind().fieldIR(generator, this);
	}

	@SuppressWarnings("unchecked")
	private Field<A>[] overriddenFields() {

		final Member[] overriddenMembers = this.member.getOverridden();
		final Field<A>[] overridden = new Field[overriddenMembers.length];

		for (int i = 0; i < overridden.length; ++i) {
			overridden[i] = (Field<A>) overriddenMembers[i].toField();
		}

		return overridden;
	}

	private static final class FieldContainer extends AbstractContainer {

		private final Field<?> field;

		FieldContainer(Field<?> field) {
			super(field);
			this.field = field;
		}

		@Override
		public Scope getScope() {
			return this.field;
		}

		@Override
		public Container getEnclosingContainer() {
			return this.field.getEnclosingContainer();
		}

		@Override
		public Member toMember() {
			return this.field.toMember();
		}

		@Override
		public Artifact<?> toArtifact() {
			return this.field.getArtifact();
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
		public LocalScope toLocal() {
			return null;
		}

		@Override
		public Namespace toNamespace() {
			return null;
		}

		@Override
		public Member member(MemberKey memberKey) {
			return null;
		}

		@Override
		public Path member(ScopeInfo user, MemberId memberId, Obj declaredIn) {
			return null;
		}

		@Override
		public Path findMember(
				ScopeInfo user,
				MemberId memberId,
				Obj declaredIn) {
			return null;
		}

	}

}
