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
package org.o42a.core.st.impl.imperative;

import java.util.HashMap;

import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.string.Name;


public class NamedBlocks {

	private final DeclarativeBlock block;
	private HashMap<Name, Location> blocks;

	public NamedBlocks(DeclarativeBlock block) {
		this.block = block;
	}

	public final DeclarativeBlock getBlock() {
		return this.block;
	}

	public boolean declareBlock(LocationInfo location, Name name) {
		if (this.blocks == null) {
			this.blocks = new HashMap<>();
		}

		final Location previousLocation =
				this.blocks.put(name, location.getLocation());

		if (previousLocation == null) {
			return true;
		}

		this.blocks.put(name, previousLocation);
		location.getLocation().getLogger().error(
				"duplicate_block_name",
				location.getLocation().addAnother(previousLocation),
				"Imperative block with name '%s' already declared",
				name);

		return false;
	}

}
