/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import static org.o42a.compiler.ip.clause.ClauseInterpreter.clauseObjectPath;
import static org.o42a.compiler.ip.ref.owner.OwnerFactory.DEFAULT_OWNER_FACTORY;
import static org.o42a.compiler.ip.ref.owner.OwnerFactory.NON_LINK_OWNER_FACTORY;
import static org.o42a.core.member.MemberName.clauseName;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.value.ValueType.FLOAT;
import static org.o42a.core.value.ValueType.INTEGER;

import org.o42a.ast.Node;
import org.o42a.ast.atom.*;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.ref.owner.OwnerFactory;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.member.AccessSource;
import org.o42a.core.member.MemberId;
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

	public static Ref enclosingModuleRef(
			LocationInfo location,
			Distributor distributor) {

		final Path path = enclosingModulePath(distributor.getContainer());

		return path.bind(location, distributor.getScope()).target(distributor);
	}

	public static boolean isRootRef(ExpressionNode node) {

		final RefNode ref = node.toRef();

		if (ref == null) {
			return false;
		}

		final ScopeRefNode scopeRef = ref.toScopeRef();

		if (scopeRef == null) {
			return false;
		}

		return scopeRef.getType() == ScopeType.ROOT;
	}

	public static boolean linkTargetIsAccessibleFrom(AccessSource accessSource) {
		switch (accessSource) {
		case FROM_CLAUSE_REUSE:
		case FROM_TYPE:
			return false;
		case FROM_DECLARATION:
		case FROM_DEFINITION:
			return true;
		}
		return true;
	}

	public static Ref number(NumberNode number, Distributor distributor) {

		final DigitsNode integer = number.getInteger();

		if (integer == null) {
			// No digits interpreted as zero.
			return integerConstant(number, distributor, 0L);
		}

		final String integerDigits = digits(number.getSign(), integer);
		final FractionalPartNode fractional = number.getFractional();
		final ExponentNode exponent = number.getExponent();

		if (fractional == null && exponent == null) {
			return integerNumber(number, distributor, integerDigits);
		}

		final StringBuilder digits = new StringBuilder(integerDigits);

		if (fractional != null) {
			digits.append('.').append(fractional.getDigits().getDigits());
		}
		if (exponent != null) {
			digits.append('e');
			digits.append(digits(exponent.getSign(), exponent.getDigits()));
		}

		return floatNumber(number, distributor, digits.toString());
	}

	private static String digits(
			SignNode<SignOfNumber> sign,
			DigitsNode digits) {

		final String result = digits.getDigits();

		if (sign != null && sign.getType().isNegative()) {
			return '-' + result;
		}

		return result;
	}

	private static Ref integerNumber(
			NumberNode number,
			Distributor distributor,
			String digits) {

		final long value;

		try {
			value = Long.parseLong(
					digits,
					number.getRadix().getRadix());
		} catch (NumberFormatException e) {
			distributor.getContext().getLogger().error(
					"not_number",
					number,
					"Not a number");
			return integerConstant(number, distributor, 0L);
		}

		return integerConstant(number, distributor, value);
	}

	private static final Ref integerConstant(
			Node node,
			Distributor p,
			long value) {

		final Location location = location(p, node);

		return INTEGER.constantRef(location, p, value);
	}

	private static Ref floatNumber(
			NumberNode number,
			Distributor distributor,
			String digits) {

		final Double value;

		try {
			value = Double.parseDouble(digits);
		} catch (NumberFormatException e) {
			distributor.getContext().getLogger().error(
					"not_number",
					number,
					"Not a number");
			return floatConstant(number, distributor, 0.0d);
		}

		return floatConstant(number, distributor, value);
	}

	private static final Ref floatConstant(
			Node node,
			Distributor p,
			double value) {

		final Location location = location(p, node);

		return FLOAT.constantRef(location, p, value);
	}

	private static Path enclosingModulePath(Container of) {

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

	public final RefNodeVisitor<Ref, AccessDistributor> targetRefVisitor() {
		return this.targetRefVisitor;
	}

	public final RefNodeVisitor<Ref, AccessDistributor> bodyRefVisitor() {
		return this.bodyRefVisitor;
	}

	public final
	ExpressionNodeVisitor<Owner, AccessDistributor> ownerVisitor() {
		return this.ownerVisitor;
	}

	public abstract MemberId memberName(Name name);

	public final OwnerFactory ownerFactory() {
		return this.ownerFactory;
	}

	/**
	 * Constructs a reference, which is {@code $object} expression resolved to.
	 *
	 * @param ref a source expression AST node.
	 * @param p a constructed reference distributor.
	 *
	 * @return resolution reference, or <code>null</code> if {@code $object}
	 * expression has no predefined resolution.
	 */
	public Ref intrinsicObject(MemberRefNode ref, Distributor p) {
		return null;
	}

	public StaticTypeRef declaredIn(
			RefNode declaredInNode,
			AccessDistributor p) {
		if (declaredInNode == null) {
			return null;
		}

		final Ref declaredIn =
				declaredInNode.accept(bodyRefVisitor(), p.fromDeclaration());

		if (declaredIn == null) {
			return null;
		}

		return declaredIn.toStaticTypeRef();
	}

	public RefNodeVisitor<Ref, AccessDistributor> adapterTypeVisitor() {
		return bodyRefVisitor();
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
		public Ref intrinsicObject(MemberRefNode ref, Distributor p) {

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
		public RefNodeVisitor<Ref, AccessDistributor> adapterTypeVisitor() {
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
		public StaticTypeRef declaredIn(
				RefNode declaredInNode,
				AccessDistributor p) {
			return null;
		}

		@Override
		public RefNodeVisitor<Ref, AccessDistributor> adapterTypeVisitor() {
			return PLAIN_REF_IP.adapterTypeVisitor();
		}

	}

}
