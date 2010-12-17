/*
    Compiler
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.ref.Ref.falseRef;
import static org.o42a.core.ref.path.Path.modulePath;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.RefVisitor.Owner;
import org.o42a.compiler.ip.ref.ModuleRef;
import org.o42a.core.Distributor;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.ref.Ref;


public class ModuleRefVisitor extends AbstractRefVisitor<Ref, Distributor> {

	public static final ModuleRefVisitor MODULE_REF_VISITOR =
		new ModuleRefVisitor();
	public static final ModuleRefVisitor SAME_MODULE_REF_VISITOR =
		new SameModuleRefVisitor();

	private final OwnerVisitor ownerVisitor = new OwnerVisitor();

	ModuleRefVisitor() {
	}

	@Override
	public Ref visitMemberRef(MemberRefNode ref, Distributor p) {

		final Owner result = ref.accept(this.ownerVisitor, p);

		return result != null ? result.ref() : null;
	}

	@Override
	public Ref visitAdapterRef(AdapterRefNode ref, Distributor p) {

		final Owner result = ref.accept(this.ownerVisitor, p);

		return result != null ? result.ref() : null;
	}

	@Override
	protected Ref visitRef(RefNode ref, Distributor p) {
		p.getContext().getLogger().invalidReference(ref);
		return falseRef(location(p, ref), p);
	}

	protected StaticTypeRef declaredIn(RefNode declaredInNode, Distributor p) {
		if (declaredInNode == null) {
			return null;
		}

		final Ref declaredIn = declaredInNode.accept(EXPRESSION_VISITOR, p);

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

	protected Ref moduleRef(MemberRefNode moduleRef, Distributor p) {
		return modulePath(moduleRef.getName().getName()).target(p.getContext());
	}

	private static final class SameModuleRefVisitor extends ModuleRefVisitor {

		@Override
		protected Ref moduleRef(MemberRefNode moduleRef, Distributor p) {
			return new ModuleRef(location(p, moduleRef), p);
		}

	}

	private final class OwnerVisitor
			extends AbstractExpressionVisitor<Owner, Distributor> {

		@Override
		public Owner visitMemberRef(MemberRefNode ref, Distributor p) {

			final Owner owner;
			final ExpressionNode ownerNode = ref.getOwner();

			if (ownerNode != null) {
				owner = ownerNode.accept(this, p);
				if (owner == null) {
					return null;
				}
			} else {
				owner = null;
			}

			final StaticTypeRef declaredIn = declaredIn(ref.getDeclaredIn(), p);

			if (owner != null) {
				return owner.memberRefOwner(
						location(p, ref),
						memberName(ref.getName().getName()),
						declaredIn);
			}

			return new Owner(moduleRef(ref, p));
		}

		@Override
		public Owner visitAdapterRef(AdapterRefNode ref, Distributor p) {

			final Owner owner = ref.getOwner().accept(this, p);

			if (owner == null) {
				return null;
			}

			final Ref type = ref.getType().accept(MODULE_REF_VISITOR, p);

			if (type == null) {
				return null;
			}

			return owner.memberRefOwner(
					location(p, ref),
					adapterId(type.toStaticTypeRef()),
					declaredIn(ref.getDeclaredIn(), p));
		}

		@Override
		protected Owner visitExpression(
				ExpressionNode expression,
				Distributor p) {
			p.getContext().getLogger().invalidReference(expression);
			return null;
		}

	}

}
