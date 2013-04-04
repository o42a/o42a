/*
    Compiler
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
package org.o42a.compiler.ip.ref.owner;

import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.ref.Ref;


public abstract class OwnerFactory {

	public static final OwnerFactory DEFAULT_OWNER_FACTORY =
			new DefaultOwnerFactory();
	public static final OwnerFactory NON_LINK_OWNER_FACTORY =
			new NonLinkOwnerFactory();

	public abstract Owner owner(AccessRules accessRules, Ref ownerRef);

	public abstract Owner nonLinkOwner(AccessRules accessRules, Ref ownerRef);

	private static final class DefaultOwnerFactory extends OwnerFactory {

		@Override
		public Owner owner(AccessRules accessRules, Ref ownerRef) {
			return new DefaultOwner(accessRules, ownerRef);
		}

		@Override
		public Owner nonLinkOwner(AccessRules accessRules, Ref ownerRef) {
			return new NonLinkOwner(accessRules, ownerRef);
		}

	}

	private static final class NonLinkOwnerFactory extends OwnerFactory {

		@Override
		public Owner owner(AccessRules accessRules, Ref ownerRef) {
			return new NeverDerefOwner(accessRules, ownerRef);
		}

		@Override
		public Owner nonLinkOwner(AccessRules accessRules, Ref ownerRef) {
			return new NeverDerefOwner(accessRules, ownerRef);
		}

	}

}
