/*
    Compiler Tests
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.compiler.test.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.o42a.util.Place.newTrace;

import org.junit.Test;
import org.o42a.util.Place;
import org.o42a.util.Place.Trace;


public class PlaceTest {

	@Test
	public void same() {

		final Place place = newTrace().next();

		assertNotVisible(place, place);
	}

	@Test
	public void simple() {

		final Trace trace = newTrace();
		final Place first = trace.next();
		final Place second = trace.next();

		assertVisible(first, second);
		assertNotVisible(second, first);
	}

	@Test
	public void enclosure() {

		final Trace trace = newTrace();
		final Place first = trace.next();
		final Place second = first.nestedTrace().next();
		final Place third = trace.next();

		assertVisible(first, second);
		assertVisible(second, third);
		assertNotVisible(third, second);
		assertNotVisible(second, first);
		assertVisible(first, third);
		assertNotVisible(third, first);
	}

	public static void assertVisible(Place target, Place viewer) {
		assertTrue(
				target + " is not visible by " + viewer,
				target.visibleBy(viewer));
	}

	public static void assertNotVisible(Place target, Place viewer) {
		assertFalse(
				target + " is visible by " + viewer,
				target.visibleBy(viewer));
	}

}
