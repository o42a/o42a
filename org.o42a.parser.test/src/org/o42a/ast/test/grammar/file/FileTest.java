/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.file;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.file;

import org.junit.Test;
import org.o42a.ast.file.FileNode;
import org.o42a.ast.file.SectionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class FileTest extends GrammarTestCase {

	@Test
	public void moduleWithoutTitle() {

		final FileNode file = parse(
				"Use module 'Console'",
				"Foo := bar");

		assertThat(file.getHeader(), nullValue());

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), nullValue());
		assertThat(section.getSubTitle(), nullValue());
		assertThat(section.getTypeDefinition(), nullValue());
		assertThat(section.getContent().length, is(2));
	}

	@Test
	public void moduleWithTypeDefinitionOnly() {

		final FileNode file = parse(
				"T := void",
				"####");

		assertThat(file.getHeader(), nullValue());

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), nullValue());
		assertThat(section.getSubTitle(), nullValue());
		assertThat(section.getTypeDefinition(), notNullValue());
		assertThat(section.getTypeDefinition().getContent().length, is(1));
		assertThat(section.getContent().length, is(0));
	}

	@Test
	public void moduleWithTypeDefinition() {

		final FileNode file = parse(
				"T := void",
				"####",
				"Foo := bar");

		assertThat(file.getHeader(), nullValue());

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), nullValue());
		assertThat(section.getSubTitle(), nullValue());
		assertThat(section.getTypeDefinition(), notNullValue());
		assertThat(section.getTypeDefinition().getContent().length, is(1));
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void oneSection() {

		final FileNode file = parse(
				"Foo :=> bar",
				"===========",
				"Baz = 1");

		assertThat(file.getHeader(), nullValue());

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), notNullValue());
		assertThat(section.getDeclarator(), notNullValue());
		assertThat(
				section.getSubTitle().getPrefix().getType().getLength(),
				is(11));
		assertThat(section.getTypeDefinition(), nullValue());
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void sectionWithTypeDefinition() {

		final FileNode file = parse(
				"Foo :=> bar",
				"===========",
				"T := void",
				"V := integer",
				"####",
				"Baz = 1");

		assertThat(file.getHeader(), nullValue());

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), notNullValue());
		assertThat(section.getDeclarator(), notNullValue());
		assertThat(
				section.getSubTitle().getPrefix().getType().getLength(),
				is(11));
		assertThat(section.getTypeDefinition().getContent().length, is(2));
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void header() {

		final FileNode file = parse(
				"Use namespace 'Console'",
				"Foo := bar",
				"==========",
				"Baz = 1");

		final SectionNode header = file.getHeader();

		assertThat(header, notNullValue());
		assertThat(header.getTitle(), nullValue());
		assertThat(header.getSubTitle(), nullValue());
		assertThat(header.getContent().length, is(1));

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), notNullValue());
		assertThat(section.getDeclarator(), notNullValue());
		assertThat(
				section.getSubTitle().getPrefix().getType().getLength(),
				is(10));
		assertThat(section.getTypeDefinition(), nullValue());
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void sectionWithoutTitle() {

		final FileNode file = parse(
				"~~ comment",
				"=====",
				"Baz = 1");

		assertThat(file.getHeader(), nullValue());

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), nullValue());
		assertThat(
				section.getSubTitle().getPrefix().getType().getLength(),
				is(5));
		assertThat(section.getTypeDefinition(), nullValue());
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void invalidTitle() {
		expectError("invalid_section_title");

		final FileNode file = parse(
				"Hello!",
				"======",
				"Baz = 1");

		final SectionNode header = file.getHeader();

		assertThat(header, notNullValue());
		assertThat(header.getTitle(), nullValue());
		assertThat(header.getSubTitle(), nullValue());
		assertThat(header.getContent().length, is(1));

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());
		assertThat(section.getTitle(), nullValue());
		assertThat(
				section.getSubTitle().getPrefix().getType().getLength(),
				is(6));
		assertThat(section.getTypeDefinition(), nullValue());
		assertThat(section.getContent().length, is(1));
	}

	@Test
	public void eof() {

		final FileNode file = parse(file(), "void");

		assertThat(file.getHeader(), nullValue());

		final SectionNode section = file.getSection();

		assertThat(section, notNullValue());

		final SentenceNode[] content = section.getContent();

		assertThat(content.length, is(1));

		final MemberRefNode ref =
				singleStatement(MemberRefNode.class, content[0]);

		assertThat(ref, isName("void"));
	}

	private FileNode parse(String... lines) {
		return parseLines(file(), lines);
	}

}
