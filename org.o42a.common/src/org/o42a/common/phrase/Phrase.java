/*
    Compiler Commons
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.common.ref.MayDereferenceFragment.mayDereference;

import org.o42a.common.macro.Macros;
import org.o42a.common.phrase.part.*;
import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.util.string.Name;


public class Phrase extends Contained {

	private CompilerLogger resolutionLogger;
	private SuffixedByPhrase suffixed;
	private PhrasePrefix prefix;
	private PhrasePart last;
	private MainPhraseContext mainContext;
	private Ref ref;
	private boolean macroExpansion;
	private boolean bodyReferred;

	public Phrase(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public final CompilerLogger getResolutionLogger() {
		return this.resolutionLogger != null
				? this.resolutionLogger : getLogger();
	}

	public final boolean hasResolutionLogger() {
		return this.resolutionLogger != null;
	}

	public final Phrase setResolutionLogger(CompilerLogger resolutionLogger) {
		this.resolutionLogger = resolutionLogger;
		return this;
	}

	public final PhrasePrefix getPrefix() {
		return this.prefix;
	}

	public final TypeRef getAncestor() {
		return this.prefix != null ? this.prefix.getAncestor() : null;
	}

	public final Phrase setImpliedAncestor(LocationInfo location) {
		if (this.prefix == null) {
			this.last = this.prefix = new PhrasePrefix(location, this);
			referBody();
		}
		return this;
	}

	public final Phrase setAncestor(TypeRef ancestor) {
		if (this.prefix == null) {
			this.last = this.prefix = new PhrasePrefix(ancestor, this);
		}
		this.prefix.setAncestor(ancestor);
		return this;
	}

	public final ObjectTypeParameters getTypeParameters() {
		return this.prefix.getTypeParameters();
	}

	public final Phrase setTypeParameters(ObjectTypeParameters typeParameters) {
		if (getTypeParameters() != null) {
			getLogger().error(
					"duplicate_type_parameters",
					typeParameters,
					"Type parameters already set");
			return this;
		}
		this.prefix.setTypeParameters(typeParameters);
		return referBody();
	}

	public final StaticTypeRef[] getSamples() {
		if (this.prefix == null) {
			return new StaticTypeRef[0];
		}
		return this.prefix.getSamples();
	}

	public final Phrase addSamples(StaticTypeRef... samples) {
		this.prefix.addSamples(samples);
		return this;
	}

	public final Ascendants getImplicitAscendants() {
		return getMainContext().getImplicitAscendants();
	}

	public final boolean isMacroExpansion() {
		return this.macroExpansion;
	}

	public final Phrase expandMacro() {
		this.macroExpansion = true;
		return this;
	}

	public final boolean isBodyReferred() {
		return this.bodyReferred;
	}

	public final Phrase referBody() {
		this.bodyReferred = true;
		return this;
	}

	public final SuffixedByPhrase suffix(
			LocationInfo location,
			RefBuilder prefix) {
		return append(this.suffixed = this.last.suffix(location, prefix));
	}

	public final PhraseName name(LocationInfo location, Name name) {
		return append(this.last.name(location, name));
	}

	public final PhraseArgument emptyArgument(LocationInfo location) {
		return append(this.last.argument(location, null));
	}

	public final PhraseArgument argument(RefBuilder value) {
		return append(this.last.argument(value, value));
	}

	public final PhraseArray array(RefBuilder array) {
		return append(this.last.array(array));
	}

	public final PhraseString string(LocationInfo location, String string) {
		return append(this.last.string(location, string));
	}

	public final PhraseDeclarations declarations(BlockBuilder declarations) {
		return append(this.last.declarations(declarations));
	}

	public final PhraseImperative imperative(BlockBuilder imperatives) {
		return append(this.last.imperative(imperatives));
	}

	public final IntervalBound interval(
			LocationInfo leftLocation,
			RefBuilder leftBound,
			boolean leftOpen,
			LocationInfo rightLocation,
			RefBuilder rightBound,
			boolean rightOpen) {
		return append(
				this.last.interval(
						leftLocation,
						leftBound,
						leftOpen,
						rightLocation,
						rightBound,
						rightOpen));
	}

	public final HalfBoundedInterval halfBoundedInterval(
			LocationInfo location,
			RefBuilder bound,
			boolean open,
			boolean leftBounded) {
		return append(
				this.last.halfBoundedInterval(
						location,
						bound,
						open,
						leftBounded));
	}

	public final UnboundedInterval unboundedInterval(LocationInfo location) {
		return append(this.last.unboundedInterval(location));
	}

	public final UnaryPhrasePart unary(
			LocationInfo location,
			UnaryPhraseOperator operator) {
		return append(this.last.unary(location, operator));
	}

	public final BinaryPhrasePart binary(
			LocationInfo location,
			BinaryPhraseOperator operator,
			RefBuilder rightOperand) {
		return append(this.last.binary(location, operator, rightOperand));
	}

	public final PhraseAssignment assign(
			LocationInfo location,
			RefBuilder value) {
		return append(this.last.assign(location, value));
	}

	public final Ref toRef() {
		if (this.ref != null) {
			return this.ref;
		}

		final BoundPath path =
				new PhraseFragment(this).toPath().bind(this, getScope());

		if (isMacroExpansion()) {
			return this.ref = Macros.expandMacro(path).target(distribute());
		}

		final Ref target = path.target(distribute());

		if (isBodyReferred()) {
			return this.ref = target;
		}

		return this.ref = mayDereference(target);
	}

	public final void build() {
		createsObject();
	}

	public final boolean createsObject() {
		return getMainContext().createsObject();
	}

	public final Ref substitutePrefix(Distributor distributor) {

		final TypeRef ancestor = getAncestor();

		if (ancestor == null) {
			getLogger().error(
					"missing_phrase_prefix",
					this,
					"Phrase requires prefix");
			return null;
		}

		return ancestor.getRef().rescope(distributor.getScope());
	}

	public Phrase asPrefix(Ref prefix, PhraseContinuation nextPart) {

		final Phrase newPhrase = new Phrase(this, distribute());

		newPhrase.setAncestor(prefix.toTypeRef());
		newPhrase.prefix.append(nextPart);
		newPhrase.last = this.last;
		newPhrase.macroExpansion = isMacroExpansion();
		newPhrase.bodyReferred = isBodyReferred();

		return newPhrase;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		PhrasePart part;

		if (this.suffixed != null) {
			out.append(this.suffixed).append(getPrefix());
			part = this.suffixed.getFollowing();
		} else {
			part = getPrefix();
			out.append(part);
			part = part.getFollowing();
		}

		while (part != null) {
			out.append(' ').append(part);
			part = part.getFollowing();
		}

		return out.toString();
	}

	final MainPhraseContext getMainContext() {
		if (this.mainContext != null) {
			return this.mainContext;
		}
		return this.mainContext = new MainPhraseContext(this);
	}

	private final <P extends PhrasePart> P append(P part) {
		this.last = part;
		return part;
	}

}
