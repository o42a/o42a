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
package org.o42a.core.ref;

import org.o42a.analysis.use.*;
import org.o42a.core.Container;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.LinkUses;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;


public abstract class RefUsage extends Usage<RefUsage> {

	public static final AllUsages<RefUsage> ALL_REF_USAGES =
			new AllUsages<>(RefUsage.class);

	public static final RefUsage CONDITION_REF_USAGE =
			new ValueUsage("RefCondition");
	public static final RefUsage VALUE_REF_USAGE =
			new ValueUsage("RefValue");
	public static final RefUsage DEREF_USAGE =
			new ResolutionUsage("DerefValue", Role.INSTANCE);
	public static final RefUsage TYPE_REF_USAGE =
			new TypeUsage("TypeRef");
	public static final RefUsage TYPE_PARAMETER_REF_USAGE =
			new TypeParameterUsage("TypeParameterRef");
	public static final RefUsage CONTAINER_REF_USAGE =
			new ResolutionUsage("ContainerRef", Role.INSTANCE);
	public static final RefUsage BODY_REF_USAGE =
			new BodyUsage("BodyRef", Role.PROTOTYPE);
	public static final RefUsage TARGET_REF_USAGE =
			new BodyUsage("TargetRef", Role.INSTANCE);
	public static final RefUsage ASSIGNABLE_REF_USAGE =
			new ResolutionUsage("AssignableRef", Role.INSTANCE);
	public static final RefUsage TEMP_REF_USAGE = new TempUsage();

	public static final UseSelector<RefUsage> VALUE_REF_USAGES =
			CONDITION_REF_USAGE.or(VALUE_REF_USAGE);
	public static final UseSelector<RefUsage> NON_VALUE_REF_USAGES =
			VALUE_REF_USAGES.not();
	public static final UseSelector<RefUsage> NON_DEREF_USAGES =
			DEREF_USAGE.not();


	public static Uses<RefUsage> alwaysUsed() {
		return ALL_REF_USAGES.alwaysUsed();
	}

	public static Uses<RefUsage> neverUsed() {
		return ALL_REF_USAGES.neverUsed();
	}

	public static Usable<RefUsage> usable(Object used) {
		return ALL_REF_USAGES.usable(used);
	}

	public static Usable<RefUsage> usable(String name, Object used) {
		return ALL_REF_USAGES.usable(name, used);
	}

	private final Role role;

	private RefUsage(String name, Role role) {
		super(ALL_REF_USAGES, name);
		this.role = role;
	}

	public final Role getRole() {
		return this.role;
	}

	public boolean isCompileTimeOnly() {
		return false;
	}

	public void fullyResolve(FullResolver resolver, Container resolved) {
		resolveObject(resolved.toObject(), resolver);
	}

	protected abstract void resolveObject(Obj object, UserInfo user);

	private static final class ValueUsage extends RefUsage {

		ValueUsage(String name) {
			super(name, Role.INSTANCE);
		}

		@Override
		protected void resolveObject(Obj object, UserInfo user) {
			object.value().resolveAll(user);
		}

	}

	private static class TypeUsage extends RefUsage {

		TypeUsage(String name) {
			super(name, Role.PROTOTYPE);
		}

		@Override
		protected void resolveObject(Obj object, UserInfo user) {
			object.type().useBy(user);
		}

	}

	private static final class TypeParameterUsage extends RefUsage {

		TypeParameterUsage(String name) {
			super(name, Role.PROTOTYPE);
		}

		@Override
		public boolean isCompileTimeOnly() {
			return true;
		}

		@Override
		protected void resolveObject(Obj object, UserInfo user) {
			object.type().useBy(user);
		}

	}

	private static final class ResolutionUsage extends RefUsage {

		ResolutionUsage(String name, Role role) {
			super(name, role);
		}

		@Override
		public void fullyResolve(FullResolver resolver, Container resolved) {

			final Clause clause = resolved.toClause();

			if (clause != null) {
				clause.resolveAll();
			} else {
				resolveObject(resolved.toObject(), resolver);
			}
		}

		@Override
		protected void resolveObject(Obj object, UserInfo user) {
			object.resolveAll();
			object.type().useBy(user);
		}

	}

	private static final class BodyUsage extends RefUsage {

		BodyUsage(String name, Role role) {
			super(name, role);
		}

		@Override
		protected void resolveObject(Obj object, UserInfo user) {
			object.resolveAll();
			object.type().useBy(user);

			final LinkUses linkUses = object.type().linkUses();

			if (linkUses != null) {
				linkUses.useBodyBy(user);
			}
		}

	}

	private static final class TempUsage extends RefUsage {

		TempUsage() {
			super("TempRef", Role.ANY);
		}

		@Override
		public boolean isCompileTimeOnly() {
			return true;
		}

		@Override
		protected void resolveObject(Obj object, UserInfo user) {
			object.type().useBy(user);
		}

	}

}
