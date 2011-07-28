/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.object.impl.decl;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.*;


final class ReproducedObjectDefinition extends FieldDefinition {

	private final ObjectFieldVariant variant;
	private final Reproducer reproducer;

	ReproducedObjectDefinition(
			ObjectFieldVariant variant,
			Reproducer reproducer) {
		super(variant, reproducer.distribute());
		this.variant = variant;
		this.reproducer = reproducer;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		return this.variant.getField().getArtifactKind();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {

		final Ascendants oldAscendants = this.variant.getAscendants();
		final TypeRef oldAncestor = oldAscendants.getExplicitAncestor();

		if (oldAncestor != null) {

			final TypeRef newAncestor = oldAncestor.reproduce(this.reproducer);

			if (newAncestor != null) {
				definer.setAncestor(newAncestor);
			}
		}
		for (Sample oldSample : oldAscendants.getSamples()) {
			oldSample.reproduce(this.reproducer, definer);
		}

		definer.define(new ReproducedContent(
				this.variant.getContent(),
				this.reproducer));
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void defineArray(ArrayDefiner definer) {
		throw new UnsupportedOperationException();
	}

	private static final class ReproducedContent extends BlockBuilder {

		private final DeclarativeBlock content;
		private final Reproducer reproducer;

		ReproducedContent(DeclarativeBlock content, Reproducer reproducer) {
			super(content);
			this.content = content;
			this.reproducer = reproducer;
		}

		@Override
		public void buildBlock(Block<?> block) {

			final ContentReproducer reproducer = new ContentReproducer(
					this.content.getScope(),
					block.distribute(),
					this.reproducer);

			this.content.reproduceSentences(
					reproducer,
					block.toDeclarativeBlock());
		}

		@Override
		public String toString() {
			if (this.content == null) {
				return super.toString();
			}
			return this.content.toString();
		}

	}

	private static final class ContentReproducer extends Reproducer {

		private final Reproducer reproducer;

		public ContentReproducer(
				Scope reproducingScope,
				Distributor distributor,
				Reproducer reproducer) {
			super(reproducingScope, distributor);
			this.reproducer = reproducer;
		}

		@Override
		public Ref getPhrasePrefix() {
			return this.reproducer.getPhrasePrefix();
		}

		@Override
		public boolean phraseCreatesObject() {
			return this.reproducer.phraseCreatesObject();
		}

		@Override
		public MemberRegistry getMemberRegistry() {
			return this.reproducer.getMemberRegistry();
		}

		@Override
		public Statements<?> getStatements() {
			return null;
		}

		@Override
		public Reproducer reproducerOf(Scope reproducingScope) {
			if (getReproducingScope() == reproducingScope) {
				return this;
			}
			return this.reproducer.reproducerOf(reproducingScope);
		}

		@Override
		public void applyClause(
				LocationInfo location,
				Statements<?> statements,
				Clause clause) {
			throw new UnsupportedOperationException();
		}

	}

}
