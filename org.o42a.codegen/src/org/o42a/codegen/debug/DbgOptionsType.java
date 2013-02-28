package org.o42a.codegen.debug;

import static org.o42a.util.string.ID.rawId;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Int8rec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public class DbgOptionsType extends Type<DbgOptionsType.Op> {

	public static final DbgOptionsType DBG_OPTIONS_TYPE = new DbgOptionsType();

	private Int8rec noDebugMessages;
	private Int8rec debugBlocksOmitted;
	private Int8rec silentCalls;

	private DbgOptionsType() {
		super(rawId("o42a_dbg_options_t"));
	}

	@Override
	public boolean isDebugInfo() {
		return true;
	}

	public final Int8rec debugBlocksOmitted() {
		return this.debugBlocksOmitted;
	}

	public final Int8rec noDebugMessages() {
		return this.noDebugMessages;
	}

	public final Int8rec silentCalls() {
		return this.silentCalls;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.noDebugMessages = data.addInt8("no_debug_messages");
		this.debugBlocksOmitted = data.addInt8("debug_blocks_omitted");
		this.silentCalls = data.addInt8("silent_calls");
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

}
