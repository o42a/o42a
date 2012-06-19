/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.*;
import static org.o42a.compiler.ip.ref.MemberById.prototypeExpressionClause;
import static org.o42a.compiler.ip.ref.owner.OwnerFactory.DEFAULT_OWNER_FACTORY;
import static org.o42a.compiler.ip.ref.owner.OwnerFactory.NON_LINK_OWNER_FACTORY;
import static org.o42a.core.member.MemberName.clauseName;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.IntrinsicRefNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.ref.owner.OwnerFactory;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public abstract class RefInterpreter {

	public static final RefInterpreter PLAIN_REF_IP =
			new PlainRefIp();
	public static final RefInterpreter PATH_COMPILER_REF_IP =
			new PathCompilerRefIp();
	public static final RefInterpreter CLAUSE_DEF_REF_IP =
			new ClauseDefRefIp();
	public static final RefInterpreter CLAUSE_DECL_REF_IP =
			new ClauseDeclRefIp();
	public static final RefInterpreter ADAPTER_FIELD_REF_IP =
			new AdapterFieldRefIp();

	public static Path enclosingModulePath(Container of) {

		Container container = of;

		if (container.getScope().isTopScope()) {
			return ROOT_PATH;
		}

		Path result = null;

		for (;;) {

			final Container enclosing =
					container.getScope().getEnclosingContainer();

			if (enclosing.getScope().isTopScope()) {
				if (result == null) {
					return SELF_PATH;
				}
				return result;
			}

			final Path enclosingScopePath =
					container.getScope().getEnclosingScopePath();

			if (result == null) {
				result = enclosingScopePath;
			} else {
				result = result.append(enclosingScopePath);
			}

			container = enclosing;
		}
	}

	public static Path clauseObjectPath(LocationInfo location, Scope of) {

		Scope scope = of;
		Path path = Path.SELF_PATH;

		for (;;) {

			final Clause clause = scope.getContainer().toClause();

			if (clause == null) {

				final Obj object = scope.toObject();

				if (object == null) {
					location.getContext().getLogger().error(
							"unresolved_object_intrinsic",
							location,
							"Enclosing object not found");
					return null;
				}

				return path;
			}

			final Scope enclosingScope = scope.getEnclosingScope();

			if (enclosingScope == null) {
				return null;
			}

			path = path.append(scope.getEnclosingScopePath());
			scope = enclosingScope;
		}
	}

	public static boolean isRootRef(ExpressionNode node) {
		return node.accept(RootVisitor.ROOT_VISITOR, null) != null;
	}

	private static boolean match(Name name, Container container) {

		final Member member = container.toMember();

		if (member == null) {
			return false;
		}
		if (name == null) {
			return true;
		}

		final MemberName memberName = member.getMemberKey().getMemberName();

		if (memberName == null) {
			return false;
		}

		return name.is(memberName.getName());
	}

	private static void unresolvedParent(LocationInfo location, Name name) {
		location.getContext().getLogger().error(
				"unresolved_parent",
				location,
				"Enclosing member '%s' can not be found",
				name);
	}

	private final OwnerFactory ownerFactory;
	private final TargetRefVisitor targetRefVisitor;
	private final BodyRefVisitor bodyRefVisitor;
	private final OwnerVisitor ownerVisitor;

	RefInterpreter(OwnerFactory ownerFactory) {
		this.ownerFactory = ownerFactory;
		this.targetRefVisitor = new TargetRefVisitor(this);
		this.bodyRefVisitor = new BodyRefVisitor(this);
		this.ownerVisitor = new OwnerVisitor(this);
	}

	public abstract Interpreter ip();

	public final RefNodeVisitor<Ref, Distributor> targetRefVisitor() {
		return this.targetRefVisitor;
	}

	public final RefNodeVisitor<Ref, Distributor> bodyRefVisitor() {
		return this.bodyRefVisitor;
	}

	public final ExpressionNodeVisitor<Owner, Distributor> ownerVisitor() {
		return this.ownerVisitor;
	}

	public abstract MemberId memberName(Name name);

	public final OwnerFactory ownerFactory() {
		return this.ownerFactory;
	}

	public Ref objectIntrinsic(IntrinsicRefNode ref, Distributor p) {
		p.getLogger().error(
				"prohibited_object_intrinsic",
				ref,
				"$object$ intrinsic is allowed only within clauses");
		return errorRef(location(p, ref), p);
	}

	public StaticTypeRef declaredIn(RefNode declaredInNode, Distributor p) {
		if (declaredInNode == null) {
			return null;
		}

		final Ref declaredIn = declaredInNode.accept(bodyRefVisitor(), p);

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

	public RefNodeVisitor<Ref, Distributor> adapterTypeVisitor() {
		return bodyRefVisitor();
	}

	public Path parentPath(LocationInfo location, Name name, Container of) {

		Path path = SELF_PATH;
		Path parentPath = SELF_PATH;
		Container nested = null;
		Container container = of;

		for (;;) {
			if (match(name, container) && !skip(nested)) {
				return path.append(parentPath);
			}

			nested = container;

			final Container parent = container.getParentContainer();

			if (parent == null) {
				unresolvedParent(location, name);
				return null;
			}

			final Scope scope = container.getScope();
			final Path enclosingScopePath = scope.getEnclosingScopePath();

			if (enclosingScopePath == null) {
				unresolvedParent(location, name);
				return null;
			}

			if (scope == parent.getScope()) {

				final Member parentMember = parent.toMember();

				if (parentMember == null
						|| scope.getContainer().toMember()
						== parentMember) {
					parentPath = SELF_PATH;
				} else {
					parentPath = parentMember.getMemberKey().toPath();
				}
				container = parent;
				continue;
			}

			container = parent;
			parentPath = SELF_PATH;
			if (path != null) {
				path = path.append(enclosingScopePath);
			} else {
				path = enclosingScopePath;
			}
		}
	}

	private boolean skip(Container nested) {
		if (nested == null) {
			return false;
		}
		if (this == CLAUSE_DECL_REF_IP) {
			return false;
		}
		// Top-level expression clause
		// shouldn't have access to enclosing prototype.
		return prototypeExpressionClause(nested);
	}

	private static final class PlainRefIp extends RefInterpreter {

		PlainRefIp() {
			super(DEFAULT_OWNER_FACTORY);
		}

		@Override
		public Interpreter ip() {
			return PLAIN_IP;
		}

		@Override
		public MemberId memberName(Name name) {
			return fieldName(name);
		}

	}

	private static final class PathCompilerRefIp extends RefInterpreter {

		PathCompilerRefIp() {
			super(NON_LINK_OWNER_FACTORY);
		}

		@Override
		public Interpreter ip() {
			return PATH_COMPILER_IP;
		}

		@Override
		public MemberId memberName(Name name) {
			return fieldName(name);
		}

	}

	private static abstract class ClauseRefIp extends RefInterpreter {

		ClauseRefIp(OwnerFactory ownerFactory) {
			super(ownerFactory);
		}

		@Override
		public Ref objectIntrinsic(IntrinsicRefNode ref, Distributor p) {

			final Location location = location(p, ref);
			final Path path = clauseObjectPath(location, p.getScope());

			if (path == null) {
				return errorRef(location, p);
			}

			return path.bind(location, p.getScope()).target(p);
		}

	}

	private static final class ClauseDefRefIp extends ClauseRefIp {

		ClauseDefRefIp() {
			super(DEFAULT_OWNER_FACTORY);
		}

		@Override
		public Interpreter ip() {
			return CLAUSE_DEF_IP;
		}

		@Override
		public MemberId memberName(Name name) {
			return fieldName(name);
		}

		@Override
		public RefNodeVisitor<Ref, Distributor> adapterTypeVisitor() {
			return PLAIN_REF_IP.adapterTypeVisitor();
		}

	}

	private static final class ClauseDeclRefIp extends ClauseRefIp {

		ClauseDeclRefIp() {
			super(NON_LINK_OWNER_FACTORY);
		}

		@Override
		public Interpreter ip() {
			return CLAUSE_DECL_IP;
		}

		@Override
		public MemberId memberName(Name name) {
			return clauseName(name);
		}

	}

	private static final class AdapterFieldRefIp extends RefInterpreter {

		AdapterFieldRefIp() {
			super(DEFAULT_OWNER_FACTORY);
		}

		@Override
		public Interpreter ip() {
			return PLAIN_IP;
		}

		@Override
		public MemberId memberName(Name name) {
			return fieldName(name);
		}

		@Override
		public StaticTypeRef declaredIn(RefNode declaredInNode, Distributor p) {
			return null;
		}

		@Override
		public RefNodeVisitor<Ref, Distributor> adapterTypeVisitor() {
			return PLAIN_REF_IP.adapterTypeVisitor();
		}

	}

}
