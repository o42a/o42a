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

import static org.o42a.common.macro.Macros.consumeCondition;
import static org.o42a.common.macro.Macros.consumeSelfAssignment;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.ir.op.RefTargetIR;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.macro.MacroConsumer;
import org.o42a.util.log.LogInfo;


public class MacroExpansionStep extends Step {

	public static final MacroExpansionStep MACRO_EXPANSION_STEP =
			new MacroExpansionStep(false);
	public static final MacroExpansionStep MACRO_REEXPANSION_STEP =
			new MacroExpansionStep(true);

	static void prohibitedExpansion(CompilerLogger logger, LogInfo location) {
		logger.error(
				"prohibited_macro_expansion",
				location,
				"Macro expansion is not allowed here");
	}

	private final boolean reexpansion;

	private MacroExpansionStep(boolean reexpansion) {
		this.reexpansion = reexpansion;
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
	protected Statement condition(Ref condition, Statements<?> statements) {
		return consumeCondition(statements, condition);
	}

	@Override
	protected Statement value(
			LocationInfo location,
			Ref value,
			Statements<?> statements) {
		return consumeSelfAssignment(statements, location, value);
	}

	@Override
	protected Ref consume(Ref ref, Consumer consumer) {

		final Ref macroRef = ref.getPath().cut(1).target(ref.distribute());
		final MacroExpansion expansion =
				new MacroExpansion(macroRef, this.reexpansion);

		if (!this.reexpansion) {
			return new MacroExpansionTemplate(expansion).toRef(consumer);
		}

		final MacroConsumer macroConsumer =
				consumer.expandMacro(macroRef, null, null);

		if (macroConsumer == null) {
			return null;
		}

		return expansion.expandMacro(macroConsumer, ref.getScope());
	}

	@Override
	protected FieldDefinition fieldDefinition(Ref ref) {
		return new FieldDefinitionByMacroExpansion(ref);
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
		prohibitedExpansion(resolver.getLogger(), resolver.getLocation());
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

	@Override
	protected RefTargetIR targetIR(RefIR refIR) {
		throw new UnsupportedOperationException();
	}

}
