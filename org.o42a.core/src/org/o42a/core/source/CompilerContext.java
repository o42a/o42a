/*
    Compiler Core
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
package org.o42a.core.source;

import org.o42a.core.object.Obj;
import org.o42a.core.st.sentence.Block;
import org.o42a.util.io.Source;
import org.o42a.util.log.Logger;


public abstract class CompilerContext implements LocationInfo {

	private final SourceCompiler compiler;
	private final Intrinsics intrinsics;
	private final CompilerLogger logger;
	private Location location;

	public CompilerContext(CompilerContext parent, Logger logger) {
		this.compiler = parent.compiler;
		this.intrinsics = parent.intrinsics;
		this.logger =
				logger != null
				? new CompilerLogger(logger, this) : parent.logger;
	}

	protected CompilerContext(
			SourceCompiler compiler,
			Intrinsics intrinsics,
			Logger logger) {
		this.compiler = compiler;
		this.intrinsics = intrinsics;
		this.logger = new CompilerLogger(logger, this);
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		}
		return this.location = new Location(this, getSource());
	}

	public final SourceCompiler getCompiler() {
		return this.compiler;
	}

	public final Intrinsics getIntrinsics() {
		return this.intrinsics;
	}

	@Override
	public final CompilerLogger getLogger() {
		return this.logger;
	}

	public final Obj getVoid() {
		return getIntrinsics().getVoid();
	}

	public final Obj getFalse() {
		return getIntrinsics().getFalse();
	}

	public final Obj getNone() {
		return getIntrinsics().getNone();
	}

	public final Obj getRoot() {
		return getIntrinsics().getRoot();
	}

	public final boolean compatible(LocationInfo location) {
		return compatible(location.getLocation().getContext());
	}

	public final boolean compatible(CompilerContext other) {
		return (other.compiler == this.compiler
				&& other.intrinsics == this.intrinsics);
	}

	public abstract Source getSource();

	public abstract ModuleCompiler compileModule();

	public abstract FieldCompiler compileField();

	public abstract void include(Block block);

	public boolean declarationsVisibleFrom(CompilerContext viewer) {
		return getSource().equals(viewer.getSource());
	}

	public final FullResolution fullResolution() {
		return this.intrinsics.fullResolution();
	}

	@Override
	public String toString() {

		final Source source = getSource();

		if (source == null) {
			return super.toString();
		}

		return source.toString();
	}

}
