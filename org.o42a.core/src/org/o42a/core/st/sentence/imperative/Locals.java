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
package org.o42a.core.st.sentence.imperative;

import java.util.HashSet;

import org.o42a.core.LocationInfo;
import org.o42a.core.st.sentence.DeclarativeBlock;


public class Locals {

	private final DeclarativeBlock block;
	private final HashSet<String> blockNames = new HashSet<String>();

	public Locals(DeclarativeBlock block) {
		this.block = block;
	}

	public final DeclarativeBlock getBlock() {
		return this.block;
	}

	public boolean declareBlock(LocationInfo location, String name) {
		if (this.blockNames.add(name)) {
			return true;
		}

		location.getContext().getLogger().error(
				"duplicate_block_name",
				location,
				"Block '%s' already declared",
				name);

		return false;
	}

}
