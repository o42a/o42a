/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.keeper;

import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;


final class KeepValueStatement extends Statement {

	private final Ref value;

	KeepValueStatement(
			LocationInfo location,
			Distributor distributor,
			Ref value) {
		super(location, distributor);
		this.value = value.rescope(getScope());
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public boolean isValid() {
		return getValue().isValid();
	}

	@Override
	public Command command(CommandEnv env) {
		return new KeptValueCommand(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatibleScope(reproducer.getReproducingScope());

		final Ref value = this.value.reproduce(reproducer);

		if (value == null) {
			return null;
		}

		return new KeepValueStatement(this, reproducer.distribute(), value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "= //" + this.value;
	}

}
