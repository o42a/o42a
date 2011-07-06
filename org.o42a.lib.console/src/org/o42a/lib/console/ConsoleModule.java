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
import static org.o42a.core.ir.value.ValType.VAL_TYPE;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.lib.console.DebugExecMainFunc.DEBUG_EXEC_MAIN;
import static org.o42a.lib.console.DebuggableMainFunc.DEBUGGABLE_MAIN;
import static org.o42a.lib.console.MainFunc.MAIN;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.core.CompilerContext;
import org.o42a.core.artifact.common.Module;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.ValueType;
import org.o42a.lib.console.impl.Print;
import org.o42a.util.use.UserInfo;


public class ConsoleModule extends Module {

	public static ConsoleModule consoleModule(CompilerContext context) {

		final URL base;

		try {

			final URL self = ConsoleModule.class.getResource(
					ConsoleModule.class.getSimpleName() + ".class");

			base = new URL(self, "../../../..");
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}

		final CompilerContext moduleContext =
			context.urlContext(
				"Console",
				base,
				"console.o42a",
				DECLARATION_LOGGER);

		return new ConsoleModule(moduleContext);
	}

	private Obj main;

	private ConsoleModule(CompilerContext context) {
		super(context, "Console");
	}

	public Obj createMain(UserInfo user) {

		final Obj mainModule = getContext().getIntrinsics().getMainModule();

		if (mainModule == null) {
			return null;
		}

		final Obj mainObject = field("main").substance(user).toObject();
		final AdapterId mainAdapterId = adapterId(mainObject);
		final Member mainMember = mainModule.member(mainAdapterId);

		if (mainMember == null) {
			return null;
		}

		final Field<?> mainAdapter = mainMember.toField(user);

		if (mainAdapter == null) {
			return null;
		}

		final Obj main = mainAdapter.getArtifact().toObject();

		if (main == null) {
			return null;
		}

		final Path adapterPath =
			mainAdapterId.key(mainModule.getScope()).toPath();
		final Ref adapterRef =
			adapterPath.target(mainAdapter, mainModule.distribute());

		if (!main.isPrototype()) {
			this.main = main;
		} else {
			this.main = new MainCall(
					main,
					mainModule.distribute(),
					adapterRef.toStaticTypeRef());
		}

		this.main.value(user);

		return this.main;
	}

	public void generateMain(Generator generator) {
		if (this.main == null) {
			return;
		}

		this.main.assertFullyResolved();

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
		final Code exit = main.addBlock("exit");
		final AllocationCode alloc = main.undisposable();
		final ValOp result =
			alloc.allocate(null, VAL_TYPE)
			.op(builder, ValueType.INTEGER)
			.storeUnknown(main);
		final ValDirs dirs =
			builder.falseWhenUnknown(alloc, exit.head())
			.value(main.id("exec_main"), result);
		final Code code = dirs.code();

		result.store(
				code,
				this.main.ir(generator).op(builder, code).writeValue(dirs));

		dirs.done();
		alloc.done();

		if (exit.exists()) {
			exit.debug("Execution failed");
			exit.int32(-1).returnValue(exit);
		}

		main.debug("Execution succeed");
		result.rawValue(main.id("execution_result_ptr"), main)
		.toAny(null, main)
		.toInt32(null, main)
		.load(null, main)
		.returnValue(main);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		members.addMember(new Print(
				this,
				"print",
				"o42a_io_print_str").toMember());
		members.addMember(new Print(
				this,
				"print_error",
				"o42a_error_append_str").toMember());
		super.declareMembers(members);
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

		executeMain.op(null, debugMain).call(
				debugMain,
				main.getPointer().op(null, debugMain),
				debugMain.arg(debugMain, MAIN.argc()),
				debugMain.arg(debugMain, MAIN.argv())).returnValue(debugMain);
	}

}
