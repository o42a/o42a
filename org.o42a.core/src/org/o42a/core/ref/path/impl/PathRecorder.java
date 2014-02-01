/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


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
		this.records = new ArrayList<>(path.length());
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
			final Container enclosing,
			final ReversePath reversePath) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.up(enclosed, step, enclosing, reversePath);
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
	public boolean local(
			final Scope scope,
			final Local local) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.local(scope, local);
			}
		});
	}

	@Override
	public boolean dep(
			final Obj object,
			final Dep dep) {
		return record(new Record() {
			@Override
			public boolean replay(PathWalker walker) {
				return walker.dep(object, dep);
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
	public boolean pathTrimmed(BoundPath path, Scope root) {
		this.records.clear();
		this.absolute = true;
		this.start = root;
		return super.pathTrimmed(path, root);
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
