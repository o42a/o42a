/*
    Compiler
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
package org.o42a.compiler.ip.type.ancestor;

import static org.o42a.common.macro.Macros.removeMacroRequirement;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.type.ancestor.AncestorTypeRef.ancestorTypeRef;
import static org.o42a.compiler.ip.type.ancestor.AncestorTypeRef.impliedAncestorTypeRef;
import static org.o42a.compiler.ip.type.ancestor.AncestorTypeRef.macroAncestorTypeRef;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.type.ParamTypeRef;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRefParameters;


public class AncestorVisitor
		implements ExpressionNodeVisitor<AncestorTypeRef, AccessDistributor> {

	private final Interpreter ip;
	private final TypeRefParameters typeParameters;
	private final TypeConsumer typeConsumer;

	public AncestorVisitor(
			Interpreter ip,
			TypeRefParameters typeParameters,
			TypeConsumer typeConsumer) {
		this.ip = ip;
		this.typeParameters = typeParameters;
		this.typeConsumer = typeConsumer;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final TypeRefParameters typeParameters() {
		return this.typeParameters;
	}

	public final TypeConsumer typeConsumer() {
		return this.typeConsumer;
	}

	@Override
	public AncestorTypeRef visitParentheses(
			ParenthesesNode parentheses,
			AccessDistributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return visitExpression(parentheses, p);
	}

	@Override
	public AncestorTypeRef visitScopeRef(ScopeRefNode ref, AccessDistributor p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return impliedAncestorTypeRef();
		}
		return visitRef(ref, p);
	}

	@Override
	public AncestorTypeRef visitRef(RefNode ref, AccessDistributor p) {
		if (typeParameters() != null) {
			return visitExpression(ref, p);
		}

		final Owner owner = ref.accept(ip().refIp().ownerVisitor(), p);

		if (owner == null) {
			return null;
		}

		final Ref result = owner.targetRef();

		if (owner.isMacroExpanding()) {
			return macroAncestorTypeRef(
					paramTypeRef(removeMacroRequirement(result)));
		}

		final ParamTypeRef typeRef = paramTypeRef(result);

		if (owner.isBodyReferred()) {
			return ancestorTypeRef(typeRef);
		}

		return ancestorTypeRef(typeRef);
	}

	@Override
	public AncestorTypeRef visitExpression(
			ExpressionNode expression,
			AccessDistributor p) {

		final Ref ref = expression.accept(
				ip().expressionVisitor(this.typeConsumer),
				p);

		if (ref == null) {
			return null;
		}

		return ancestorTypeRef(paramTypeRef(ref));
	}

	protected ParamTypeRef paramTypeRef(Ref ref) {
		return new ParamTypeRef(ref.toTypeRef(), typeParameters());
	}

}
