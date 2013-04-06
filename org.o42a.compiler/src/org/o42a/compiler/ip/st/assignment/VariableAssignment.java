/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.st.assignment;

import static org.o42a.core.ref.RefUsage.ASSIGNABLE_REF_USAGE;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;
import static org.o42a.core.st.DefValue.RUNTIME_DEF_VALUE;
import static org.o42a.core.value.link.LinkValueType.VARIABLE;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.Condition;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.LinkValueType;


final class VariableAssignment extends AssignmentKind {

	static AssignmentKind variableAssignment(
			AssignmentStatement statement,
			Obj destination) {

		final TypeParameters<?> destParams =
				destination.type().getParameters();
		final LinkValueType destLinkType =
				destParams.getValueType().toLinkType();

		if (destLinkType == null || !destLinkType.is(VARIABLE)) {
			statement.getLogger().error(
					"not_variable_assigned",
					statement.getDestination(),
					"Can only assign to variable");
			return new AssignmentError(statement);
		}

		final PrefixPath prefix =
				statement.getDestination()
				.getPath()
				.toPrefix(statement.getScope());
		final TypeRef destTypeRef =
				destLinkType.interfaceRef(destParams).prefixWith(prefix);

		final Ref value = statement.getValue().buildRef(statement.distribute());

		if (value == null || value.getResolution().isError()) {
			return new AssignmentError(statement);
		}

		if (!value.toTypeRef()
				.relationTo(destTypeRef)
				.checkDerived(statement.getLogger())) {
			return new AssignmentError(statement);
		}

		return new VariableAssignment(statement, value);
	}

	private final Ref value;

	private VariableAssignment(AssignmentStatement statement, Ref value) {
		super(statement);
		this.value = value;
	}

	private VariableAssignment(Ref value) {
		this.value = value;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public DefValue value(Resolver resolver) {
		return RUNTIME_DEF_VALUE;
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Eval eval(CodeBuilder builder, Scope origin) {
		return new VariableAssignmentEval(this);
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Action initialValue(Resolver resolver) {
		return new ExecuteCommand(getStatement(), Condition.RUNTIME);
	}

	@Override
	public void resolve(FullResolver resolver) {

		final Ref destination = getStatement().getDestination();

		destination.resolveAll(resolver.setRefUsage(ASSIGNABLE_REF_USAGE));

		final Ref destTarget =
				destination.getPath()
				.dereference()
				.target(destination.distribute());
		final FullResolver targetResolver =
				resolver.setRefUsage(TARGET_REF_USAGE);
		final Resolution val = getValue().resolveAll(targetResolver);
		final Resolution dest = destTarget.resolveAll(targetResolver);

		if (dest.isError() || val.isError()) {
			return;
		}

		final Obj destObj = dest.toObject();
		final Obj valObj = val.toObject();

		valObj.value().wrapBy(destObj.value());
		valObj.type().wrapBy(destObj.type());
	}

	@Override
	public InlineCmd inlineCommand(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public void normalizeCommand(RootNormalizer normalizer) {
	}

	@Override
	public AssignmentStatement reproduce(
			Reproducer reproducer,
			AssignmentStatement prototype) {

		final Ref destination =
				getStatement().getDestination().reproduce(reproducer);
		final Ref value = getValue().reproduce(reproducer);

		if (destination == null || value == null) {
			return null;
		}

		return new AssignmentStatement(
				prototype,
				reproducer,
				new VariableAssignment(value), destination, value);
	}

	@Override
	public Cmd cmd() {
		return new VariableAssignmentCmd(this);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return getStatement().getDestination() + "=" + this.value;
	}

}
