/*
    Console Module
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import static org.o42a.core.ir.CodeBuilder.defaultBuilder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.ir.value.ValOp.stackAllocatedVal;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.ref.RefUser.refUser;
import static org.o42a.core.ref.path.Path.modulePath;
import static org.o42a.lib.console.DebugExecMainFunc.DEBUG_EXEC_MAIN;
import static org.o42a.lib.console.DebuggableMainFunc.DEBUGGABLE_MAIN;
import static org.o42a.lib.console.MainFunc.MAIN;
import static org.o42a.lib.console.impl.InitFunc.INIT;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.UserInfo;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.common.object.*;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


@SourcePath("console.o42a")
@RelatedSource("print_to_console.o42a")
public class ConsoleModule extends AnnotatedModule {

	public static final ID CONSOLE_ID = ID.id("console");

	private static final ID MAIN_ID = ID.rawId("main");

	private static final MemberName MAIN_MEMBER =
			FIELD_NAME.memberName(CASE_INSENSITIVE.canonicalName("main"));

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

		final Obj mainObject =
				member(MAIN_MEMBER).substance(dummyUser()).toObject();
		final Scope mainScope = mainModule.getScope();

		final Path modulePath = modulePath(mainModule.getModuleName());

		if (mainModule.type().derivedFrom(mainObject.type())) {
			this.main =
					modulePath.bind(mainModule, mainScope)
					.target(mainScope.distribute());
		} else {

			final AdapterId mainAdapterId = adapterId(mainObject);
			final Member mainMember = mainModule.member(mainAdapterId);

			if (mainMember == null) {
				return null;
			}

			final Field mainAdapter = mainMember.toField().field(dummyUser());

			if (mainAdapter == null) {
				return null;
			}

			final Obj main = mainAdapter.toObject();

			if (main == null) {
				return null;
			}

			final Path adapterPath =
					mainAdapterId.key(mainModule.getScope()).toPath();
			final Path mainPath = modulePath.append(adapterPath);

			this.main =
					mainPath.bind(mainAdapter, mainScope)
					.target(mainScope.distribute());
		}

		return this.main;
	}

	public void generateMain(Generator generator) {
		if (this.main == null) {
			return;
		}

		this.main.assertFullyResolved();

		if (!generator.isDebug()) {
			generator.newFunction().export().create(
					MAIN_ID,
					DEBUGGABLE_MAIN,
					new Main());
			return;
		}

		final Function<DebuggableMainFunc> main =
				generator.newFunction().create(
						ID.rawId("__o42a_main__"),
						DEBUGGABLE_MAIN,
						new Main());

		generator.newFunction().export().create(
				MAIN_ID,
				MAIN,
				new DebugMain(main));
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();
		if (this.main != null) {

			final FullResolver resolver =
					this.main.getScope()
					.resolver()
					.fullResolver(refUser(this.user), VALUE_REF_USAGE);

			this.main.resolveAll(resolver);
		}
	}

	@Override
	protected void normalizeObject(Analyzer analyzer) {
		super.normalizeObject(analyzer);
		if (this.main != null) {

			final Normalizer normalizer = new RootNormalizer(
					analyzer,
					this.main.getScope()).newNormalizer();

			this.inlineMain = this.main.inline(
					normalizer,
					this.main.getScope());
		}
	}

	private ValOp callMain(Function<DebuggableMainFunc> main) {
		main.getGenerator()
		.externalFunction()
		.link("o42a_init", INIT)
		.op(null, main)
		.init(main);

		main.debug("Start execution");

		final Module mainModule =
				getContext()
				.getIntrinsics()
				.getMainModule();
		final CodeBuilder builder = defaultBuilder(main, mainModule);
		final Block exit = main.addBlock("exit");
		final ValOp result = stackAllocatedVal(
				"result",
				main,
				builder,
				ValueType.VOID,
				TEMP_VAL_HOLDER);
		final ValDirs dirs = builder.dirs(main, exit.head()).value(result);
		final ValOp programResult;

		if (this.inlineMain != null) {
			programResult = this.inlineMain.writeValue(dirs, builder.host());
		} else {
			programResult = this.main.op(builder.host()).writeValue(dirs);
		}

		dirs.done();

		if (exit.exists()) {
			exit.debug("Execution failed");
			exit.int32(1).returnValue(exit);
		}

		main.debug("Execution succeed");

		return programResult;
	}

	private final class Main implements FunctionBuilder<DebuggableMainFunc> {

		@Override
		public void build(Function<DebuggableMainFunc> function) {
			callMain(function);
			function.int32(0).returnValue(function);
		}

	}

	private static final class DebugMain implements FunctionBuilder<MainFunc> {

		private final Function<DebuggableMainFunc> main;

		DebugMain(Function<DebuggableMainFunc> main) {
			this.main = main;
		}

		@Override
		public void build(Function<MainFunc> function) {

			final FuncPtr<DebugExecMainFunc> executeMain =
					function.getGenerator()
					.externalFunction()
					.link("o42a_dbg_exec_main", DEBUG_EXEC_MAIN);

			executeMain.op(null, function)
			.call(
					function,
					this.main.getPointer().op(null, function),
					function.arg(function, MAIN.argc()),
					function.arg(function, MAIN.argv()))
			.returnValue(function);
		}

	}

}
