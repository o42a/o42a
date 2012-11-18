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
import static org.o42a.compiler.ip.type.def.TypeParameterDefinitionVisitor.TYPE_PARAMETER_DEFINITION_VISITOR;
import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.TypeParametersBuilder;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;


final class TypeDefinition
		extends Placed
		implements TypeParametersBuilder {

	private static final TypeParameterDeclaration[] NO_PARAMETERS =
			new TypeParameterDeclaration[0];

	private TypeParameterDeclaration[] parameters;

	TypeDefinition(LocationInfo location, Distributor disrtibutor) {
		super(location, disrtibutor);
		this.parameters = NO_PARAMETERS;
	}

	private TypeDefinition(
			TypeDefinition location,
			TypeParameterDeclaration[] parameters) {
		super(location, location.distribute());
		this.parameters = parameters;
	}

	public TypeDefinition addParameter(DeclaratorNode declarator) {

		final ExpressionNode definitionNode = declarator.getDefinition();

		if (definitionNode == null) {
			return this;
		}
		if (declarator.getTarget().isPrototype()) {
			getLogger().error(
					"prohibited_prototype_type_parameter",
					location(this, declarator.getDefinitionAssignment()),
					"Type parameter can not be a prototype");
		} else if (declarator.getTarget().isAbstract()) {
			getLogger().error(
					"prohibited_abstract_type_parameter",
					location(this, declarator.getDefinitionAssignment()),
					"Type parameter can not be abstract");
		}
		if (declarator.getInterface() != null) {
			getLogger().error(
					"prohibited_link_type_parameter",
					location(this, declarator.getInterface()),
					"Type parameter can not be a link");
		}

		final TypeRef definition = definitionNode.accept(
				TYPE_PARAMETER_DEFINITION_VISITOR,
				distribute());

		if (definition == null) {
			return this;
		}

		final TypeParameterDeclaration parameter =
				new TypeParameterDeclaration(this, declarator, definition);

		return new TypeDefinition(
				this,
				ArrayUtil.append(this.parameters, parameter));
	}

	@Override
	public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
		return toTypeParameters(defaultParameters.getValueType())
				.refine(defaultParameters);
	}

	@Override
	public TypeParametersBuilder prefixWith(PrefixPath prefix) {

		final TypeParameterDeclaration[] newParameters =
				new TypeParameterDeclaration[this.parameters.length];

		for (int i = 0; i < newParameters.length; ++i) {
			newParameters[i] = this.parameters[i].prefixWith(prefix);
		}

		return new TypeDefinition(this, newParameters);
	}

	@Override
	public TypeParametersBuilder reproduce(Reproducer reproducer) {

		final TypeParameterDeclaration[] newParameters =
				new TypeParameterDeclaration[this.parameters.length];

		for (int i = 0; i < newParameters.length; ++i) {

			final TypeParameterDeclaration newParameter =
					this.parameters[i].reproduce(reproducer);

			if (newParameter == null) {
				return null;
			}
			newParameters[i] = newParameter;
		}

		return new TypeDefinition(this, newParameters);
	}

	@Override
	public String toString() {
		if (this.parameters == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("#(");
		if (this.parameters.length != 0) {
			this.parameters[0].toString(out);
			for (int i = 1; i < this.parameters.length; ++i) {
				out.append(". ");
				this.parameters[i].toString(out);
			}
		}
		out.append(')');

		return out.toString();
	}
	private TypeParameters<?> toTypeParameters(ValueType<?> valueType) {

		TypeParameters<?> parameters = typeParameters(this, valueType);

		for (TypeParameterDeclaration decl : this.parameters) {

			final MemberKey key = decl.getKey();

			if (!key.isValid()) {
				continue;
			}
			parameters = parameters.add(key, decl.getDefinition());
		}
		return parameters;
	}

}
