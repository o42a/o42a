/*
    Compiler
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
package org.o42a.compiler.ip.phrase.ref;

import static org.o42a.compiler.ip.ref.owner.MayDereferenceFragment.mayDereference;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.common.macro.Macros;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.part.*;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.value.TypeParameters;
import org.o42a.util.string.Name;


public class Phrase extends Placed {

	private final Interpreter ip;
	private PhrasePrefix prefix;
	private PhrasePart last;
	private MainPhraseContext mainContext;
	private boolean macroExpansion;
	private boolean bodyRef;

	public Phrase(
			Interpreter ip,
			LocationInfo location,
			Distributor distributor) {
		super(location, distributor);
		this.ip = ip;
	}

	public final Interpreter ip() {
		return this.ip;
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
			setBodyRef(true);
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

	public final TypeParameters getTypeParameters() {
		return this.prefix.getTypeParameters();
	}

	public final Phrase setTypeParameters(TypeParameters typeParameters) {
		this.prefix.setTypeParameters(typeParameters);
		return setBodyRef(true);
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

	public final boolean isBodyRef() {
		return this.bodyRef;
	}

	public final Phrase setBodyRef(boolean bodyRef) {
		this.bodyRef = bodyRef;
		return this;
	}

	public final PhraseName name(LocationInfo location, Name name) {
		return append(this.last.name(location, name));
	}

	public final PhraseArgument emptyArgument(LocationInfo location) {
		return append(this.last.argument(location, null));
	}

	public final PhraseArgument argument(Ref value) {
		value.assertSameScope(this);
		return append(this.last.argument(value, value));
	}

	public final PhraseArray array(ArrayConstructor array) {
		array.assertSameScope(this);
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

	public final UnaryPhrasePart unary(UnaryNode node) {
		return append(this.last.unary(node));
	}

	public final BinaryPhrasePart binary(BinaryNode node) {
		return append(this.last.binary(node));
	}

	public final PhraseAssignment assign(AssignmentNode node) {
		return append(this.last.assign(node));
	}

	public final OperandPhrasePart operand(Ref value) {
		return append(this.last.operand(value));
	}

	public final Ref toRef() {

		final BoundPath path =
				new PhraseFragment(this).toPath().bind(this, getScope());

		if (isMacroExpansion()) {
			return Macros.expandMacro(path).target(distribute());
		}

		final Ref target = path.target(distribute());

		if (isBodyRef()) {
			return target;
		}

		return mayDereference(target);
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

		final Phrase newPhrase = new Phrase(this.ip, this, distribute());

		newPhrase.setAncestor(prefix.toTypeRef());
		newPhrase.prefix.append(nextPart);
		newPhrase.last = this.last;
		newPhrase.macroExpansion = isMacroExpansion();
		newPhrase.setBodyRef(isBodyRef());

		return newPhrase;
	}

	@Override
	public String toString() {
		return this.prefix.phraseString();
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
