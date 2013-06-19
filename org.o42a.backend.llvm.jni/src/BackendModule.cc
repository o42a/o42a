/*
    Compiler JNI Bindings to LLVM
    Copyright (C) 2010-2013 Ruslan Lopatin

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

#include "llvm/PassManager.h"
#include "llvm/Analysis/Verifier.h"
#include "llvm/Assembly/PrintModulePass.h"
#include "llvm/CodeGen/AsmPrinter.h"
#include "llvm/IR/DataLayout.h"
#include "llvm/IR/DerivedTypes.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/MC/MCContext.h"
#include "llvm/MC/MCStreamer.h"
#include "llvm/MC/SubtargetFeature.h"
#include "llvm/Support/CommandLine.h"
#include "llvm/Support/FormattedStream.h"
#include "llvm/Support/Host.h"
#include "llvm/Support/TargetRegistry.h"
#include "llvm/Support/TargetSelect.h"
#include "llvm/Target/TargetMachine.h"
#include "llvm/Transforms/Scalar.h"


using namespace llvm;


namespace o42ac {

static cl::opt<std::string> InputFilename(
		cl::Positional,
		cl::desc("Input file name"),
		cl::value_desc("input file"));

static cl::opt<std::string> InputEncoding(
		"encoding",
		cl::Optional,
		cl::ValueRequired,
		cl::desc("Encoding of input files (UTF-8 by default)"),
		cl::value_desc("encoding"));

static cl::alias ShortInputEncoding(
		"E",
		cl::aliasopt(InputEncoding),
		cl::Prefix,
		cl::desc("Alias of -encoding option"));

static cl::opt<cl::boolOrDefault> Debug(
		"rt-debug",
		cl::ValueOptional,
		cl::desc(
				"Whether compiled code contains run-time debug info. "
				"Disabled by default. "
				"Enabled if value is omitted"),
		cl::value_desc("0/1"));

static cl::opt<std::string> OutputFilename(
		"o",
		cl::Prefix,
		cl::ValueRequired,
		cl::desc("Output file name. Print to standard output if omitted"),
		cl::value_desc("output file"));

enum OutputFormat {
	OUTF_LL,
	OUTF_ASM,
	OUTF_OBJ,
};

static cl::opt<OutputFormat> OutFormat(
		"format",
		cl::ValueRequired,
		cl::desc("Override the output format"),
		cl::values(
				clEnumValN(OUTF_LL, "ll", "LLVM assembly"),
				clEnumValN(OUTF_ASM, "s", "assembly"),
				clEnumValN(OUTF_OBJ, "o", "object"),
				clEnumValEnd));

static cl::alias ShortOutFormat(
		"F",
		cl::aliasopt(OutFormat),
		cl::Prefix,
		cl::desc("Alias of -format option"));

static cl::opt<cl::boolOrDefault> AnalyzeUses(
		"analyze-uses",
		cl::ValueOptional,
		cl::Hidden,
		cl::desc(
				"Enables or disables the uses analysis. "
				"Enabled by default"),
		cl::value_desc("0/1"));

static cl::opt<cl::boolOrDefault> Normalize(
		"normalize",
		cl::ValueOptional,
		cl::Hidden,
		cl::desc(
				"Enables or disables the normalization. "
				"Enabled by default"),
		cl::value_desc("0/1"));

BackendModule::BackendModule(StringRef ModuleID, LLVMContext &context) :
		Module(ModuleID, context),
		targetMachine(),
		targetDataLayout(),
		functionPassManager(),
		stackSaveFunc(),
		stackRestoreFunc(),
		hostMachine() {
}

BackendModule::~BackendModule() {
	if (this->targetMachine) {
		delete this->targetMachine;
	} else if (this->targetDataLayout) {
		delete this->targetDataLayout;
	}
	if (this->functionPassManager) {
		delete this->functionPassManager;
	}
}

void BackendModule::initializeTargets() {
	OTRACE("--------- Initializing\n");
	InitializeAllTargetInfos();
	InitializeAllTargets();
	InitializeAllTargetMCs();
	InitializeAllAsmPrinters();
}

const std::string *BackendModule::getInputFilename() {
	if (!InputFilename.getNumOccurrences()) {
		return NULL;
	}
	return &InputFilename;
}

const std::string *BackendModule::getInputEncoding() {
	if (!InputEncoding.getNumOccurrences()) {
		return NULL;
	}
	return &InputEncoding;
}

int BackendModule::debugEnabled() {
	if (!Debug.getNumOccurrences()) {
		return 0;
	}
	return Debug.getValue() != cl::BOU_FALSE ? 1 : -1;
}

int BackendModule::usesAnalysed() {
	if (!AnalyzeUses.getNumOccurrences()) {
		return 0;
	}
	return AnalyzeUses.getValue() != cl::BOU_FALSE ? 1 : -1;
}

int BackendModule::normalizationEnabled() {
	if (!Normalize.getNumOccurrences()) {
		return 0;
	}
	return Normalize.getValue() != cl::BOU_FALSE ? 1 : -1;
}

BackendModule *BackendModule::createBackend(StringRef &ModuleID) {

	BackendModule *backend = new BackendModule(ModuleID, *new LLVMContext());

	std::string triple = backend->getTargetTriple();

	if (triple.empty()) {
		backend->hostMachine = true;
		backend->setTargetTriple(sys::getDefaultTargetTriple());
	}

	backend->setDataLayout(
			backend->getTargetDataLayout()->getStringRepresentation());

	return backend;
}

TargetMachine *BackendModule::getTargetMachine() const {
	if (this->targetMachine) {
		return this->targetMachine;
	}

	std::string triple = getTargetTriple();
	std::string features;
	std::string CPU;

	if (this->hostMachine) {
		CPU = sys::getHostCPUName();

		StringMap<bool> featureMap;
		SubtargetFeatures f;

		f.getDefaultSubtargetFeatures(Triple(triple));
		if (sys::getHostCPUFeatures(featureMap)) {
			for (StringMapIterator<bool> it = featureMap.begin();
					it != featureMap.end();) {
				f.AddFeature(it->getKey(), it->getValue());
			}
		}

		features = f.getString();
	}

	std::string error = std::string("Target not supported: ").append(triple);

	ODEBUG("Emitting code for " << triple << " (" << features << ")\n");

	const Target *const target = TargetRegistry::lookupTarget(triple, error);

	if (!target) {
		return NULL;
	}

	return this->targetMachine = target->createTargetMachine(
			getTargetTriple(),
			CPU,
			features,
			TargetOptions());
}

const llvm::DataLayout *BackendModule::getTargetDataLayout() const {
	if (this->targetDataLayout) {
		return this->targetDataLayout;
	}

	TargetMachine *const machine = getTargetMachine();

	if (!machine) {
		return this->targetDataLayout = new llvm::DataLayout(this);
	}

	return this->targetDataLayout = machine->getDataLayout();
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

	FunctionType *type = FunctionType::get(
			Type::getVoidTy(getContext()),
			ArrayRef<Type*>(Type::getInt8PtrTy(getContext())),
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
		this->functionPassManager->add(
				new llvm::DataLayout(*this->getTargetDataLayout()));
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
		if (OutFormat.getValue() != OUTF_LL) {
			return false;
		}
	}

	OTRACE("========== " << this->getModuleIdentifier() << " generation\n");

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

	PassManager pm;

	if (fileType == TargetMachine::CGFT_Null) {
		pm.add(createPrintModulePass(out, false));
	} else {

		TargetMachine *const machine = getTargetMachine();

		if (!machine || machine->addPassesToEmitFile(
				pm,
				*out,
				fileType,
				CodeGenOpt::Default)) {
			errs() << "Can not emit code\n";
			return false;
		}
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
