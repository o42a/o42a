/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.st.impl.local;

import static org.o42a.core.st.sentence.Local.ANONYMOUS_LOCAL_NAME;

import org.o42a.core.Container;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.string.Name;


public final class Locals {

	private final LocalFactory factory;
	private final Locals enclosing;
	private final Statements statements;
	private final Local local;
	private Container container;

	public Locals(Block block, LocalFactory factory) {
		this.factory = factory;
		this.enclosing = null;
		this.statements = null;
		this.local = null;
		this.container = block.getContainer();
	}

	private Locals(Locals enclosing, Container container) {
		this.factory = enclosing.factory;
		this.enclosing = enclosing;
		this.statements = null;
		this.local = null;
		this.container = container;
	}

	private Locals(Locals enclosing, Statements statements, Local local) {
		this.factory = enclosing.factory;
		this.enclosing = enclosing;
		this.statements = statements;
		this.local = local;
	}

	public final boolean isEmpty() {
		if (this.enclosing == null) {
			return this.local == null;
		}
		return this.enclosing.isEmpty();
	}

	public final Local getLocal() {
		return this.local;
	}

	public final Container getContainer() {
		if (this.container != null) {
			return this.container;
		}
		return this.container = new LocalInsides(this.local);
	}

	public final Locals forBlock(Block block) {

		final Container container = block.getContainer();

		if (this.container == container) {
			return this;
		}

		return new Locals(this, container);
	}

	public final Locals declareLocal(
			Statements statements,
			LocationInfo location,
			Name name,
			Ref ref) {
		if (duplicateLocal(statements, location, name)) {
			return null;
		}

		final Local local = this.factory.createLocal(location, name, ref);

		return new Locals(this, statements, local);
	}

	private boolean duplicateLocal(
			Statements statements,
			LocationInfo location,
			Name name) {

		final Local existing = existingLocal(statements, name);

		if (existing == null) {
			return false;
		}

		location.getLocation().getLogger().error(
				"duplicate_local",
				location.getLocation().addAnother(existing),
				"Imperative block with name '%s' already declared",
				name);

		return true;
	}

	private Local existingLocal(Statements statements, Name name) {
		if (name.is(ANONYMOUS_LOCAL_NAME)) {
			// Anonymous locals allowed in enclosing blocks.
			return anonymousLocal(statements);
		}
		return localByName(name);
	}

	private Local localByName(Name name) {

		Locals locals = this;

		do {

			final Local local = locals.local;

			if (local != null && local.getName().is(name)) {
				return this.local;
			}

			locals = locals.enclosing;
		} while (locals != null);

		return null;
	}

	private Local anonymousLocal(Statements statements) {
		// Search for anonymous local in the same statements only.
		if (this.statements != statements) {
			return null;
		}

		Locals locals = this;

		do {

			final Local local = locals.local;

			if (local != null && local.getName().is(ANONYMOUS_LOCAL_NAME)) {
				return local;
			}

			locals = locals.enclosing;
		} while (locals != null && locals.statements == statements);

		return null;
	}

}
