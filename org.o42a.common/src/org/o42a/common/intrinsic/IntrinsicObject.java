/*
    Modules Commons
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
package org.o42a.common.intrinsic;

import org.o42a.core.*;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.*;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.log.Loggable;


public abstract class IntrinsicObject extends PlainObject {

	private ObjectMemberRegistry memberRegistry;
	private DeclarativeBlock definition;

	public IntrinsicObject(FieldDeclaration declaration) {
		super(new IntrinsicField(declaration));
		((IntrinsicField) getScope()).init(this);
	}

	@SuppressWarnings("unchecked")
	public final Field<Obj> getField() {
		return (Field<Obj>) getScope();
	}

	@Override
	public String toString() {
		return getScope().toString();
	}

	@Override
	protected final Ascendants buildAscendants() {

		Ascendants ascendants = createAscendants();
		final Field<Obj> field = getField();

		if (field.isOverride()) {

			final ObjectType containerType =
				getScope().getEnclosingContainer().toObject()
				.type().useBy(ascendants);
			final TypeRef ancestor = containerType.getAncestor();

			if (ancestor != null) {

				final Member overridden =
					ancestor.type(ascendants)
					.getObject().member(field.getKey());

				if (overridden != null) {
					ascendants = ascendants.addMemberOverride(overridden);
				}
			}

			final Sample[] containerSamples = containerType.getSamples();

			for (int i = containerSamples.length - 1; i >= 0; --i) {

				final Member overridden =
					containerSamples[i].type(ascendants)
					.getObject().member(field.getKey());

				if (overridden != null) {
					ascendants = ascendants.addMemberOverride(overridden);
				}
			}
		} else {

			final AdapterId adapterId =
				field.toMember().getId().getAdapterId();

			if (adapterId != null) {
				ascendants = ascendants.addExplicitSample(adapterId.adapterType(
						ascendants.getScope().getEnclosingScope()));
			}
		}

		return ascendants;
	}

	protected abstract Ascendants createAscendants();

	@Override
	protected void declareMembers(ObjectMembers members) {
		if (this.definition != null) {
			this.memberRegistry.registerMembers(members);
		}
	}

	@Override
	protected void updateMembers() {
		if (this.definition != null) {
			this.definition.executeInstructions();
		}
	}

	protected void includeSource() {

		final BlockBuilder compiled = getContext().compileBlock();

		this.memberRegistry = new ObjectMemberRegistry(this);

		this.definition = new DeclarativeBlock(
				this,
				new DefinitionDistributor(this),
				this.memberRegistry);

		compiled.buildBlock(this.definition);
		this.definition.executeInstructions();
	}

	private static final class IntrinsicField extends ObjectField {

		IntrinsicField(FieldDeclaration declaration) {
			super(declaration);
		}

		private IntrinsicField(
				Container enclosingContainer,
				IntrinsicField sample) {
			super(enclosingContainer, sample);
		}

		@Override
		public Obj getArtifact() {
			return getScopeArtifact();
		}

		@Override
		protected IntrinsicField propagate(Scope enclosingScope) {
			return new IntrinsicField(enclosingScope.getContainer(), this);
		}

		private final void init(IntrinsicObject object) {
			setScopeArtifact(object);
		}

	}

	private static final class DefinitionDistributor extends Distributor {

		private final IntrinsicObject object;
		private final Namespace namespace;

		DefinitionDistributor(IntrinsicObject object) {
			this.object = object;
			this.namespace = new Namespace(this, this.object);
		}

		@Override
		public Loggable getLoggable() {
			return this.object.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.object.getContext();
		}

		@Override
		public ScopePlace getPlace() {
			return this.object.getPlace();
		}

		@Override
		public Container getContainer() {
			return this.namespace;
		}

		@Override
		public Scope getScope() {
			return this.object.getScope();
		}

	}
}
