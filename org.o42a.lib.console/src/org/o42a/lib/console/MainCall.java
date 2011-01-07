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
import static org.o42a.core.member.AdapterId.adapterId;

import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CodePtr;
import org.o42a.codegen.code.Function;
import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
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
			LocationSpec location,
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

	public void generateMain(IRGenerator generator) {

		final ObjectIR ir = ir(generator);
		final Function<MainFunc> main;

		if (generator.getGenerator().isDebug()) {
			main = generator.newFunction().create(
					"_o42a_main",
					MainFunc.SIGNATURE);
			generateDebugMain(generator, main);
		} else {
			main = generator.newFunction().export().create(
					"main",
					MainFunc.SIGNATURE);
		}

		main.debug("Start execution");

		final CodeBuilder builder = codeBuilder(generator, getContext(), main);

		final ValOp result =
			main.allocate(generator.valType()).storeUnknown(main);
		final CodeBlk exit = main.addBlock("exit");

		ir.op(builder, main).writeValue(main, exit.head(), result);

		if (exit.exists()) {
			exit.debug("Execution failed");
			exit.int32(-1).returnValue(exit);
		}

		main.debug("Execution succeed");
		result.plainValue(main).toInt32(main).load(main).returnValue(main);
	}

	private void generateDebugMain(
			IRGenerator generator,
			Function<MainFunc> main) {

		final Function<MainFunc> debugMain =
			generator.newFunction().export().create("main", MainFunc.SIGNATURE);

		final CodePtr<DbgExecMainFunc> executeMain =
			generator.externalFunction(
					"o42a_dbg_exec_main",
					DbgExecMainFunc.SIGNATURE);

		executeMain.op(debugMain).call(
				debugMain,
				main.getPointer().op(debugMain),
				debugMain.int32arg(debugMain, 0),
				debugMain.ptrArg(debugMain, 1)).returnValue(debugMain);
	}

}
