/*
    Compiler Core
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
package org.o42a.core.st.sentence;

import org.o42a.core.Container;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public final class Declaratives extends Statements<Declaratives> {

	Declaratives(LocationInfo location, DeclarativeSentence sentence) {
		super(location, sentence);
	}

	@Override
	public final DeclarativeSentence getSentence() {
		return (DeclarativeSentence) super.getSentence();
	}

	@Override
	public final DeclarativeFactory getSentenceFactory() {
		return super.getSentenceFactory().toDeclarativeFactory();
	}

	@Override
	public FieldBuilder field(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		if (getSentence().isConditional()) {
			getLogger().error(
					"prohibited_conditional_field",
					declaration,
					"Field declaration can not be conditional");
			dropStatement();
			return null;
		}
		return super.field(declaration, definition);
	}

	@Override
	public final DeclarativeBlock parentheses(LocationInfo location) {
		return super.parentheses(location).toDeclarativeBlock();
	}

	@Override
	public final DeclarativeBlock parentheses(
			LocationInfo location,
			Container container) {
		return super.parentheses(location, container).toDeclarativeBlock();
	}

	@Override
	public void loop(LocationInfo location, Name name) {
		dropStatement();
		getLogger().error(
				"prohibited_declarative_loop",
				location,
				"Loops are only allowed within imperative blocks");
	}

	@Override
	protected void braces(ImperativeBlock braces) {
		statement(braces);
	}

}
