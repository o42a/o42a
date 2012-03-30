/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl;

import java.util.ArrayList;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;


public class PathRecorder extends PathTracker {

	private Scope start;
	private boolean absolute;
	private final ArrayList<Record> records;

	public PathRecorder(
			BoundPath path,
			PathResolver resolver,
			PathWalker walker,
			Scope start,
			boolean absolute) {
		super(path, resolver, walker);
		this.start = start;
		this.absolute = absolute;
		this.records = new ArrayList<Record>(path.length());
	}

	@Override
	public boolean replay(PathWalker walker) {
		if (this.absolute) {
			if (!walker.root(getPath(), this.start)) {
				return false;
			}
		} else {
			if (!walker.start(getPath(), this.start)) {
				return false;
			}
		}
		for (Record record : this.records) {
			if (!record.replay(walker)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean module(final Step step, final Obj module) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.module(step, module);
			}
		});
	}

	@Override
	public boolean skip(final Step step, final Scope scope) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.skip(step, scope);
			}
		});
	}

	@Override
	public boolean staticScope(final Step step, final Scope scope) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.staticScope(step, scope);
			}
		});
	}

	@Override
	public boolean up(
			final Container enclosed,
			final Step step,
			final Container enclosing) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.up(enclosed, step, enclosing);
			}
		});
	}

	@Override
	public boolean member(
			final Container container,
			final Step step,
			final Member member) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.member(container, step, member);
			}
		});
	}

	@Override
	public boolean dereference(
			final Obj linkObject,
			final Step step,
			final Link link) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.dereference(linkObject, step, link);
			}
		});
	}

	@Override
	public boolean arrayElement(
			final Obj array,
			final Step step,
			final ArrayElement element) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.arrayElement(array, step, element);
			}
		});
	}

	@Override
	public boolean refDep(
			final Obj object,
			final Step step,
			final Ref dependency) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.refDep(object, step, dependency);
			}
		});
	}

	@Override
	public boolean object(final Step step, final Obj object) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.object(step, object);
			}
		});
	}

	@Override
	public void pathTrimmed(BoundPath path, Scope root) {
		this.records.clear();
		if (!this.absolute) {
			this.absolute = true;
			this.start = root;
		}
		super.pathTrimmed(path, root);
	}

	@Override
	public void abortedAt(final Scope last, final Step brokenStep) {
		this.records.add(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				walker().abortedAt(last, brokenStep);
				return false;
			}
		});
	}

	@Override
	public boolean done(final Container result) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.done(result);
			}
		});
	}

	private boolean record(Record record) {
		if (!record.replay(walker())) {
			return false;
		}
		this.records.add(record);
		return true;
	}

	interface Record {

		boolean replay(PathWalker walker);

	}

}
