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
package org.o42a.core.object.value;

import static org.o42a.core.object.value.ValuePartUsage.VALUE_PART_ACCESS;
import static org.o42a.core.object.value.ValuePartUsage.VALUE_PART_USAGE;
import static org.o42a.core.object.value.ValueUsage.VALUE_USAGE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;


final class ValueDefsUses implements UserInfo {

	private final ObjectValueDefs part;
	private Usable<ValuePartUsage> uses;

	ValueDefsUses(ObjectValueDefs part) {
		this.part = part;
	}

	public final Obj getObject() {
		return this.part.getObject();
	}

	public final ObjectValue getObjectValue() {
		return this.part.getObjectValue();
	}

	@Override
	public final User<ValuePartUsage> toUser() {
		return uses().toUser();
	}

	public final void useBy(UserInfo user) {
		if (!user.isDummyUser()) {
			uses().useBy(user, VALUE_PART_ACCESS);
		}
	}

	public final UseFlag selectUse(
			Analyzer analyzer,
			UseSelector<ValuePartUsage> selector) {
		return uses().selectUse(analyzer, selector);
	}

	public final boolean isUsed(
			Analyzer analyzer,
			UseSelector<ValuePartUsage> selector) {
		return selectUse(analyzer, selector).isUsed();
	}

	@Override
	public String toString() {
		if (this.part == null) {
			return super.toString();
		}
		return "ValueDefsUsesOf[" + getObject() + ']';
	}

	final void wrapBy(ValueDefsUses wrapUses) {
		uses().useBy(
				wrapUses.uses().usageUser(VALUE_PART_USAGE),
				VALUE_PART_USAGE);
		uses().useBy(
				wrapUses.uses().usageUser(VALUE_PART_ACCESS),
				VALUE_PART_ACCESS);
	}

	final Usable<ValuePartUsage> uses() {
		if (this.uses != null) {
			return this.uses;
		}

		this.uses = ValuePartUsage.usable(this);

		final ObjectValueBase objectValue = getObjectValue();
		final Usable<ValueUsage> valueUses = objectValue.uses();

		valueUses.useBy(this.uses, VALUE_USAGE);
		this.uses.useBy(valueUses, VALUE_PART_USAGE);

		return this.uses;
	}

}
