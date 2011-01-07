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

import static org.o42a.core.ir.IRUtil.canonicalName;
import static org.o42a.intrinsic.root.Root.createRoot;
import static org.o42a.lib.console.ConsoleModule.consoleModule;

import java.util.HashMap;

import org.o42a.core.*;
import org.o42a.core.artifact.intrinsic.Module;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.member.field.Field;
import org.o42a.intrinsic.root.*;
import org.o42a.lib.console.ConsoleModule;


public class CompilerIntrinsics implements Intrinsics {

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
		this.root.resolveAll();
		this.consoleModule = consoleModule(this.root.getContext());
		addModule(this.consoleModule);
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
	public Obj getModule(String moduleId) {

		final ModuleUse module = this.modules.get(canonicalName(moduleId));

		if (module == null) {
			return null;
		}

		return module.use();
	}

	@Override
	public Obj getMainModule() {
		return this.mainModule.use();
	}

	public void addModule(String moduleId, Obj module) {
		registerModule(canonicalName(moduleId), module);
	}

	public void addModule(Module module) {
		registerModule(module.getModuleId(), module);
	}

	public void setMainModule(String moduleId, Obj module) {
		this.mainModule = registerModule(canonicalName(moduleId), module);
	}

	public void setMainModule(Module module) {
		this.mainModule = registerModule(module.getModuleId(), module);
	}

	public void generateAll(IRGenerator generator) {
		getMainModule().ir(generator).allocate();
		if (this.modules.get(this.consoleModule.getModuleId()).isUsed()) {
			this.consoleModule.generateMain(generator);
		}
	}

	private ModuleUse registerModule(String moduleId, Obj module) {

		final ModuleUse use = new ModuleUse(module);

		this.modules.put(moduleId, use);

		return use;
	}

	private static final class ModuleUse {

		private final Obj module;
		private boolean used;

		ModuleUse(Obj module) {
			assert module != null :
				"Module not specified";
			this.module = module;
		}

		public boolean isUsed() {
			return this.used;
		}

		public Obj use() {
			this.used = true;
			return this.module;
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
