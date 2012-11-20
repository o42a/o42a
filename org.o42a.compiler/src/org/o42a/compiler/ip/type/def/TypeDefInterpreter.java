/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.type.def;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.type.def.TypeDefinitionVisitor.TYPE_DEFINITION_VISITOR;

import org.o42a.ast.phrase.TypeDefinitionNode;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.value.TypeParametersBuilder;


public class TypeDefInterpreter {

	public static TypeParametersBuilder typeDefinition(
			TypeDefinitionNode node,
			Distributor distributor,
			Nesting nesting,
			TypeConsumer consumer) {

		TypeDefinition definition = new TypeDefinition(
				location(distributor, node),
				distributor,
				nesting,
				consumer);

		for (SentenceNode sentence : node.getDefinition().getContent()) {
			definition = addSentence(definition, sentence);
		}

		return definition;
	}

	private static TypeDefinition addSentence(
			TypeDefinition definition,
			SentenceNode sentence) {
		if (sentence.getType() != SentenceType.PROPOSITION) {
			definition.getLogger().error(
					"prohibited_type_definition_sentence_type",
					sentence.getMark(),
					"Only propositions allowed within type definition");
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length == 0) {
			return definition;
		}
		if (disjunction.length > 1) {
			definition.getLogger().error(
					"prohibited_type_definition_disjunction",
					disjunction[1].getSeparator(),
					"Disjunctions prohibited within type definition");
		}

		return addAlt(definition, disjunction[0]);
	}

	private static TypeDefinition addAlt(
			TypeDefinition definition,
			AlternativeNode alt) {

		final SerialNode[] conjunction = alt.getConjunction();

		if (conjunction.length > 1) {
			definition.getLogger().error(
					"prohibited_type_definition_conjunction",
					conjunction[1].getSeparator(),
					"Conjunctions prohibited within type definition");
		}

		TypeDefinition result = definition;

		for (SerialNode node : conjunction) {

			final StatementNode statement = node.getStatement();

			if (statement == null) {
				continue;
			}
			result = statement.accept(TYPE_DEFINITION_VISITOR, result);
		}

		return result;
	}

	private TypeDefInterpreter() {
	}

}
