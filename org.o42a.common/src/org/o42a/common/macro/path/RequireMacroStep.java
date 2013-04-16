/*
    Compiler Commons
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
package org.o42a.common.macro.path;

import static org.o42a.common.macro.path.MacroExpansionStep.prohibitedExpansion;
import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;

import org.o42a.core.Container;
import org.o42a.core.ir.op.*;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public final class RequireMacroStep extends Step {

	private final LogInfo expansion;

	public RequireMacroStep(LogInfo expansion) {
		this.expansion = expansion;
	}

	@Override
	public PathKind getPathKind() {
		return PathKind.RELATIVE_PATH;
	}

	@Override
	public RefUsage getObjectUsage() {
		return null;
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		prohibitedExpansion(ref.getLogger(), this.expansion);
		return invalidDefinition(ref, ref.distribute());
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return defaultAncestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected Container resolve(StepResolver resolver) {
		prohibitedExpansion(resolver.getLogger(), this.expansion);
		return null;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return null;
	}

	@Override
	protected PathOp op(HostOp host) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "#";
	}

}
