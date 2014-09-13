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
package org.o42a.core.object;

import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.SimpleUsage.simpleUsable;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;


public class ObjectContent implements UserInfo {

	static ObjectContent objectContent(Obj object, boolean clonesContent) {
		if (!object.meta().isUpdated()) {
			return object.getCloneOf().clonesContent();
		}
		return new ObjectContent(object, clonesContent);
	}

	private final Obj object;
	private Usable<SimpleUsage> usable;
	private final boolean clonesContent;

	private ObjectContent(Obj object, boolean clonesContent) {
		this.object = object;
		this.clonesContent = clonesContent;
	}

	public final Obj getObject() {
		return this.object;
	}

	@Override
	public final User<SimpleUsage> toUser() {
		return uses().toUser();
	}

	public final UseFlag selectUse(
			Analyzer analyzer,
			UseSelector<SimpleUsage> selector) {
		if (this.usable == null) {
			return analyzer.toUseCase().unusedFlag();
		}
		return this.usable.selectUse(analyzer, selector);
	}

	public final boolean isUsed(
			Analyzer analyzer,
			UseSelector<SimpleUsage> selector) {
		return selectUse(analyzer, selector).isUsed();
	}

	public final void useBy(UserInfo user) {
		if (user.isDummyUser()) {
			return;
		}
		uses().useBy(user, SIMPLE_USAGE);
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.clonesContent ? "ClonesContent[" : "Content[");
		out.append(this.object).append(']');

		return out.toString();
	}

	private final Usable<SimpleUsage> uses() {
		if (this.usable != null) {
			return this.usable;
		}

		this.usable = simpleUsable(this);

		return this.usable;
	}

}
