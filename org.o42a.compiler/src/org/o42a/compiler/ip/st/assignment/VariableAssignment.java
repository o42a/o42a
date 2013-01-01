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

import static org.o42a.core.ref.RefUsage.ASSIGNEE_REF_USAGE;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;
import static org.o42a.core.value.link.LinkValueType.VARIABLE;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.member.local.FullLocalResolver;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
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

		if (!statement.getValue()
				.toTypeRef()
				.relationTo(destTypeRef)
				.checkDerived(statement.getLogger())) {
			return new AssignmentError(statement);
		}

		return new VariableAssignment(statement);
	}

	private VariableAssignment(AssignmentStatement statement) {
		super(statement);
	}

	@Override
	public void resolve(FullLocalResolver resolver) {

		final Ref destination = getStatement().getDestination();
		final Ref value = getStatement().getValue();

		destination.resolveAll(resolver.setRefUsage(ASSIGNEE_REF_USAGE));

		final Ref destTarget =
				destination.getPath()
				.dereference()
				.target(destination.distribute());
		final FullLocalResolver targetResolver =
				resolver.setRefUsage(TARGET_REF_USAGE);
		final Resolution val = value.resolveAll(targetResolver);
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
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	public AssignmentKind reproduce(
			AssignmentStatement statement,
			Reproducer reproducer) {
		return new VariableAssignment(statement);
	}

	@Override
	public Cmd cmd() {
		return new AssignmentCmd(getStatement());
	}

}
