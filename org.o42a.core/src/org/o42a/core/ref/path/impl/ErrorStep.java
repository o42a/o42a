/*
    Compiler Core
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
package org.o42a.core.ref.path.impl;

import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;

import org.o42a.core.Container;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.ir.op.RefTargetIR;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public class ErrorStep extends Step {

	public static final ErrorStep ERROR_STEP = new ErrorStep();

	private ErrorStep() {
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
	public String toString() {
		return "ERROR";
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return invalidDefinition(ref, ref.distribute());
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	protected Container resolve(StepResolver resolver) {
		return null;
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		normalizer.cancel(); // Normalization impossible.
	}

	@Override
	protected void normalizeStatic(PathNormalizer normalizer) {
		normalizer.cancel(); // Normalization impossible.
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer) {
		return unchangedPath(toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		throw new UnsupportedOperationException(
				"Error path step can not be written");
	}

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException(
				"Error path step can not be written");
	}

}
