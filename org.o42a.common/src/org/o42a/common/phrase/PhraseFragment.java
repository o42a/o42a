/*
    Compiler Commons
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
package org.o42a.common.phrase;

import org.o42a.common.phrase.part.PhraseContinuation;
import org.o42a.core.Scope;
import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;


class PhraseFragment extends PathFragment {

	private final Phrase phrase;

	PhraseFragment(Phrase phrase) {
		this.phrase = phrase;
	}

	public Phrase getPhrase() {
		return this.phrase;
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		// This is called before path resolution.
		return new Definition(this.phrase, defaultFieldDefinition(ref));
	}

	@Override
	public TypeRef iface(Ref ref) {
		return defaultInterface(ref);
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

		final Ref terminated =
				ref.getPath()
				.append(context.getOutcome())
				.target(ref.distribute());
		final PhraseContinuation nextPart = context.getNextPart();

		if (nextPart == null) {
			return terminated.getPath().getPath();
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
		public void init(Field field, Ascendants implicitAscendants) {
			this.phrase.getMainContext().setImplicitAscendants(implicitAscendants);
		}

		@Override
		public DefinitionTarget getDefinitionTarget() {
			return this.definition.getDefinitionTarget();
		}

		@Override
		public void defineObject(ObjectDefiner definer) {
			this.phrase.getMainContext().setImplicitAscendants(
					definer.getImplicitAscendants());
			this.definition.defineObject(definer);
		}

		@Override
		public void overrideObject(ObjectDefiner definer) {
			this.phrase.getMainContext().setImplicitAscendants(
					definer.getImplicitAscendants());
			this.definition.overrideObject(definer);
		}

		@Override
		public void defineMacro(MacroDefiner definer) {
			this.definition.defineMacro(definer);
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
