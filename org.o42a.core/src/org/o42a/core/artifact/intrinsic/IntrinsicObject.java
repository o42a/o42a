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
package org.o42a.core.artifact.intrinsic;

import org.o42a.core.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.*;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;


public abstract class IntrinsicObject extends PlainObject {

	private ObjectMemberRegistry fieldRegistry;

	public IntrinsicObject(
			Container enclosingContainer,
			String name) {
		super(new IntrinsicField(enclosingContainer, name));
		((IntrinsicField) getScope()).init(this);
	}

	public IntrinsicObject(FieldDeclaration declarator) {
		super(new IntrinsicField(declarator));
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

		final Ascendants ascendants = createAscendants();
		final Field<Obj> field = getField();

		if (field.isOverride()) {

			final Obj container = getScope().getEnclosingContainer().toObject();
			final TypeRef ancestor = container.getAncestor();

			if (ancestor != null) {

				final Member overridden =
					ancestor.getType().member(field.getKey());

				if (overridden != null) {
					ascendants.addMemberOverride(overridden);
				}
			}

			final Sample[] containerSamples = container.getSamples();

			for (int i = containerSamples.length - 1; i >= 0; --i) {

				final Member overridden =
					containerSamples[i].getType().member(field.getKey());

				if (overridden != null) {
					ascendants.addMemberOverride(overridden);
				}
			}
		} else {

			final AdapterId adapterId =
				field.toMember().getId().getAdapterId();

			if (adapterId != null) {
				ascendants.addExplicitSample(
						adapterId.adapterType(ascendants.getScope()));
			}
		}

		return ascendants;
	}

	protected abstract Ascendants createAscendants();

	@Override
	protected void declareMembers(ObjectMembers members) {
		getFieldRegistry().registerMembers(members);
	}

	protected ObjectMemberRegistry getFieldRegistry() {
		if (this.fieldRegistry == null) {
			this.fieldRegistry = new ObjectMemberRegistry(this);
		}
		return this.fieldRegistry;
	}

	protected void includeSource(String source) {

		final CompilerContext context;

		try {
			context = getContext().contextFor(source);
		} catch (Exception e) {
			getLogger().unavailableSource(
					this,
					source,
					e.getLocalizedMessage());
			return;
		}

		final BlockBuilder compiled = context.compileBlock();
		final DeclarativeBlock definition = new DeclarativeBlock(
				new Location(context, compiled.getNode()),
				new DefinitionDistributor(this),
				getFieldRegistry());

		compiled.buildBlock(definition);
	}

	private static final class IntrinsicField extends ObjectField {

		IntrinsicField(FieldDeclaration declarator) {
			super(declarator);
		}

		IntrinsicField(Container enclosingContainer, String name) {
			super(enclosingContainer, name);
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
			this.namespace = new Namespace(this.object);
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
