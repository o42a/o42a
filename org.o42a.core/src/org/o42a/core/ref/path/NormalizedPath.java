/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import java.util.ArrayList;
import java.util.Iterator;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.RefUser;
import org.o42a.core.source.FullResolution;


final class NormalizedPath implements NormalPath {

	private final Scope origin;
	private final ArrayList<NormalStep> normalSteps;
	private final int firstNonIgnored;
	private final boolean isAbsolute;
	private final boolean isStatic;

	private InlineStep inline;
	private BoundPath path;

	NormalizedPath(
			Scope origin,
			BoundPath path,
			ArrayList<NormalStep> normalSteps,
			int firstNonIgnored,
			boolean isAbsolute,
			boolean isStatic) {
		this.origin = origin;
		this.path = path;
		this.normalSteps = normalSteps;
		this.firstNonIgnored = firstNonIgnored < 0 ? 0 : firstNonIgnored;
		this.isAbsolute = isAbsolute;
		this.isStatic = isStatic;
	}

	@Override
	public final boolean isNormalized() {
		return true;
	}

	@Override
	public final Scope getOrigin() {
		return this.origin;
	}

	@Override
	public void appendTo(NormalSteps normalSteps) {
		for (NormalStep normalStep : this.normalSteps) {
			normalSteps.addNormalStep(normalStep);
		}
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {
		if (this.inline != null) {
			this.inline.writeCond(dirs, host);
		} else {
			this.path.op(dirs, host).value().writeCond(dirs);
		}
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {
		if (this.inline != null) {
			return this.inline.writeValue(dirs, host);
		}
		return this.path.op(dirs.dirs(), host).value().writeValue(dirs);
	}

	public final NormalPath done(Analyzer analyzer, boolean done) {

		// Inform the doubt that the path is normalized.
		final RefUser user = this.path.doubt(analyzer).pathNormalized();

		if (done) {
			// Build the normal path.
			// This is called only for a top-level path.
			// The nested paths will be included in the result.
			ignoreLeading();
			build(user);
		}

		return this;
	}

	@Override
	public String toString() {
		if (this.normalSteps == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("NormalPath<");
		if (this.path.isAbsolute()) {
			out.append('/');
		}

		final Iterator<NormalStep> steps = this.normalSteps.iterator();

		if (steps.hasNext()) {
			out.append(steps.next());
			while (steps.hasNext()) {
				out.append('/').append(steps.next());
			}
		}
		out.append('>');

		return out.toString();
	}

	private void ignoreLeading() {
		for (int i = 0; i < this.firstNonIgnored; ++i) {
			this.normalSteps.get(i).ignore();
		}
	}

	private NormalPath build(RefUser user) {

		InlineStep precedingInline = null;
		Path path;

		if (this.isAbsolute) {
			path = ROOT_PATH;
		} else {
			path = SELF_PATH;
		}

		for (int i = this.firstNonIgnored, len = this.normalSteps.size();
				i < len;
				++i) {

			final NormalStep normalStep = this.normalSteps.get(i);
			final InlineStep inline = normalStep.toInline();

			if (inline != null) {
				inline.after(precedingInline);
				precedingInline = inline;
				continue;
			}

			assert precedingInline == null :
				"Non-in-line normal step (" + normalStep
				+ ") after the in-line one (" + precedingInline
				+ ")";

			path = normalStep.toAppender().appendTo(path);
		}

		if (precedingInline != null) {
			// In-line normal step.
			this.inline = precedingInline;
			return this;
		}
		if (!this.isStatic) {
			this.path = path.bind(this.path, getOrigin());
		} else {
			this.path = path.bindStatically(this.path, getOrigin());
		}

		final FullResolution fullResolution =
				this.path.getContext().fullResolution();

		fullResolution.start();
		try {
			this.path.target(getOrigin().distribute()).resolveAll(
					getOrigin()
					.resolver()
					.fullResolver(user, VALUE_REF_USAGE));
		} finally {
			fullResolution.end();
		}

		return this;
	}

}
