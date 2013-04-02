/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.type;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;


public class TypeDefinitionErrorsTest extends CompilerTestCase {

	@Test
	public void prohibitSelfRef() {
		expectError("compiler.prohibited_type_object_ref");

		compile("A :=> void #(T := :)");
	}

	@Test
	public void prohibitParentRef() {
		expectError("compiler.prohibited_type_object_ref");

		compile("A :=> void #(T := ::)");
	}

	@Test
	public void prohibitObjectRef() {
		expectError("compiler.prohibited_type_object_ref");

		compile("A :=> void #(T := a)");
	}

	@Test
	public void prohibitObjectRefByParentRef() {
		expectError("compiler.prohibited_type_object_ref");

		compile("A :=> void #(T := a::)");
	}

	@Test
	public void prohibitObjectRefByName() {
		expectError("compiler.prohibited_type_object_ref");

		compile("A :=> void #(T := a)");
	}

	@Test
	public void prohibitObjectRefByQualifiedName() {
		expectError("compiler.prohibited_type_object_ref");

		compile("A :=> void #(T := a @prohibit object ref by qualified name)");
	}

}
