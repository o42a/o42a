/*
    Compiler
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
package org.o42a.compiler.ip.phrase.ref;

import org.o42a.compiler.ip.phrase.part.PhraseContinuation;
import org.o42a.compiler.ip.phrase.part.PhraseTerminator;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.LinkDefiner;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;


class PhraseFragment extends PathFragment {

	private final Phrase phrase;

	PhraseFragment(Phrase phrase) {
		this.phrase = phrase;
	}

	public Phrase getPhrase() {
		return this.phrase;
	}

	@Override
	public FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		// This is called before path resolution.
		return new Definition(
				this.phrase,
				super.fieldDefinition(path, distributor));
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {
		return buildPath(this.phrase);
	}

	@Override
	public String toString() {
		return this.phrase.toString();
	}

	private Path buildPath(Phrase phrase) {

		final MainPhraseContext context = phrase.getMainContext();
		final Ref ref;

		if (!context.createsObject()) {
			ref = context.standalone();
		} else {
			ref = new PhraseConstructor(phrase).toRef();
		}

		final Ref result =
				ref.getPath()
				.append(context.getOutcome())
				.target(ref.distribute());
		final PhraseTerminator terminator = context.getTerminator();
		final Ref terminated;

		if (terminator == null) {
			terminated = result;
		} else {
			terminated = terminator.terminate(result);
		}

		final PhraseContinuation nextPart = context.getNextPart();

		if (nextPart == null) {
			return terminated.getPath().getRawPath();
		}

		return buildPath(phrase.asPrefix(terminated, nextPart));
	}

	private static final class Definition extends FieldDefinition {

		private final Phrase phrase;
		private final FieldDefinition definition;

		Definition(Phrase phrase, FieldDefinition definition) {
			super(definition, definition.distribute());
			this.phrase = phrase;
			this.definition = definition;
		}

		@Override
		public ArtifactKind<?> determineArtifactKind() {
			return this.definition.determineArtifactKind();
		}

		@Override
		public void defineObject(ObjectDefiner definer) {
			this.phrase.getMainContext().setImplicitAscendants(
					definer.getImplicitAscendants());
			this.definition.defineObject(definer);
		}

		@Override
		public void defineLink(LinkDefiner definer) {
			this.definition.defineLink(definer);
		}

		@Override
		public String toString() {
			if (this.definition == null) {
				return super.toString();
			}
			return this.definition.toString();
		}

	}

}
