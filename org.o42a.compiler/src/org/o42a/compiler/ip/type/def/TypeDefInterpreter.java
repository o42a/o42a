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

import org.o42a.ast.Node;
import org.o42a.ast.phrase.TypeDefinitionNode;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogInfo;


public class TypeDefInterpreter {

	public static TypeDefinition typeDefinition(
			TypeDefinitionNode node,
			Distributor distributor,
			TypeConsumer consumer) {
		return typeDefinition(
				node,
				node.getDefinition().getContent(),
				distributor,
				consumer);
	}

	public static TypeDefinition typeDefinition(
			Node node,
			SentenceNode[] definitions,
			Distributor distributor,
			TypeConsumer consumer) {

		final TypeDefinitionBuilder builder = new TypeDefinitionBuilder(
				location(distributor, node),
				distributor,
				consumer);
		for (SentenceNode sentence : definitions) {
			addSentence(builder, sentence);
		}

		return builder.buildDefinition();
	}

	public static void redundantTypeParameters(
			CompilerLogger logger,
			LogInfo location) {
		logger.error(
				"redundant_type_parameters",
				location,
				"Redundant type parameters");
	}

	private static void addSentence(
			TypeDefinitionBuilder builder,
			SentenceNode sentence) {
		if (sentence.getType() != SentenceType.PROPOSITION) {
			builder.getLogger().error(
					"prohibited_type_definition_sentence_type",
					sentence.getMark(),
					"Only propositions allowed within type definition");
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length == 0) {
			return;
		}
		if (disjunction.length > 1) {
			builder.getLogger().error(
					"prohibited_type_definition_disjunction",
					disjunction[1].getSeparator(),
					"Disjunctions prohibited within type definition");
		}

		addAlt(builder, disjunction[0]);
	}

	private static void addAlt(
			TypeDefinitionBuilder builder,
			AlternativeNode alt) {

		final SerialNode[] conjunction = alt.getConjunction();

		if (conjunction.length > 1) {
			builder.getLogger().error(
					"prohibited_type_definition_conjunction",
					conjunction[1].getSeparator(),
					"Conjunctions prohibited within type definition");
		}

		for (SerialNode node : conjunction) {

			final StatementNode statement = node.getStatement();

			if (statement == null) {
				continue;
			}
			statement.accept(TYPE_DEFINITION_VISITOR, builder);
		}
	}

	private TypeDefInterpreter() {
	}

}
