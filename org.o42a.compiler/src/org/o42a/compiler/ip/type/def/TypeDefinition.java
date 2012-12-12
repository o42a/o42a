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

import static org.o42a.compiler.ip.type.def.TypeDefinitionVisitor.TYPE_DEFINITION_VISITOR;

import org.o42a.ast.Node;
import org.o42a.ast.phrase.TypeDefinitionNode;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.*;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.LogInfo;


public final class TypeDefinition
		extends Location
		implements ObjectTypeParameters {

	public static TypeDefinition typeDefinition(
			Node node,
			CompilerContext context,
			SentenceNode[] definitions) {
		return new TypeDefinition(
				new Location(context, node),
				definitions);
	}

	public static TypeDefinition typeDefinition(
			TypeDefinitionNode node,
			CompilerContext context) {
		return typeDefinition(
				node,
				context,
				node.getDefinition().getContent());
	}

	public static void redundantTypeParameters(
			CompilerLogger logger,
			LogInfo location) {
		logger.error(
				"redundant_type_parameters",
				location,
				"Redundant type parameters");
	}

	private final SentenceNode[] definitions;

	TypeDefinition(LocationInfo location, SentenceNode[] definitions) {
		super(location);
		this.definitions = definitions;
	}

	@Override
	public TypeParameters<?> refine(
			Obj object,
			TypeParameters<?> defaultParameters) {
		return buildTypeParameters(object, defaultParameters.getValueType())
				.refine(defaultParameters);
	}

	@Override
	public TypeDefinition prefixWith(PrefixPath prefix) {
		return this;
	}

	@Override
	public String toString() {
		if (this.definitions == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("#(");
		if (this.definitions.length != 0) {
			out.append('\n');
			for (SentenceNode definition : this.definitions) {
				out.append("  ");
				definition.printContent(out);
				out.append('\n');
			}
		}
		out.append(")");

		return out.toString();
	}

	private <T> TypeParameters<T> buildTypeParameters(
			Obj object,
			ValueType<T> valueType) {

		final TypeDefinitionBuilder builder =
				new TypeDefinitionBuilder(this, object);

		addDefinitions(builder);

		return builder.buildTypeParameters(valueType);
	}

	private void addDefinitions(TypeDefinitionBuilder builder) {
		for (SentenceNode sentence : this.definitions) {
			addSentence(builder, sentence);
		}
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

}
