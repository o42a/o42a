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
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Ex;
import org.o42a.core.st.Reproducer;


class PathTarget extends Ex {

	private final Path path;
	private final Ref start;
	private Path fullPath;
	private boolean fullPathBuilt;

	PathTarget(
			LocationInfo location,
			Distributor distributor,
			Path path,
			Ref start) {
		super(location, distributor);
		this.path = path.rebuild();
		this.start = start;
	}

	PathTarget(
			LocationInfo location,
			Distributor distributor,
			Path path) {
		super(location, distributor);
		this.path = path.rebuild();
		this.start = null;
		this.fullPath = this.path;
		this.fullPathBuilt = true;
	}

	@Override
	public Path getPath() {
		if (this.fullPathBuilt) {
			return this.fullPath;
		}
		this.fullPathBuilt = true;

		final Path startPath = this.start.getPath();

		if (startPath == null) {
			return null;
		}

		return this.fullPath = startPath.append(this.path).rebuild();
	}

	@Override
	public Ref rescope(Rescoper rescoper) {
		if (this.start != null) {
			return this.path.target(
					this,
					distributeIn(rescoper.getFinalScope().getContainer()),
					this.start.rescope(rescoper));
		}

		if (!(rescoper instanceof PathRescoper)) {
			return super.rescope(rescoper);
		}

		final PathRescoper pathRescoper = (PathRescoper) rescoper;
		final Path rescopePath = pathRescoper.getPath();

		return rescopePath.append(this.path).target(
				this,
				distributeIn(rescoper.getFinalScope().getContainer()));
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		Path path = getPath();
		final Ref start;

		if (path == null) {
			start = this.start;
			path = this.path;
		} else {
			start = null;
		}

		final PathReproduction pathReproduction =
			this.path.reproduce(this, reproducer);

		if (pathReproduction == null) {
			return null;
		}

		final PathTarget reproducedPart;

		if (start == null) {
			reproducedPart = new PathTarget(
					this,
					reproducer.distribute(),
					pathReproduction.getReproducedPath());
		} else {

			final Ref reproducedStart = start.reproduce(reproducer);

			if (reproducedStart == null) {
				return null;
			}

			reproducedPart = new PathTarget(
					this,
					reproducer.distribute(),
					pathReproduction.getReproducedPath(),
					reproducedStart);
		}

		if (!pathReproduction.isOutOfClause()) {
			return reproducedPart;
		}

		final Ref phrasePrefix =
			reproducer.getPhrasePrefix().rescope(reproducedPart.toRescoper());
		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phrasePrefix;
		}

		return new PathTarget(
				this,
				reproducer.distribute(),
				externalPath,
				phrasePrefix);
	}

	@Override
	public Rescoper toRescoper() {
		if (this.start == null) {
			return this.path.rescoper(getScope());
		}

		final Scope start = this.start.resolve(getScope()).getScope();

		return this.path.rescoper(start).and(this.start.toRescoper());
	}

	@Override
	public FieldDefinition toFieldDefinition() {
		return new PathTargetDefinition(this);
	}

	@Override
	public String toString() {
		if (this.fullPath != null) {
			return this.fullPath.toString();
		}
		if (this.path != null) {
			if (this.start == null) {
				return this.path.toString();
			}
			return this.start + ":" + this.path;
		}
		return super.toString();
	}

	@Override
	protected Resolution resolveExpression(Scope scope) {

		final Container resolved;

		if (this.start == null) {
			resolved = this.path.resolve(this, scope);
		} else {
			resolved = this.path.resolve(this, scope, this.start);
		}

		return containerResolution(resolved);
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private static final class Op extends RefOp {

		Op(HostOp host, PathTarget ref) {
			super(host, ref);
		}

		@Override
		public HostOp target(Code code, CodePos exit) {

			final PathTarget ref = (PathTarget) getRef();
			final HostOp start;

			if (ref.path.isAbsolute()) {

				final Obj root = host().getContext().getRoot();

				start = root.ir(getGenerator()).op(getBuilder(), code);
			} else if (ref.start != null) {
				start = ref.start.op(host()).target(code, exit);
			} else {
				start = host();
			}

			return ref.path.write(code, exit, start);
		}

	}

}
