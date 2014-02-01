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
package org.o42a.core.source;

import static org.o42a.util.log.LogDetail.logDetail;

import org.o42a.util.log.LogDetail;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


public final class Location implements LocationInfo, LogInfo {

	public static final LogDetail ANOTHER_LOG_DETAIL =
			logDetail("compiler.another", "Another location");

	public static final LogDetail DECLARATION_LOG_DETAIL =
			logDetail("compiler.declration", "Declaration");

	private final CompilerContext context;
	private final Loggable loggable;

	public Location(Location location) {
		assert location != null :
			"Location not specified";
		this.context = location.getContext();
		this.loggable = location.getLoggable();
	}

	public Location(LocationInfo location) {
		this(location.getLocation());
	}

	public Location(CompilerContext context, LogInfo logInfo) {
		assert context != null :
			"Compiler context not specified";
		assert logInfo != null :
			"Log info not specified";
		this.context = context;
		this.loggable = logInfo.getLoggable();
	}

	@Override
	public final Location getLocation() {
		return this;
	}

	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public final Loggable getLoggable() {
		return this.loggable;
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	public final Location setDeclaration(Location declaration) {
		return setDeclaration(declaration.getLoggable());
	}

	public final Location setDeclaration(LocationInfo declaration) {
		return setDeclaration(declaration.getLocation().getLoggable());
	}

	public final Location setDeclaration(LogInfo declaration) {
		return addDetailLocation(DECLARATION_LOG_DETAIL, declaration);
	}

	public final Location addAnother(Location another) {
		return addAnother(another.getLoggable());
	}

	public final Location addAnother(LocationInfo another) {
		return addAnother(another.getLocation().getLoggable());
	}

	public final Location addAnother(LogInfo another) {
		return addDetail(ANOTHER_LOG_DETAIL, another);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName()).append('[');
		out.append(this.context);

		final Loggable loggable = getLoggable();

		if (loggable != null) {
			out.append("]:");
			loggable.print(out);
		} else {
			out.append(']');
		}

		return out.toString();
	}

	private Location addDetail(LogDetail detail, LogInfo location) {
		return new Location(
				getContext(),
				getLoggable().addDetail(detail, location));
	}

	private Location addDetailLocation(LogDetail detail, LogInfo location) {
		return new Location(
				getContext(),
				getLoggable().addDetailLocation(detail, location));
	}

}
