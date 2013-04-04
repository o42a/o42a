/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.st;

import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.ref.owner.Referral.TARGET_REFERRAL;
import static org.o42a.compiler.ip.st.LocalInterpreter.local;
import static org.o42a.compiler.ip.st.StatementVisitor.validateAssignment;
import static org.o42a.compiler.ip.st.StatementVisitor.validateLocalScope;

import org.o42a.ast.statement.*;
import org.o42a.core.st.sentence.Local;


final class LocalStatementVisitor
		extends AbstractStatementVisitor<Void, StatementsAccess> {

	private final StatementVisitor visitor;

	LocalStatementVisitor(StatementVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public Void visitAssignment(AssignmentNode assignment, StatementsAccess p) {
		if (!validateAssignment(assignment)) {
			return null;
		}

		final LocalNode local = assignment.getDestination().toLocal();

		if (local == null) {
			return super.visitAssignment(assignment, p);
		}

		addLocalAssignment(p, assignment, local);

		return null;
	}

	@Override
	public Void visitLocalScope(LocalScopeNode scope, StatementsAccess p) {
		if (!validateLocalScope(scope)) {
			return null;
		}

		addLocalScope(p, scope);

		return super.visitLocalScope(scope, p);
	}

	@Override
	protected Void visitStatement(StatementNode statement, StatementsAccess p) {
		return statement.accept(this.visitor, p);
	}

	void addLocalScope(StatementsAccess statements, LocalScopeNode scope) {
		local(
				this.visitor.ip(),
				this.visitor.getContext(),
				statements,
				scope.getInterface(),
				scope.getLocal(),
				TARGET_REFERRAL);
		scope.getContent().accept(this, statements);
	}

	void addLocalAssignment(
			StatementsAccess statements,
			AssignmentNode assignment,
			LocalNode localNode) {

		final Local local = local(
				this.visitor.ip(),
				this.visitor.getContext(),
				statements,
				null,
				localNode,
				BODY_REFERRAL);

		if (local == null) {
			return;
		}

		this.visitor.addAssignment(statements, assignment, local.toRef());
	}

}
