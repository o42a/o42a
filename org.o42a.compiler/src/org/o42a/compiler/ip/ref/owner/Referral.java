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
package org.o42a.compiler.ip.ref.owner;

import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.RefNodeVisitor;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.RefInterpreter;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;


public abstract class Referral {

	public static final Referral TARGET_REFERRAL = new TargetReferral();
	public static final Referral BODY_REFERRAL = new BodyReferral();

	public final RefNodeVisitor<Ref, Distributor> refVisitor(Interpreter ip) {
		return refVisitor(ip.refIp());
	}

	public abstract RefNodeVisitor<Ref, Distributor> refVisitor(
			RefInterpreter ip);

	public abstract ExpressionNodeVisitor<Ref, Distributor> expressionVisitor(
			Interpreter ip);

	public abstract Ref refer(Owner owner);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	private static final class TargetReferral extends Referral {

		@Override
		public RefNodeVisitor<Ref, Distributor> refVisitor(RefInterpreter ip) {
			return ip.derefVisitor();
		}

		@Override
		public ExpressionNodeVisitor<Ref, Distributor> expressionVisitor(
				Interpreter ip) {
			return ip.derefExVisitor();
		}

		@Override
		public Ref refer(Owner owner) {
			return owner.deref();
		}

	}

	private static final class BodyReferral extends Referral {

		@Override
		public RefNodeVisitor<Ref, Distributor> refVisitor(RefInterpreter ip) {
			return ip.bodyRefVisitor();
		}

		@Override
		public ExpressionNodeVisitor<Ref, Distributor> expressionVisitor(
				Interpreter ip) {
			return ip.bodyExVisitor();
		}

		@Override
		public Ref refer(Owner owner) {
			return owner.bodyRef();
		}

	}

}
