/*
    Collections Library
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.lib.collections;

import org.o42a.common.object.*;
import org.o42a.core.source.CompilerContext;


@SourcePath("collections.o42a")
@RelatedSource("array_list.o42a")
@RelatedSource("collection.o42a")
@RelatedSource("iterator.o42a")
@RelatedSource("list.o42a")
@RelatedSource("list/sublist.o42a")
@RelatedSource("mutable_list.o42a")
@RelatedSource("mutable_list/sublist.o42a")
@RelatedSource("row_list.o42a")
public class CollectionsModule extends AnnotatedModule {

	public static CollectionsModule collectionsModule(
			CompilerContext parentContext) {
		return new CollectionsModule(
				parentContext,
				moduleSources(CollectionsModule.class));
	}

	private CollectionsModule(
			CompilerContext parentContext,
			AnnotatedSources sources) {
		super(parentContext, sources);
	}

}
