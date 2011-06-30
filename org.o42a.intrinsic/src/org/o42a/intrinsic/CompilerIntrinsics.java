/*
    Intrinsics
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
package org.o42a.intrinsic;

import static org.o42a.intrinsic.root.Root.createRoot;
import static org.o42a.lib.console.ConsoleModule.consoleModule;
import static org.o42a.lib.test.TestModule.testModule;
import static org.o42a.util.StringCodec.canonicalName;
import static org.o42a.util.use.Usable.simpleUsable;

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.common.FullResolution;
import org.o42a.core.artifact.common.Intrinsics;
import org.o42a.core.artifact.common.Module;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.intrinsic.root.*;
import org.o42a.lib.console.ConsoleModule;
import org.o42a.util.use.Usable;


public class CompilerIntrinsics extends Intrinsics {

	private final Usable user = simpleUsable("MainUser");
	private final BlockCompiler compiler;
	private final Top top;
	private final CompilerContext topContext;

	private final ModuleNamespace moduleNamespace;
	private final Obj voidObject;
	private final Root root;

	private final HashMap<String, ModuleUse> modules =
		new HashMap<String, ModuleUse>();
	private ModuleUse mainModule;
	private ConsoleModule consoleModule;
	private Obj main;

	public static CompilerIntrinsics intrinsics(BlockCompiler compiler) {
		return new CompilerIntrinsics(compiler);
	}

	private CompilerIntrinsics(BlockCompiler compiler) {
		this.compiler = compiler;
		this.topContext = new TopContext(this);
		this.top = new Top(this.topContext);
		this.moduleNamespace = new ModuleNamespace(this);
		this.voidObject = new VoidObject(this.top);
		this.root = createRoot(this.top);
		this.consoleModule = consoleModule(this.root.getContext());
		addModule(this.consoleModule);
		addModule(testModule(this.root.getContext()));
	}

	public final BlockCompiler getCompiler() {
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
	public Field<Obj> getVoidField() {
		return this.root.getVoidField();
	}

	@Override
	public Obj getVoid() {
		return this.voidObject;
	}

	@Override
	public Obj getRoot() {
		return this.root;
	}

	@Override
	public Obj getFalse() {
		return this.root.getFalse();
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
	public Obj getDirective() {
		return this.root.getDirective();
	}

	@Override
	public Module getModule(String moduleId) {

		final ModuleUse module = this.modules.get(canonicalName(moduleId));

		if (module == null) {
			return null;
		}

		return module.use();
	}

	@Override
	public Module getMainModule() {
		return this.mainModule.use();
	}

	public void addModule(Module module) {
		registerModule(module.getModuleId(), module);
	}

	public void setMainModule(Module module) {
		this.mainModule = registerModule(module.getModuleId(), module);
		this.main = this.consoleModule.createMain(this.user);
	}

	public void resolveAll() {

		final FullResolution fullResolution =
			this.root.getContext().fullResolution();

		fullResolution.start();
		try {
			this.root.resolveAll();
			if (this.mainModule != null) {
				this.mainModule.resolveAll();
				if (this.main != null) {
					this.main.resolveAll();
				}
			}
			for (ModuleUse module : this.modules.values()) {
				module.resolveAll();
			}
		} finally {
			fullResolution.end();
		}
	}

	public void generateAll(Generator generator) {
		if (consoleUsed()) {
			this.user.useBy(generator);
			this.consoleModule.generateMain(generator);
		}
	}

	private ModuleUse registerModule(String moduleId, Module module) {

		final ModuleUse use = new ModuleUse(module);

		this.modules.put(moduleId, use);

		return use;
	}

	private final boolean consoleUsed() {
		return this.modules.get(this.consoleModule.getModuleId()).isUsed();
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

		@Override
		public String toString() {
			if (this.used) {
				return this.module.toString();
			}
			return this.module + "[unused]";
		}

	}

}
