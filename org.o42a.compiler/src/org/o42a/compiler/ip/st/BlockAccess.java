/*
    Compiler
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
package org.o42a.compiler.ip.st;

import org.o42a.ast.Node;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.compiler.ip.access.ParentAccessRules;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.ScopeInfo;
import org.o42a.core.st.sentence.BlockBuilder;


public abstract class BlockAccess<T extends Node> extends BlockBuilder {

	private final T node;
	private final AccessRules accessRules;
	private final Container parent;

	public BlockAccess(T node, AccessDistributor distributor) {
		super(distributor.getContext(), node);
		this.node = node;
		this.accessRules = distributor.getAccessRules();
		this.parent= distributor.getContainer();
	}

	public final T getNode() {
		return this.node;
	}

	public final AccessRules getAccessRules() {
		return this.accessRules;
	}

	public final Container getParent() {
		return this.parent;
	}

	public final AccessDistributor distribute(Distributor distributor) {
		return accessRules(distributor)
				.distribute(distributor)
				.distributeIn(getContext());
	}

	public final AccessRules accessRules(ScopeInfo scoped) {
		if (scoped.getScope().is(getParent().getScope())) {
			return getAccessRules();
		}
		return new ParentAccessRules(getParent(), getAccessRules());
	}

	@Override
	public String toString() {
		if (this.node == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		this.node.printContent(out);

		return out.toString();
	}

}
