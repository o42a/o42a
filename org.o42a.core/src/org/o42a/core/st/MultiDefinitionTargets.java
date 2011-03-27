/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.st;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


final class MultiDefinitionTargets extends DefinitionTargets {

	private final HashMap<DefinitionKey, Entry> targets;
	private final DefinitionTarget firstDeclaration;
	private final DefinitionTarget lastDeclaration;

	MultiDefinitionTargets(
			DefinitionTargets targets1,
			DefinitionTargets targets2) {
		super((byte) (targets1.mask() | targets2.mask()));

		final Map<DefinitionKey, Entry> t1 = targets1.targets();
		final Map<DefinitionKey, Entry> t2 = targets2.targets();

		this.targets = new HashMap<DefinitionKey, Entry>(t1.size() + t2.size());

		for (Map.Entry<DefinitionKey, Entry> e : t1.entrySet()) {
			this.targets.put(e.getKey(), e.getValue().add(t2.get(e.getKey())));
		}
		for (Map.Entry<DefinitionKey, Entry> e : t2.entrySet()) {

			final DefinitionKey key = e.getKey();

			if (!this.targets.containsKey(key)) {
				this.targets.put(key, e.getValue());
			}
		}

		final DefinitionTarget firstDeclaration = targets1.firstDeclaration();

		if (firstDeclaration != null) {
			this.firstDeclaration = firstDeclaration;
		} else {
			this.firstDeclaration = targets2.firstDeclaration();
		}

		final DefinitionTarget lastDeclaration = targets2.lastDeclaration();

		if (lastDeclaration != null) {
			this.lastDeclaration = lastDeclaration;
		} else {
			this.lastDeclaration = targets1.lastDeclaration();
		}
	}

	@Override
	public final DefinitionTarget firstDeclaration() {
		return this.firstDeclaration;
	}

	@Override
	public final DefinitionTarget lastDeclaration() {
		return this.lastDeclaration;
	}

	@Override
	public final DefinitionTarget first(DefinitionKey key) {

		final Entry entry = this.targets.get(key);

		return entry != null ? entry.getLast() : null;
	}

	@Override
	public final DefinitionTarget last(DefinitionKey key) {

		final Entry entry = this.targets.get(key);

		return entry != null ? entry.getLast() : null;
	}

	@Override
	public final Iterator<DefinitionKey> iterator() {
		return this.targets.keySet().iterator();
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "DefinitionTargets[]";
		}

		final StringBuilder out = new StringBuilder();
		boolean comma = false;

		out.append("DefinitionTargets[");
		for (DefinitionKey key : this.targets.keySet()) {
			if (comma) {
				out.append(", ");
			}
			out.append(key);
		}
		out.append(']');

		return out.toString();
	}

	@Override
	final Map<DefinitionKey, Entry> targets() {
		return this.targets;
	}

}
