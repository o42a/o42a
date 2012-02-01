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
package org.o42a.core.member.field;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.impl.prediction.FieldPrediction.predictField;

import org.o42a.codegen.Generator;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ScopePlace;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.ArtifactScope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.member.*;
import org.o42a.core.member.impl.field.FieldContainer;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.log.Loggable;


public abstract class Field<A extends Artifact<A>> extends ArtifactScope<A> {

	public static Field<?> fieldOf(Scope scope) {

		final Field<?> field = scope.toField();

		if (field != null) {
			return field;
		}

		final Obj object = scope.toObject();

		return object.getMaterializationOf().getScope().toField();
	}

	private final MemberField member;
	private Path enclosingScopePath;
	private Field<A>[] overridden;

	private MemberContainer container;
	private FieldIR<A> ir;

	public Field(MemberField member) {
		this.member = member;
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
		if (this.enclosingScopePath != null) {
			return this.enclosingScopePath;
		}
		if (getEnclosingContainer().getScope().isTopScope()) {
			return null;
		}

		final Obj object = getArtifact().toObject();

		if (object == null) {
			return null;
		}

		return this.enclosingScopePath = object.scopePath();
	}

	@Override
	public final MemberContainer getContainer() {
		if (this.container != null) {
			return this.container;
		}

		final Obj object = getArtifact().toObject();

		if (object != null) {
			return this.container = object;
		}

		return this.container = new FieldContainer(this);
	}

	public boolean isScopeField() {
		return false;
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
		final Field<A> original =
				member.toField().field(dummyUser()).toKind(getArtifactKind());

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

	@Override
	public final Field<A> getFirstDeclaration() {
		return this.member.getFirstDeclaration().toField().field(dummyUser())
				.toKind(getArtifactKind());
	}

	/**
	 * The last definition of this field.
	 *
	 * @return the last field's explicit definition or implicit definition
	 * with multiple inheritance.
	 */
	@Override
	public final Field<A> getLastDefinition() {
		return this.member.getLastDefinition().toField().field(dummyUser())
				.toKind(getArtifactKind());
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
	@Override
	public final boolean isClone() {
		return toMember().isClone();
	}

	public Field<A>[] getOverridden() {
		if (this.overridden == null) {
			this.overridden = overriddenFields();
		}
		return this.overridden;
	}

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
		return this.member.isAdapter();
	}

	public final boolean isAbstract() {
		return this.member.isAbstract();
	}

	public final boolean isPrototype() {
		return this.member.isPrototype();
	}

	public final boolean isOverride() {
		return this.member.isOverride();
	}

	@Override
	public final Prediction predict(Prediction enclosing) {
		return predictField(enclosing, this);
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}

		final Obj object1 = toObject();

		if (object1 != null) {

			final Obj object2 = other.toObject();

			if (object2 != null) {
				return object1.type().derivedFrom(object2.type());
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

	protected void merge(Field<?> field) {
		throw new UnsupportedOperationException(
				"Field " + this + " can not have variants");
	}

	protected FieldIR<A> createIR(Generator generator) {
		return getArtifact().getKind().fieldIR(generator, this);
	}

	@SuppressWarnings("unchecked")
	private Field<A>[] overriddenFields() {

		final ArtifactKind<A> artifactKind = getArtifactKind();
		final Member[] overriddenMembers = this.member.getOverridden();
		final Field<A>[] overridden = new Field[overriddenMembers.length];

		for (int i = 0; i < overridden.length; ++i) {
			overridden[i] =
					overriddenMembers[i]
					.toField()
					.field(dummyUser())
					.toKind(artifactKind);
		}

		return overridden;
	}

}
