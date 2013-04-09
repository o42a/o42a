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
package org.o42a.common.macro;

import static org.o42a.common.macro.path.MacroExpansionStep.MACRO_EXPANSION_STEP;
import static org.o42a.common.macro.path.MacroExpansionStep.MACRO_REEXPANSION_STEP;
import static org.o42a.common.macro.st.StatementConsumer.consumeStatement;

import org.o42a.common.macro.field.MacroFieldConsumer;
import org.o42a.common.macro.path.RequireMacroStep;
import org.o42a.core.Distributor;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.log.LogInfo;


public final class Macros {

	public static final Consumer MACRO_FIELD_CONSUMER =
			MacroFieldConsumer.INSTANCE;

	public static Ref expandMacro(Ref ref) {

		final BoundPath path = removeMacroRequirement(ref.getPath());

		return expandMacro(path).target(ref.distribute());
	}

	public static Ref expandMacroField(
			MemberKey macroFieldKey,
			Distributor distributor) {

		final BoundPath path =
				macroFieldKey.toPath()
				.bind(distributor, distributor.getScope());

		return expandMacro(path)
				.target(distributor)
				.consume(MACRO_FIELD_CONSUMER);
	}

	public static BoundPath expandMacro(BoundPath path) {
		return path.append(MACRO_EXPANSION_STEP);
	}

	public static Path expandMacro(Path path) {
		return path.append(MACRO_EXPANSION_STEP);
	}

	public static Ref reexpandMacro(Ref ref) {
		return ref.getPath()
				.append(MACRO_REEXPANSION_STEP)
				.target(ref.distribute());
	}

	public static Ref requireMacro(Ref ref, LogInfo expansion) {
		if (ref == null) {
			return null;
		}
		return ref.getPath()
				.append(new RequireMacroStep(expansion))
				.target(ref.distribute());
	}

	public static Ref removeMacroRequirement(Ref ref) {
		return removeMacroRequirement(ref.getPath()).target(ref.distribute());
	}

	public static BoundPath removeMacroRequirement(BoundPath path) {
		if (path.getRawPath().lastStep() instanceof RequireMacroStep) {
			return path.cut(1);
		}
		return path;
	}

	private Macros() {
	}

	public static Ref consumeCondition(Statements<?> statements, Ref value) {
		return consumeStatement(statements, value, value, true);
	}

	public static Ref consumeSelfAssignment(
			Statements<?> statements,
			LocationInfo location,
			Ref condition) {
		return consumeStatement(statements, location, condition, false);
	}

}
