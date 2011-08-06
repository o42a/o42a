/*
    Compiler JNI Bindings to LLVM
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
#include "o42ac/llvm/BackendModule.h"

#include "o42ac/llvm/debug.h"

#include "llvm/DerivedTypes.h"
#include "llvm/LLVMContext.h"
#include "llvm/PassManager.h"
#include "llvm/Analysis/Verifier.h"
#include "llvm/Assembly/PrintModulePass.h"
#include "llvm/CodeGen/AsmPrinter.h"
#include "llvm/MC/MCContext.h"
#include "llvm/MC/MCStreamer.h"
#include "llvm/Support/CommandLine.h"
#include "llvm/Support/FormattedStream.h"
#include "llvm/Support/Host.h"
#include "llvm/Target/SubtargetFeature.h"
#include "llvm/Target/TargetData.h"
#include "llvm/Target/TargetMachine.h"
#include "llvm/Target/TargetRegistry.h"
#include "llvm/Target/TargetSelect.h"
#include "llvm/Transforms/Scalar.h"


using namespace llvm;


namespace o42ac {

static cl::opt<std::string> OutputFilename(
		"o",
		cl::Prefix,
		cl::ValueRequired,
		cl::desc("Output file name. Print to standard output if omitted."),
		cl::value_desc("output file"));

static cl::opt<std::string> InputFilename(
		cl::Positional,
		cl::desc("Input file name."),
		cl::value_desc("input file"));

static cl::opt<cl::boolOrDefault> Debug(
		"rt-debug",
		cl::ValueOptional,
		cl::desc(
				"Whether compiled code contains run-time debug info. "
				"Disabled by default. "
				"Enabled if value is omitted."),
		cl::value_desc("0/1"));

enum OutputFormat {
	OUTF_LL,
	OUTF_ASM,
	OUTF_OBJ,
};

static cl::opt<OutputFormat> OutFormat(
		"format",
		cl::ValueRequired,
		cl::desc("Set the output format"),
		cl::values(
				clEnumValN(OUTF_LL, "ll", "LLVM assembly"),
				clEnumValN(OUTF_ASM, "s", "assembly"),
				clEnumValN(OUTF_OBJ, "o", "object (the default)"),
				clEnumValEnd));

BackendModule::BackendModule(StringRef ModuleID, LLVMContext &context) :
		Module(ModuleID, context),
		targetData(NULL),
		functionPassManager(NULL),
		types(),
		stackSaveFunc(NULL),
		stackRestoreFunc(NULL) {
}

BackendModule::~BackendModule() {
	if (this->targetData) {
		delete this->targetData;
	}
	if (this->functionPassManager) {
		delete this->functionPassManager;
	}

	const size_t size = this->types.size();

	for (size_t i = 0; i < size; ++i) {
		delete this->types[i];
	}
}

void BackendModule::initializeTargets() {
	OTRACE("--------- Initializing\n");
	InitializeAllTargetInfos();
	InitializeAllTargets();
	InitializeAllAsmPrinters();
}

const std::string *BackendModule::getInputFilename() {
	if (!InputFilename.getNumOccurrences()) {
		return NULL;
	}
	return &InputFilename;
}

bool BackendModule::isDebugEnabled() {
	if (!Debug.getNumOccurrences()) {
		return false;
	}
	return Debug.getValue() != cl::BOU_FALSE;
}

BackendModule *BackendModule::createBackend(StringRef &ModuleID) {
	return new BackendModule(ModuleID, *new LLVMContext());
}

const TargetData &BackendModule::getTargetData() const {
	if (this->targetData) {
		return *this->targetData;
	}

	this->targetData = new TargetData(this);

	return *this->targetData;
}

PATypeHolder *BackendModule::newOpaqueType() {

	PATypeHolder *holder = new PATypeHolder(OpaqueType::get(getContext()));

	this->types.push_back(holder);

	return holder;
}

Constant *BackendModule::getStackSaveFunc() {
	if (this->stackSaveFunc) {
		return this->stackSaveFunc;
	}

	FunctionType *type =
			FunctionType::get(Type::getInt8PtrTy(getContext()), false);

	return this->stackSaveFunc =
			getOrInsertFunction("llvm.stacksave", type);
}

Constant *BackendModule::getStackRestoreFunc() {
	if (this->stackRestoreFunc) {
		return this->stackRestoreFunc;
	}

	std::vector<const Type*> args(1);

	args[0] = Type::getInt8PtrTy(getContext());

	FunctionType *type = FunctionType::get(
			Type::getVoidTy(getContext()),
			args,
			false);

	return this->stackRestoreFunc =
			getOrInsertFunction("llvm.stackrestore", type);
}

bool BackendModule::validateFunction(Function *const function) {
	OTRACE("--------- Validating " << function->getName() << "\n");

	if (verifyFunction(*function, PrintMessageAction)) {
		ODEBUG("--------- Validation failed: " << function->getName() << "\n");
		ODDUMP(function);
		ODEBUG("---------\n");

		return false;
	}

	OTRACE("--------- Validation succeed: " << function->getName() << "\n");

	if (!this->functionPassManager) {
		this->functionPassManager = new FunctionPassManager(this);
		this->functionPassManager->add(new TargetData(this->getTargetData()));
		// Do simple "peephole" optimizations and bit-twiddling optzns.
		this->functionPassManager->add(createInstructionCombiningPass());
		// Reassociate expressions.
		this->functionPassManager->add(createReassociatePass());
		// Eliminate Common SubExpressions.
		this->functionPassManager->add(createGVNPass());
		// Simplify the control flow graph (deleting unreachable blocks, etc).
		this->functionPassManager->add(createCFGSimplificationPass());
	}

	this->functionPassManager->run(*function);

	return true;
}

bool BackendModule::writeCode() {

	OTRACE("========= " << this->getModuleIdentifier() << " verification\n");

	if (verifyModule(*this, PrintMessageAction)) {
		return false;
	}

	OTRACE("========== " << this->getModuleIdentifier() << " generation\n");

	PassManager pm;

	std::string triple = getTargetTriple();
	std::string features;

	if (triple.empty()) {
		triple = sys::getHostTriple();

		StringMap<bool> featureMap;
		SubtargetFeatures f;

		f.getDefaultSubtargetFeatures(sys::getHostCPUName(), Triple(triple));

		f.setCPU(sys::getHostCPUName());
		if (sys::getHostCPUFeatures(featureMap)) {
			for (StringMapIterator<bool> it = featureMap.begin();
					it != featureMap.end();) {
				f.AddFeature(it->getKey(), it->getValue());
			}
		}

		features = f.getString();
	}

	std::string error =
			std::string("Target not supported: ").append(triple);

	ODEBUG("Emitting code for " << triple << " (" << features << ")\n");

	const Target *const target = TargetRegistry::lookupTarget(triple, error);

	if (!target) {
		return false;
	}

	TargetMachine *const machine =
			target->createTargetMachine(getTargetTriple(), features);

	formatted_raw_ostream *out;
	std::auto_ptr<formatted_raw_ostream> outPtr;

	if (!OutputFilename.getNumOccurrences()) {
		out = &fouts();
	} else {

		std::string errorInfo;

		raw_fd_ostream *const ostream =
				new raw_fd_ostream(
						OutputFilename.getValue().c_str(),
						errorInfo,
						raw_fd_ostream::F_Binary);

		if (!errorInfo.empty()) {
			errs() << errorInfo << '\n';
			return false;
		}

		out = new formatted_raw_ostream(*ostream, true);
		outPtr.reset(out);
	}

	TargetMachine::CodeGenFileType fileType = TargetMachine::CGFT_ObjectFile;

	if (OutFormat.getNumOccurrences()) {
		switch (OutFormat.getValue()) {
		case OUTF_LL:
			fileType = TargetMachine::CGFT_Null;
			break;
		case OUTF_ASM:
			fileType = TargetMachine::CGFT_AssemblyFile;
			break;
		case OUTF_OBJ:
			break;
		}
	}

	if (fileType == TargetMachine::CGFT_Null) {
		pm.add(createPrintModulePass(out, false));
	} else if (machine->addPassesToEmitFile(
			pm,
			*out,
			fileType,
			CodeGenOpt::Default)) {
		errs() << "Can not emit code\n";
		return false;
	}

	pm.run(*this);

	return true;
}

void BackendModule::destroyBackend() {

	LLVMContext *context = &this->getContext();

	delete this;
	delete context;
}

} /* o42a */
