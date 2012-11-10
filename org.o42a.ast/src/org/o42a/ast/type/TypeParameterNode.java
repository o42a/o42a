package org.o42a.ast.type;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.CommaSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.util.io.SourcePosition;


public class TypeParameterNode extends AbstractNode {

	private final SignNode<CommaSign> separator;
	private final TypeNode type;

	public TypeParameterNode(SourcePosition start) {
		super(start, start);
		this.separator = null;
		this.type = null;
	}

	public TypeParameterNode(SignNode<CommaSign> separator, TypeNode type) {
		super(separator, type);
		this.separator = separator;
		this.type = type;
	}

	public final SignNode<CommaSign> getSeparator() {
		return this.separator;
	}


	public final TypeNode getType() {
		return this.type;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitTypeParameter(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.separator != null) {
			this.separator.printContent(out);
		}
		if (this.type != null) {
			this.type.printContent(out);
		}
	}

}
