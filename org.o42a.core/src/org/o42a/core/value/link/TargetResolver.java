/*
    Compiler Core
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
package org.o42a.core.value.link;

import org.o42a.core.object.Obj;


public abstract class TargetResolver {

	public static TargetResolver wrapTargetTypeBy(Obj wrapper) {
		return new TargetTypeWrapper(wrapper);
	}

	public static TargetResolver wrapTargetValueBy(Obj wrapper) {
		return new TargetValueWrapper(wrapper);
	}

	public abstract void resolveTarget(Obj target);

	private static final class TargetTypeWrapper extends TargetResolver {

		private final Obj wrapper;

		TargetTypeWrapper(Obj wrapper) {
			this.wrapper = wrapper;
		}

		@Override
		public void resolveTarget(Obj target) {
			target.type().wrapBy(this.wrapper.type());
		}

		@Override
		public String toString() {
			return "TargetTypeWrapper[" + this.wrapper + ']';
		}

	}

	private static final class TargetValueWrapper extends TargetResolver {

		private final Obj wrapper;

		TargetValueWrapper(Obj wrapper) {
			this.wrapper = wrapper;
		}

		@Override
		public void resolveTarget(Obj target) {
			target.value().wrapBy(this.wrapper.value());
		}

		@Override
		public String toString() {
			return "TargetValueWrapper[" + this.wrapper + ']';
		}

	}

}