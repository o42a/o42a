/*
    Console Module
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.CodeBuilder.hostlessBuilder;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.ref.path.Path.modulePath;
import static org.o42a.lib.console.DebugExecMainFunc.DEBUG_EXEC_MAIN;
import static org.o42a.lib.console.DebuggableMainFunc.DEBUGGABLE_MAIN;
import static org.o42a.lib.console.MainFunc.MAIN;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.UserInfo;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.common.object.*;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;
import org.o42a.core.value.ValueStruct;


@SourcePath("console.o42a")
@RelatedSources("print_to_console.o42a")
public class ConsoleModule extends AnnotatedModule {

	public static ConsoleModule consoleModule(CompilerContext parentContext) {
		return new ConsoleModule(
				parentContext,
				moduleSources(ConsoleModule.class));
	}

	private Ref main;
	private InlineValue inlineMain;
	private UserInfo user;

	private ConsoleModule(
			CompilerContext parentContext,
			AnnotatedSources sources) {
		super(parentContext, sources);
	}

	public Ref createMain(UserInfo user) {
		this.user = user;

		final Module mainModule = getContext().getIntrinsics().getMainModule();

		if (mainModule == null) {
			return null;
		}

		final Obj mainObject = field("main").substance(dummyUser()).toObject();
		final AdapterId mainAdapterId = adapterId(mainObject);
		final Member mainMember = mainModule.member(mainAdapterId);

		if (mainMember == null) {
			return null;
		}

		final Field<?> mainAdapter = mainMember.toField().field(dummyUser());

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
				adapterPath.bind(mainAdapter, mainMember.getScope())
				.target(mainModule.distribute());
		final Path mainPath;

		if (!main.isPrototype()) {
			mainPath =
					modulePath(mainModule.getModuleId())
					.append(adapterPath);
		} else {
			mainPath =
					modulePath(mainModule.getModuleId())
					.newObject(new MainCall(
							main,
							mainModule.distribute(),
							adapterRef.toStaticTypeRef()));
		}

		final Scope mainScope = mainModule.getScope();

		this.main = mainPath
				.bind(mainAdapter, mainScope)
				.target(mainScope.distribute());

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

		final ValOp result = callMain(main);

		result.rawValue(main.id("execution_result_ptr"), main)
		.toAny(null, main)
		.toInt32(null, main)
		.load(null, main)
		.returnValue(main);

		main.done();
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();
		if (this.main != null) {

			final Resolver resolver =
					this.main.getScope().newResolver(this.user);

			this.main.resolve(resolver).resolveValue();
		}
	}

	@Override
	protected void normalizeArtifact(Analyzer analyzer) {
		super.normalizeArtifact(analyzer);
		if (this.main != null) {
			this.inlineMain = this.main.inline(
					new Normalizer(analyzer, this.main.getScope()),
					this.main.getScope());
		}
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

	private ValOp callMain(Function<DebuggableMainFunc> main) {
		main.debug("Start execution");

		final CodeBuilder builder = hostlessBuilder(getContext(), main);
		final Block exit = main.addBlock("exit");
		final AllocationCode alloc = main.undisposable();
		final ValOp result =
				alloc.allocate(null, VAL_TYPE)
				.op(builder, ValueStruct.INTEGER)
				.storeIndefinite(alloc);
		final ValDirs dirs =
				builder.falseWhenUnknown(main, exit.head())
				.value(alloc.id("exec_main"), result);
		final Block code = dirs.code();

		final ValOp programResult;

		if (this.inlineMain != null) {
			programResult = this.inlineMain.writeValue(dirs, builder.host());
		} else {
			programResult = this.main.op(builder.host()).writeValue(dirs);
		}
		result.store(code, programResult);

		dirs.done();
		alloc.done();

		if (exit.exists()) {
			exit.debug("Execution failed");
			exit.int32(-1).returnValue(exit);
		}

		main.debug("Execution succeed");

		return result;
	}

}
