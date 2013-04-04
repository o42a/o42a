/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.compiler.ip.type.TypeConsumer.typeConsumer;
import static org.o42a.compiler.ip.type.def.TypeDefinitionAccessRules.ACCESS_FROM_TYPE;
import static org.o42a.compiler.ip.type.def.TypeDefinitionVisitor.TYPE_DEFINITION_VISITOR;
import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.*;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.type.MemberTypeParameter;
import org.o42a.core.object.Obj;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;


public class TypeDefinitionBuilder
		extends AbstractContainer
		implements ContainerInfo {

	private static final TypeParameterDeclaration[] NO_PARAMETERS =
			new TypeParameterDeclaration[0];

	private final Obj object;
	private final TypeConsumer consumer;
	private TypeParameterDeclaration[] parameters = NO_PARAMETERS;

	public TypeDefinitionBuilder(LocationInfo location, Obj object) {
		super(location);
		this.object = object;
		this.consumer = typeConsumer(null);
	}

	@Override
	public final Scope getScope() {
		return this.object.getScope();
	}

	@Override
	public final Container getContainer() {
		return this;
	}

	@Override
	public final Container getEnclosingContainer() {
		return this.object.getEnclosingContainer();
	}

	public final TypeConsumer getConsumer() {
		return this.consumer;
	}

	public final TypeParameterDeclaration[] getParameters() {
		return this.parameters;
	}

	@Override
	public final Member toMember() {
		return this.object.toMember();
	}

	@Override
	public final Obj toObject() {
		return this.object;
	}

	@Override
	public final Clause toClause() {
		return this.object.toClause();
	}

	@Override
	public Namespace toNamespace() {
		return null;
	}

	@Override
	public Member member(MemberKey memberKey) {
		return this.object.member(memberKey);
	}

	@Override
	public MemberPath member(Access access, MemberId memberId, Obj declaredIn) {
		return findTypeParameter(memberId, declaredIn);
	}

	@Override
	public MemberPath findMember(
			Access access,
			MemberId memberId,
			Obj declaredIn) {

		final MemberTypeParameter typeParameter =
				findTypeParameter(memberId, declaredIn);

		if (typeParameter != null) {
			return typeParameter;
		}

		final Member member = toMember();

		if (member == null) {
			return null;
		}

		return member.matchingPath(memberId, declaredIn);
	}

	public final AccessDistributor distributeAccess() {
		return ACCESS_FROM_TYPE.distribute(distribute());
	}

	@Override
	public final Distributor distribute() {
		return Contained.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Contained.distributeIn(this, container);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "TypeDefinition[" + this.object + ']';
	}

	final void addParameter(TypeParameterDeclaration parameter) {
		this.parameters = ArrayUtil.append(this.parameters, parameter);
	}

	final void addDefinitions(SentenceNode[] definitions) {
		for (SentenceNode sentence : definitions) {
			addSentence(sentence);
		}
	}

	final <T> TypeParameters<T> buildTypeParameters(ValueType<T> valueType) {

		TypeParameters<T> parameters = typeParameters(this, valueType);

		for (TypeParameterDeclaration decl : this.parameters) {

			final MemberKey key = decl.getKey();

			if (!key.isValid()) {
				continue;
			}
			parameters = parameters.add(key, decl.getDefinition());
		}

		return parameters;
	}

	private void addSentence(SentenceNode sentence) {
		if (sentence.getType() != SentenceType.PROPOSITION) {
			getLogger().error(
					"prohibited_type_definition_sentence_type",
					sentence.getMark(),
					"Only propositions allowed within type definition");
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length == 0) {
			return;
		}
		if (disjunction.length > 1) {
			getLogger().error(
					"prohibited_type_definition_disjunction",
					disjunction[1].getSeparator(),
					"Disjunctions prohibited within type definition");
		}

		addAlt(disjunction[0]);
	}

	private void addAlt(AlternativeNode alt) {

		final SerialNode[] conjunction = alt.getConjunction();

		if (conjunction.length > 1) {
			getLogger().error(
					"prohibited_type_definition_conjunction",
					conjunction[1].getSeparator(),
					"Conjunctions prohibited within type definition");
		}

		for (SerialNode node : conjunction) {

			final StatementNode statement = node.getStatement();

			if (statement == null) {
				continue;
			}
			statement.accept(TYPE_DEFINITION_VISITOR, this);
		}
	}

	private MemberTypeParameter findTypeParameter(
			MemberId memberId,
			Obj declaredIn) {

		final Member objectMember =
				this.object.objectMember(Accessor.PUBLIC, memberId, declaredIn);

		if (objectMember == null) {
			return null;
		}

		return objectMember.toTypeParameter();
	}

}
