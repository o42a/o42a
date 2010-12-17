/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core;

import java.net.URL;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.util.Source;
import org.o42a.util.log.Logger;


public abstract class CompilerContext {

	private final BlockCompiler compiler;
	private final Intrinsics intrinsics;
	private final CompilerLogger logger;

	public CompilerContext(CompilerContext parent, Logger logger) {
		this.compiler = parent.compiler;
		this.intrinsics = parent.intrinsics;
		this.logger =
			logger != null ? new CompilerLogger(logger, this) : parent.logger;
	}

	protected CompilerContext(
			BlockCompiler compiler,
			Intrinsics intrinsics,
			Logger logger) {
		this.compiler = compiler;
		this.intrinsics = intrinsics;
		this.logger = new CompilerLogger(logger, this);
	}

	public final BlockCompiler getCompiler() {
		return this.compiler;
	}

	public final Intrinsics getIntrinsics() {
		return this.intrinsics;
	}

	public final CompilerLogger getLogger() {
		return this.logger;
	}

	public final Obj getVoid() {
		return getIntrinsics().getVoid();
	}

	public final Obj getFalse() {
		return getIntrinsics().getFalse();
	}

	public final Obj getRoot() {
		return getIntrinsics().getRoot();
	}

	public final boolean compatible(LocationSpec location) {
		return location.getContext().compiler == this.compiler
		&& location.getContext().intrinsics == this.intrinsics;
	}

	public abstract Source getSource();

	public abstract CompilerContext contextFor(String path) throws Exception;

	public boolean declarationsVisibleFrom(CompilerContext viewer) {
		return getSource().equals(viewer.getSource());
	}

	public CompilerContext urlContext(
			String name,
			URL base,
			String path,
			Logger logger) {
		return new URLContext(this, name, base, path, logger);
	}

	public final BlockBuilder compileBlock() {
		return getCompiler().compileBlock(this);
	}

	@Override
	public String toString() {
		return getSource().toString();
	}

}
