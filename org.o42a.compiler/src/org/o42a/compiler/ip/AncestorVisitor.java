/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.core.Distributor.declarativeDistributor;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.core.Distributor;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.util.log.LoggableData;


public class AncestorVisitor
		extends AbstractExpressionVisitor<TypeRef, Distributor> {

	public static TypeRef parseAncestor(
			Interpreter ip,
			AscendantsNode ascendants,
			Distributor distributor) {

		final AscendantNode firstAscendant = ascendants.getAscendants()[0];

		if (firstAscendant.getSeparator() == null) {
			return firstAscendant.getAscendant().accept(
					ip.ancestorVisitor(),
					distributor);
		}

		return firstAscendant.getAscendant().accept(
				ip.staticAnncestorVisitor(),
				distributor);
	}

	public static AscendantsDefinition parseAscendants(
			Interpreter ip,
			AscendantsNode node,
			Distributor distributor) {

		AscendantsDefinition ascendants =
				new AscendantsDefinition(location(distributor, node), distributor);
		final AscendantNode[] ascendantNodes = node.getAscendants();
		final TypeRef ancestor = parseAncestor(ip, node, distributor);

		if (ancestor != noAncestor(distributor.getContext())
				&& ancestor != impliedAncestor(distributor.getContext())) {
			ascendants = ascendants.setAncestor(ancestor);
		}

		for (int i = 1; i < ascendantNodes.length; ++i) {

			final RefNode sampleNode = ascendantNodes[i].getAscendant();

			if (sampleNode != null) {

				final Ref sampleRef =
						sampleNode.accept(ip.refVisitor(), distributor);

				if (sampleRef != null) {
					ascendants = ascendants.addSample(
							sampleRef.toStaticTypeRef());
				}
			}
		}

		return ascendants;
	}

	private static TypeRef impliedAncestor;
	private static TypeRef noAncestor;
	private final Interpreter ip;

	public static TypeRef impliedAncestor(CompilerContext context) {
		if (impliedAncestor == null) {
			impliedAncestor = new NoRef(context).toStaticTypeRef();
		}
		return impliedAncestor;
	}

	public static TypeRef noAncestor(CompilerContext context) {
		if (noAncestor == null) {
			noAncestor = new NoRef(context).toStaticTypeRef();
		}
		return noAncestor;
	}

	AncestorVisitor(Interpreter ip) {
		this.ip = ip;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public TypeRef visitParentheses(
			ParenthesesNode parentheses,
			Distributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	public TypeRef visitScopeRef(ScopeRefNode ref, Distributor p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return impliedAncestor(p.getContext());
		}
		return super.visitScopeRef(ref, p);
	}

	@Override
	protected TypeRef visitRef(RefNode ref, Distributor p) {

		final Ref result = ref.accept(ip().expressionVisitor(), p);

		return result != null ? result.toTypeRef() : null;
	}

	@Override
	protected TypeRef visitExpression(
			ExpressionNode expression,
			Distributor p) {
		return noAncestor(p.getContext());
	}

	private static final class NoRef extends Ref {

		NoRef(CompilerContext context) {
			super(
					new Location(context, new LoggableData("<noref>")),
					declarativeDistributor(context.getRoot()));
		}

		@Override
		public boolean isConstant() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Resolution resolve(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected FieldDefinition createFieldDefinition() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolve(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void fullyResolveValues(Resolver resolver) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected RefOp createOp(HostOp host) {
			throw new UnsupportedOperationException();
		}

	}

}
