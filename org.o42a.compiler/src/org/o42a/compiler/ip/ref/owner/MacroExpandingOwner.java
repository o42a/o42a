/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.owner;

import static org.o42a.compiler.ip.macro.RequireMacroStep.requireMacro;

import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


final class MacroExpandingOwner extends Owner {

	private final Owner owner;
	private final LogInfo expansion;

	MacroExpandingOwner(Owner owner, LogInfo expansion) {
		super(owner.ownerRef);
		this.owner = owner;
		this.expansion = expansion;
	}

	@Override
	public boolean isMacroExpanding() {
		return super.isMacroExpanding();
	}

	@Override
	public Ref targetRef() {
		return requireMacro(this.owner.targetRef(), this.expansion);
	}

	@Override
	public Owner body(LocationInfo location, LocationInfo bodyRef) {
		return this.owner.body(location, bodyRef).expandMacro(this.expansion);
	}

	@Override
	public Owner deref(LocationInfo location, LocationInfo deref) {
		return this.owner.deref(location, deref).expandMacro(this.expansion);
	}

	@Override
	public Ref bodyRef() {
		return requireMacro(this.owner.bodyRef(), this.expansion);
	}

	@Override
	public String toString() {
		if (this.owner == null) {
			return '#' + super.toString();
		}
		return '#' + this.owner.toString();
	}

	@Override
	protected Owner memberOwner(Ref ref) {
		return this.owner.memberOwner(ref);
	}

}
