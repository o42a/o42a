/*
    Compiler Core
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
package org.o42a.core.object.macro.impl;

import static org.o42a.core.member.field.FieldDefinition.invalidDefinition;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public class MacroExpansionStep extends Step {

	public static final MacroExpansionStep MACRO_EXPANSION_STEP =
			new MacroExpansionStep();

	static void prohibitedExpansion(CompilerLogger logger, LogInfo location) {
		logger.error(
				"prohibited_macro_expansion",
				location,
				"Macro expansion is not allowed here");
	}

	private MacroExpansionStep() {
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
		return "#";
	}

	@Override
	protected Ref consume(Ref ref, Consumer consumer) {

		final Ref consumption = consumer.consume(ref);

		if (consumption == null) {
			return null;
		}

		consumption.assertSameScope(ref);

		final MacroExpansionFragment expansion =
				new MacroExpansionFragment(ref, consumer);
		final BoundPath newPath = consumption.getPath().append(expansion);

		expansion.init(newPath);

		return newPath.target(ref.distribute());
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		prohibitedExpansion(distributor.getLogger(), path);
		return invalidDefinition(path, distributor);
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {
		prohibitedExpansion(resolver.getPathStart().getLogger(), path);
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
		return reproducedPath(MACRO_EXPANSION_STEP.toPath());
	}

	@Override
	protected PathOp op(PathOp start) {
		throw new UnsupportedOperationException();
	}

}
