/*
    Intrinsics
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
package org.o42a.intrinsic;

import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.SimpleUsage.simpleUsable;
import static org.o42a.lib.collections.CollectionsModule.collectionsModule;
import static org.o42a.lib.console.ConsoleModule.consoleModule;
import static org.o42a.lib.macros.MacrosModule.macrosModule;
import static org.o42a.lib.test.TestModule.testModule;
import static org.o42a.root.Root.createRoot;

import java.util.HashMap;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.codegen.Generator;
import org.o42a.core.Container;
import org.o42a.core.Namespace;
import org.o42a.core.object.Obj;
import org.o42a.core.source.*;
import org.o42a.intrinsic.impl.ModuleNamespace;
import org.o42a.intrinsic.impl.Top;
import org.o42a.intrinsic.impl.TopContext;
import org.o42a.lib.console.ConsoleModule;
import org.o42a.root.*;
import org.o42a.util.log.Logger;
import org.o42a.util.string.Name;


public class CompilerIntrinsics implements Intrinsics {

	private final CompilerLogger compilerLogger;
	private final FullResolution fullResolution = new FullResolution();
	private final Usable<SimpleUsage> user = simpleUsable("MainUser");
	private final SourceCompiler compiler;
	private final Top top;
	private final CompilerContext topContext;

	private final ModuleNamespace moduleNamespace;
	private final Obj voidObject;
	private final Obj falseObject;
	private final Obj none;
	private final Root root;

	private final HashMap<Name, ModuleUse> modules = new HashMap<>();
	private ModuleUse mainModule;
	private ConsoleModule consoleModule;

	public static CompilerIntrinsics intrinsics(
			SourceCompiler compiler,
			Logger logger) {
		return intrinsics(compiler, new CompilerLogger(logger));
	}

	public static CompilerIntrinsics intrinsics(
			SourceCompiler compiler,
			CompilerLogger compilerLogger) {
		return new CompilerIntrinsics(compilerLogger, compiler);
	}

	private CompilerIntrinsics(
			CompilerLogger compilerLogger,
			SourceCompiler compiler) {
		this.compilerLogger = compilerLogger;
		this.compiler = compiler;
		this.topContext = new TopContext(this);
		this.top = new Top(this.topContext);
		this.moduleNamespace = new ModuleNamespace(this);
		this.voidObject = new VoidObject(this.top);
		this.falseObject = new FalseObject(this.top);
		this.none = new NoneObject(this.top);
		this.root = createRoot(this.top);
		this.consoleModule = consoleModule(this.root.getContext());
		addModule(collectionsModule(this.root.getContext()));
		addModule(this.consoleModule);
		addModule(macrosModule(this.root.getContext()));
		addModule(testModule(this.root.getContext()));
	}

	@Override
	public final CompilerLogger getCompilerLogger() {
		return this.compilerLogger;
	}

	public final SourceCompiler getCompiler() {
		return this.compiler;
	}

	@Override
	public final Container getTop() {
		return this.top;
	}

	@Override
	public Namespace getModuleNamespace() {
		return this.moduleNamespace;
	}

	@Override
	public Obj getVoid() {
		return this.voidObject;
	}

	@Override
	public Obj getFalse() {
		return this.falseObject;
	}

	@Override
	public Obj getNone() {
		return this.none;
	}

	@Override
	public Obj getRoot() {
		return this.root;
	}

	@Override
	public Obj getDirective() {
		return this.root.getDirective();
	}

	@Override
	public Obj getMacro() {
		return this.root.getMacro();
	}

	@Override
	public Obj getInteger() {
		return this.root.getInteger();
	}

	@Override
	public Obj getFloat() {
		return this.root.getFloat();
	}

	@Override
	public Obj getString() {
		return this.root.getString();
	}

	@Override
	public Obj getLink() {
		return this.root.getLink();
	}

	@Override
	public Obj getVariable() {
		return this.root.getVariable();
	}

	@Override
	public Obj getArray() {
		return this.root.getArray();
	}

	@Override
	public Obj getRow() {
		return this.root.getRow();
	}

	@Override
	public Obj getFlow() {
		return this.root.getFlow();
	}

	@Override
	public Module getModule(Name moduleName) {

		final ModuleUse module = this.modules.get(moduleName);

		if (module == null) {
			return null;
		}

		return module.use();
	}

	@Override
	public Module getMainModule() {
		return this.mainModule.use();
	}

	@Override
	public final FullResolution fullResolution() {
		return this.fullResolution;
	}

	public void addModule(Module module) {
		registerModule(module);
	}

	public void removeModule(Module module) {
		this.modules.remove(module.getModuleName());
	}

	public void setMainModule(Module module) {
		if (this.mainModule != null) {
			removeModule(this.mainModule.module);
		}
		this.mainModule = registerModule(module);
		this.consoleModule.createMain(this.user);
	}

	public void resolveAll(Analyzer analyzer, CompileErrors errors) {
		// False object can be used by runtime, so it should always present.

		final FullResolution fullResolution =
				this.root.getContext().fullResolution();

		fullResolution.initiate();
		try {
			this.none.resolveAll();
			this.root.resolveAll();
			for (ModuleUse module : this.modules.values()) {
				module.resolveAll();
			}
			if (consoleUsed()) {
				this.user.useBy(analyzer, SIMPLE_USAGE);
			}
			if (!errors.hasCompileErrors()) {
				normalizeAll(analyzer);
			}
		} finally {
			fullResolution.end();
		}
	}

	public void generateAll(Generator generator) {
		if (consoleUsed()) {
			this.consoleModule.generateMain(generator);
		}
	}

	private ModuleUse registerModule(Module module) {

		final ModuleUse use = new ModuleUse(module);

		this.modules.put(module.getModuleName(), use);

		return use;
	}

	private final boolean consoleUsed() {
		return this.modules.get(this.consoleModule.getModuleName()).isUsed();
	}

	private void normalizeAll(Analyzer analyzer) {
		if (analyzer.isNormalizationEnabled()) {
			this.root.normalize(analyzer);
			for (ModuleUse module : this.modules.values()) {
				module.normalize(analyzer);
			}
			analyzer.resolveDoubts();
		}
	}

	private static final class ModuleUse {

		private final Module module;
		private boolean used;

		ModuleUse(Module module) {
			assert module != null :
				"Module not specified";
			this.module = module;
		}

		public boolean isUsed() {
			return this.used;
		}

		public Module use() {
			this.used = true;
			return this.module;
		}

		public void resolveAll() {
			if (isUsed()) {
				this.module.resolveAll();
			}
		}

		public void normalize(Analyzer analyzer) {
			if (isUsed()) {
				this.module.normalize(analyzer);
			}
		}

		@Override
		public String toString() {
			if (this.used) {
				return this.module.toString();
			}
			return this.module + "[unused]";
		}

	}

}
