/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import org.o42a.core.st.impl.InterrogativeSentence;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.DeclarativeFactory;
import org.o42a.core.st.sentence.Sentence;


public class DeclarativeInterrogationFactory extends DeclarativeFactory {

	@Override
	public Sentence declare(LocationInfo location, Block block) {
		return new InterrogativeSentence(location, block, this);
	}

}
