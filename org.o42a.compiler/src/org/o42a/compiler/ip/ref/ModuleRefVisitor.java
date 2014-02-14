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
package org.o42a.compiler.ip.ref;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.ref.RefInterpreter.PATH_COMPILER_REF_IP;
import static org.o42a.compiler.ip.ref.RefInterpreter.enclosingModuleRef;
import static org.o42a.compiler.ip.ref.owner.OwnerFactory.NON_LINK_OWNER_FACTORY;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.ref.Ref.falseRef;
import static org.o42a.core.ref.path.Path.modulePath;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


public class ModuleRefVisitor
		extends AbstractRefVisitor<Ref, AccessDistributor> {

	public static final ModuleRefVisitor MODULE_REF_VISITOR =
			new ModuleRefVisitor();
	public static final ModuleRefVisitor SAME_MODULE_REF_VISITOR =
			new SameModuleRefVisitor();

	private final ModuleOwnerVisitor ownerVisitor = new ModuleOwnerVisitor();

	ModuleRefVisitor() {
	}

	@Override
	public Ref visitMemberRef(MemberRefNode ref, AccessDistributor p) {

		final Owner result = ref.accept(this.ownerVisitor, p);

		return result != null ? result.targetRef() : null;
	}

	@Override
	public Ref visitAdapterRef(AdapterRefNode ref, AccessDistributor p) {

		final Owner result = ref.accept(this.ownerVisitor, p);

		return result != null ? result.targetRef() : null;
	}

	@Override
	protected Ref visitRef(RefNode ref, AccessDistributor p) {
		p.getContext().getLogger().invalidReference(ref);
		return falseRef(location(p, ref), p);
	}

	protected StaticTypeRef declaredIn(
			RefNode declaredInNode,
			AccessDistributor p) {
		if (declaredInNode == null) {
			return null;
		}
		return PATH_COMPILER_REF_IP.declaredIn(declaredInNode, p);
	}

	protected Ref moduleRef(MemberRefNode moduleRef, AccessDistributor p) {
		return modulePath(moduleRef.getName().getName())
				.bind(location(p, moduleRef), p.getScope())
				.target(p);
	}

	private static final class SameModuleRefVisitor extends ModuleRefVisitor {

		@Override
		protected Ref moduleRef(MemberRefNode moduleRef, AccessDistributor p) {
			return enclosingModuleRef(location(p, moduleRef), p);
		}

	}

	private final class ModuleOwnerVisitor
			extends AbstractExpressionVisitor<Owner, AccessDistributor> {

		@Override
		public Owner visitMemberRef(MemberRefNode ref, AccessDistributor p) {

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
				return owner.member(
						location(p, ref),
						PATH_COMPILER_REF_IP.memberName(
								ref.getName().getName()),
						declaredIn);
			}

			return NON_LINK_OWNER_FACTORY.owner(
					p.getAccessRules(),
					moduleRef(ref, p));
		}

		@Override
		public Owner visitAdapterRef(AdapterRefNode ref, AccessDistributor p) {

			final Owner owner = ref.getOwner().accept(this, p);

			if (owner == null) {
				return null;
			}

			final Ref type = ref.getType().accept(MODULE_REF_VISITOR, p);

			if (type == null) {
				return null;
			}

			return owner.member(
					location(p, ref),
					adapterId(type.toTypeRef()),
					declaredIn(ref.getDeclaredIn(), p));
		}

		@Override
		protected Owner visitExpression(
				ExpressionNode expression,
				AccessDistributor p) {
			p.getContext().getLogger().invalidReference(expression);
			return null;
		}

	}

}
