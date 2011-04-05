/*
    Console Module
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.lib.console;

import static org.o42a.core.ir.CodeBuilder.codeBuilder;
import static org.o42a.core.ir.op.CodeDirs.exitWhenUnknown;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.lib.console.DebugExecMainFunc.DEBUG_EXEC_MAIN;
import static org.o42a.lib.console.DebuggableMainFunc.DEBUGGABLE_MAIN;
import static org.o42a.lib.console.MainFunc.MAIN;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Function;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.DeclarativeBlock;


final class MainCall extends DefinedObject {

	private final TypeRef adapterRef;

	public static MainCall mainCall(Obj consoleModule, Obj module) {

		final Obj mainObject =
			consoleModule.member("main").getSubstance().toObject();
		final AdapterId mainAdapterId = adapterId(mainObject);
		final Field<?> mainAdapter = module.member(mainAdapterId).toField();

		if (mainAdapter == null) {
			return null;
		}

		final Obj main = mainAdapter.getArtifact().toObject();

		if (main == null) {
			return null;
		}

		final Path adapterPath = mainAdapterId.key(module.getScope()).toPath();
		final Ref adapterRef =
			adapterPath.target(mainAdapter, module.distribute());

		return new MainCall(
				main,
				module.distribute(),
				adapterRef.toStaticTypeRef());
	}

	private MainCall(
			LocationInfo location,
			Distributor enclosing,
			TypeRef adapterRef) {
		super(location, enclosing);
		this.adapterRef = adapterRef;
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(this.adapterRef);
	}

	@Override
	protected void buildDefinition(DeclarativeBlock definition) {
	}

	public void generateMain(Generator generator) {

		final ObjectIR ir = ir(generator);
		final Function<DebuggableMainFunc> main;

		if (generator.isDebug()) {
			main = generator.newFunction().create(
					generator.rawId("__o42a_main__"),
					DEBUGGABLE_MAIN);
			generateDebugMain(generator, main);
		} else {
			main = generator.newFunction().export().create(
					generator.rawId("main"),
					DEBUGGABLE_MAIN);
		}

		main.debug("Start execution");

		final CodeBuilder builder = codeBuilder(getContext(), main);

		final ValOp result = main.allocate(VAL_TYPE).storeUnknown(main);
		final CodeBlk exit = main.addBlock("exit");

		ir.op(builder, main).writeValue(
				exitWhenUnknown(main, exit.head()),
				result);

		if (exit.exists()) {
			exit.debug("Execution failed");
			exit.int32(-1).returnValue(exit);
		}

		main.debug("Execution succeed");
		result.rawValue(main).toAny(main).toInt32(main)
		.load(main).returnValue(main);
	}

	private void generateDebugMain(
			Generator generator,
			Function<DebuggableMainFunc> main) {

		final Function<MainFunc> debugMain =
			generator.newFunction().export().create(
					generator.rawId("main"),
					MAIN);
		final FuncPtr<DebugExecMainFunc> executeMain =
			generator.externalFunction(
					"o42a_dbg_exec_main",
					DEBUG_EXEC_MAIN);

		executeMain.op(debugMain).call(
				debugMain,
				main.getPointer().op(debugMain),
				debugMain.arg(debugMain, MAIN.argc()),
				debugMain.arg(debugMain, MAIN.argv())).returnValue(debugMain);
	}

}
