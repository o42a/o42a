/*
    Parser Tests
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
package org.o42a.ast.test.grammar.module;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.module;

import org.junit.Test;
import org.o42a.ast.module.ModuleNode;
import org.o42a.ast.module.SectionNode;
import org.o42a.ast.module.SubTitleNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ModuleTest extends GrammarTestCase {

	@Test
	public void moduleWithoutTitle() {

		final ModuleNode module = parse(
				"Use module 'Console'",
				"Foo := bar");

		assertThat(module.getSections().length, is(1));

		final SectionNode section = module.getSections()[0];

		assertNull(section.getTitle());
		assertNull(section.getSubTitle());
		assertThat(section.getContent().length, is(2));
	}

	@Test
	public void oneSection() {

		final ModuleNode module = parse(
				"Foo :=> bar",
				"=====",
				"Baz = 1");

		assertThat(module.getSections().length, is(1));

		final SectionNode section = module.getSections()[0];

		assertNotNull(section.getTitle());
		assertNotNull(section.getDeclarator());
		assertThat(
				section.getSubTitle().getPrefix().getType().getLength(),
				is(5));
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void sectionWithoutTitle() {

		final ModuleNode module = parse(
				"=====",
				"Baz = 1");

		assertThat(module.getSections().length, is(1));

		final SectionNode section = module.getSections()[0];

		assertNull(section.getTitle());
		assertThat(
				section.getSubTitle().getPrefix().getType().getLength(),
				is(5));
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void invalidTitle() {
		expectError("invalid_section_title");

		final ModuleNode module = parse(
				"Hello!",
				"=====",
				"Baz = 1");

		final SectionNode[] sections = module.getSections();

		assertThat(sections.length, is(2));

		assertNull(sections[0].getTitle());
		assertNull(sections[0].getSubTitle());
		assertThat(sections[0].getContent().length, is(1));

		assertNull(sections[1].getTitle());
		assertThat(
				sections[1].getSubTitle().getPrefix().getType().getLength(),
				is(5));
		assertThat(sections[1].getContent().length, is(1));
	}

	@Test
	public void multipleSections() {

		final ModuleNode module = parse(
				"===== Section 1 ======",
				"Baz = 1",
				"===== Section 2",
				"Foo",
				"",
				"===== Section 3 =");

		final SectionNode[] sections = module.getSections();

		assertThat(sections.length, is(3));

		assertNull(sections[0].getTitle());
		assertThat(
				sections[0].getSubTitle().getPrefix().getType().getLength(),
				is(5));
		assertName("section1", sections[0].getSubTitle().getTag());
		assertThat(
				sections[0].getSubTitle().getSuffix().getType().getLength(),
				is(6));
		assertThat(sections[0].getContent().length, is(0));

		assertNotNull(sections[1].getTitle());
		assertNotNull(sections[1].getDeclarator());
		assertThat(
				sections[1].getSubTitle().getPrefix().getType().getLength(),
				is(5));
		assertName("section2", sections[1].getSubTitle().getTag());
		assertNull(sections[1].getSubTitle().getSuffix());
		assertThat(sections[1].getContent().length, is(1));

		assertNull(sections[2].getTitle());
		assertThat(
				sections[2].getSubTitle().getPrefix().getType().getLength(),
				is(5));
		assertName("section3", sections[2].getSubTitle().getTag());
		assertNotNull(sections[2].getSubTitle().getSuffix());
		assertThat(sections[2].getContent().length, is(0));
	}

	@Test
	public void oddCharsAfterSubTitle() {
		expectError("odd");

		final ModuleNode module = parse(
				"=== Tag 1: tag 1-1 == /* comment */ odd");

		final SectionNode[] sections = module.getSections();

		assertThat(sections.length, is(1));

		final SectionNode section = sections[0];
		final SubTitleNode subTitle = section.getSubTitle();
		final MemberRefNode tag = subTitle.getTag();

		assertName("tag1", tag.getOwner());
		assertThat(tag.getName().getName(), is("tag1-1"));

		assertThat(subTitle.getPrefix().getType().getLength(), is(3));
		assertThat(subTitle.getSuffix().getType().getLength(), is(2));
	}

	private ModuleNode parse(String... lines) {
		return parseLines(module(), lines);
	}

}
