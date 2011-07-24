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
package org.o42a.core.st.impl.declarative;

import org.o42a.core.source.LocationInfo;
import org.o42a.core.source.SectionTag;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;


public class ExplicitInclusion extends Inclusion {

	private final String tag;

	public ExplicitInclusion(
			LocationInfo location,
			Declaratives statements,
			String tag) {
		super(location, statements);
		this.tag = tag;
	}

	@Override
	public String toString() {
		if (this.tag == null) {
			return super.toString();
		}
		return "*** " + this.tag + " ***";
	}

	@Override
	protected void includeInto(DeclarativeBlock block) {

		final SectionTag tag = getContext().getSectionTag().append(this.tag);

		getContext().include(block, tag);
	}

}
