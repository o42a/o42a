/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
