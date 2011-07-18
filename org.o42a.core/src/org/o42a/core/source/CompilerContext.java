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
package org.o42a.core.source;

import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.io.Source;
import org.o42a.util.log.Logger;


public abstract class CompilerContext {

	private final SourceCompiler compiler;
	private final Intrinsics intrinsics;
	private final CompilerLogger logger;

	public CompilerContext(CompilerContext parent, Logger logger) {
		this.compiler = parent.compiler;
		this.intrinsics = parent.intrinsics;
		this.logger =
			logger != null ? new CompilerLogger(logger, this) : parent.logger;
	}

	protected CompilerContext(
			SourceCompiler compiler,
			Intrinsics intrinsics,
			Logger logger) {
		this.compiler = compiler;
		this.intrinsics = intrinsics;
		this.logger = new CompilerLogger(logger, this);
	}

	public SectionTag getSectionTag() {
		return IMPLICIT_SECTION_TAG;
	}

	public final SourceCompiler getCompiler() {
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

	public final boolean compatible(LocationInfo location) {
		return compatible(location.getContext());
	}

	public final boolean compatible(CompilerContext other) {
		return (other.compiler == this.compiler
				&& other.intrinsics == this.intrinsics);
	}

	public abstract Source getSource();

	public abstract ModuleCompiler compileModule();

	public abstract FieldCompiler compileField();

	public abstract void include(DeclarativeBlock block, SectionTag tag);

	public boolean declarationsVisibleFrom(CompilerContext viewer) {
		return getSource().equals(viewer.getSource());
	}

	public final FullResolution fullResolution() {
		return this.intrinsics.fullResolution();
	}

	@Override
	public String toString() {
		return getSource().toString();
	}

}
