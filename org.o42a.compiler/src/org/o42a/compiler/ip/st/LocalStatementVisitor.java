/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.st.LocalInterpreter.local;
import static org.o42a.compiler.ip.st.StInterpreter.addContent;
import static org.o42a.compiler.ip.st.StatementVisitor.validateAssignment;
import static org.o42a.compiler.ip.st.StatementVisitor.validateLocalScope;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.statement.*;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.Local;


final class LocalStatementVisitor
		implements StatementNodeVisitor<Void, StatementsAccess> {

	private final StatementVisitor visitor;
	private LocalNode localNode;

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
			return visitStatement(assignment, p);
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

		return visitStatement(scope, p);
	}

	@Override
	public Void visitBraces(BracesNode braces, StatementsAccess p) {

		final NameNode localName = this.localNode.getName();
		final ImperativeBlock block;

		if (localName != null) {
			block = p.get().braces(location(p, braces), localName.getName());
		} else {
			block = p.get().braces(location(p, braces));
		}

		if (block == null) {
			return null;
		}

		addContent(p.getRules(), this.visitor, block, braces);

		return null;
	}

	@Override
	public Void visitStatement(StatementNode statement, StatementsAccess p) {
		return statement.accept(this.visitor, p);
	}

	void addLocalScope(StatementsAccess statements, LocalScopeNode scope) {
		local(
				this.visitor.ip(),
				this.visitor.getContext(),
				statements,
				scope.getLocal());
		this.localNode = scope.getLocal();
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
				localNode);

		if (local == null) {
			return;
		}

		this.localNode = localNode;
		this.visitor.addAssignment(statements, assignment, local.toRef());
	}


}
