/*
    Compiler Core
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
package org.o42a.core.ref.path;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;


final class ModuleFragment extends PathFragment {

	private final String moduleId;

	ModuleFragment(String moduleId) {
		this.moduleId = moduleId;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	public String getModuleId() {
		return this.moduleId;
	}

	@Override
	public Container resolve(
			LocationSpec location,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {

		final CompilerContext context = start.getContext();
		final Obj module = context.getIntrinsics().getModule(this.moduleId);

		if (module == null) {
			context.getLogger().unresolvedModule(location, this.moduleId);
			return null;
		}
		walker.module(this, this.moduleId, module);

		return module;
	}

	@Override
	public HostOp write(Code code, CodePos exit, HostOp start) {

		final Obj module =
			start.getContext().getIntrinsics().getModule(this.moduleId);
		final ObjectIR moduleIR = module.ir(start.getGenerator());

		return moduleIR.op(start.getBuilder(), code);
	}

	@Override
	public Reproduction reproduce(LocationSpec location, Scope scope) {
		return unchanged();
	}

	@Override
	public int hashCode() {
		return this.moduleId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final ModuleFragment other = (ModuleFragment) obj;

		return this.moduleId.equals(other.moduleId);
	}

	@Override
	public String toString() {
		return "<" + this.moduleId + '>';
	}

}
