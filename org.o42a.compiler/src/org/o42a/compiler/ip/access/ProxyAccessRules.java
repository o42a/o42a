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
package org.o42a.compiler.ip.access;


public abstract class ProxyAccessRules extends AccessRules {

	private final AccessRules wrapped;

	public ProxyAccessRules(AccessRules wrapped) {
		super(wrapped.getSource());
		this.wrapped = wrapped;
	}

	public final AccessRules getWrapped() {
		return this.wrapped;
	}

	@Override
	public AccessRules typeRules() {

		final AccessRules wrapped = getWrapped();
		final AccessRules typeRules = wrapped.typeRules();

		if (wrapped == typeRules) {
			return this;
		}

		return wrap(typeRules);
	}

	@Override
	public AccessRules declarationRules() {

		final AccessRules wrapped = getWrapped();
		final AccessRules declarationRules = wrapped.declarationRules();

		if (wrapped == declarationRules) {
			return this;
		}

		return wrap(declarationRules);
	}

	@Override
	public AccessRules contentRules() {

		final AccessRules wrapped = getWrapped();
		final AccessRules contentRules = wrapped.contentRules();

		if (wrapped == contentRules) {
			return this;
		}

		return wrap(contentRules);
	}

	@Override
	public AccessRules clauseReuseRules() {

		final AccessRules wrapped = getWrapped();
		final AccessRules clauseReuseRules = wrapped.clauseReuseRules();

		if (wrapped == clauseReuseRules) {
			return this;
		}

		return wrap(clauseReuseRules);
	}

	@Override
	public String toString() {
		if (this.wrapped == null) {
			return super.toString();
		}
		return getClass().getSimpleName() + '[' + this.wrapped + ']';
	}

	protected abstract AccessRules wrap(AccessRules wrapped);

}
