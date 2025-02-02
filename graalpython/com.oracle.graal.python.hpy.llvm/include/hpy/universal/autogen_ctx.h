/* MIT License
 *  
 * Copyright (c) 2020, 2023, Oracle and/or its affiliates.
 * Copyright (c) 2019 pyhandle
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


/*
   DO NOT EDIT THIS FILE!

   This file is automatically generated by hpy.tools.autogen.ctx.autogen_ctx_h
   See also hpy.tools.autogen and hpy/tools/public_api.h

   Run this to regenerate:
       make autogen

*/

typedef HPy* _HPyPtr;
typedef HPyField* _HPyFieldPtr;
typedef HPy _HPyConst;
typedef HPyGlobal* _HPyGlobalPtr;

#ifdef GRAALVM_PYTHON_LLVM
#define HPy void*
#define HPyListBuilder void*
#define HPyTupleBuilder void*
#define HPyTracker void*
#define HPyField void*
#define HPyThreadState void*
#define HPyGlobal void*
#define _HPyCapsule_key int32_t
#endif


struct _HPyContext_s {
    const char *name; // used just to make debugging and testing easier
    void *_private;   // used by implementations to store custom data
    int ctx_version;
    _HPyConst h_None;
    _HPyConst h_True;
    _HPyConst h_False;
    _HPyConst h_NotImplemented;
    _HPyConst h_Ellipsis;
    _HPyConst h_BaseException;
    _HPyConst h_Exception;
    _HPyConst h_StopAsyncIteration;
    _HPyConst h_StopIteration;
    _HPyConst h_GeneratorExit;
    _HPyConst h_ArithmeticError;
    _HPyConst h_LookupError;
    _HPyConst h_AssertionError;
    _HPyConst h_AttributeError;
    _HPyConst h_BufferError;
    _HPyConst h_EOFError;
    _HPyConst h_FloatingPointError;
    _HPyConst h_OSError;
    _HPyConst h_ImportError;
    _HPyConst h_ModuleNotFoundError;
    _HPyConst h_IndexError;
    _HPyConst h_KeyError;
    _HPyConst h_KeyboardInterrupt;
    _HPyConst h_MemoryError;
    _HPyConst h_NameError;
    _HPyConst h_OverflowError;
    _HPyConst h_RuntimeError;
    _HPyConst h_RecursionError;
    _HPyConst h_NotImplementedError;
    _HPyConst h_SyntaxError;
    _HPyConst h_IndentationError;
    _HPyConst h_TabError;
    _HPyConst h_ReferenceError;
    _HPyConst h_SystemError;
    _HPyConst h_SystemExit;
    _HPyConst h_TypeError;
    _HPyConst h_UnboundLocalError;
    _HPyConst h_UnicodeError;
    _HPyConst h_UnicodeEncodeError;
    _HPyConst h_UnicodeDecodeError;
    _HPyConst h_UnicodeTranslateError;
    _HPyConst h_ValueError;
    _HPyConst h_ZeroDivisionError;
    _HPyConst h_BlockingIOError;
    _HPyConst h_BrokenPipeError;
    _HPyConst h_ChildProcessError;
    _HPyConst h_ConnectionError;
    _HPyConst h_ConnectionAbortedError;
    _HPyConst h_ConnectionRefusedError;
    _HPyConst h_ConnectionResetError;
    _HPyConst h_FileExistsError;
    _HPyConst h_FileNotFoundError;
    _HPyConst h_InterruptedError;
    _HPyConst h_IsADirectoryError;
    _HPyConst h_NotADirectoryError;
    _HPyConst h_PermissionError;
    _HPyConst h_ProcessLookupError;
    _HPyConst h_TimeoutError;
    _HPyConst h_Warning;
    _HPyConst h_UserWarning;
    _HPyConst h_DeprecationWarning;
    _HPyConst h_PendingDeprecationWarning;
    _HPyConst h_SyntaxWarning;
    _HPyConst h_RuntimeWarning;
    _HPyConst h_FutureWarning;
    _HPyConst h_ImportWarning;
    _HPyConst h_UnicodeWarning;
    _HPyConst h_BytesWarning;
    _HPyConst h_ResourceWarning;
    _HPyConst h_BaseObjectType;
    _HPyConst h_TypeType;
    _HPyConst h_BoolType;
    _HPyConst h_LongType;
    _HPyConst h_FloatType;
    _HPyConst h_UnicodeType;
    _HPyConst h_TupleType;
    _HPyConst h_ListType;
    HPy (*ctx_Module_Create)(HPyContext *ctx, HPyModuleDef *def);
    HPy (*ctx_Dup)(HPyContext *ctx, HPy h);
    void (*ctx_Close)(HPyContext *ctx, HPy h);
    HPy (*ctx_Long_FromLong)(HPyContext *ctx, long value);
    HPy (*ctx_Long_FromUnsignedLong)(HPyContext *ctx, unsigned long value);
    HPy (*ctx_Long_FromLongLong)(HPyContext *ctx, long long v);
    HPy (*ctx_Long_FromUnsignedLongLong)(HPyContext *ctx, unsigned long long v);
    HPy (*ctx_Long_FromSize_t)(HPyContext *ctx, size_t value);
    HPy (*ctx_Long_FromSsize_t)(HPyContext *ctx, HPy_ssize_t value);
    long (*ctx_Long_AsLong)(HPyContext *ctx, HPy h);
    unsigned long (*ctx_Long_AsUnsignedLong)(HPyContext *ctx, HPy h);
    unsigned long (*ctx_Long_AsUnsignedLongMask)(HPyContext *ctx, HPy h);
    long long (*ctx_Long_AsLongLong)(HPyContext *ctx, HPy h);
    unsigned long long (*ctx_Long_AsUnsignedLongLong)(HPyContext *ctx, HPy h);
    unsigned long long (*ctx_Long_AsUnsignedLongLongMask)(HPyContext *ctx, HPy h);
    size_t (*ctx_Long_AsSize_t)(HPyContext *ctx, HPy h);
    HPy_ssize_t (*ctx_Long_AsSsize_t)(HPyContext *ctx, HPy h);
    void *(*ctx_Long_AsVoidPtr)(HPyContext *ctx, HPy h);
    double (*ctx_Long_AsDouble)(HPyContext *ctx, HPy h);
    HPy (*ctx_Float_FromDouble)(HPyContext *ctx, double v);
    double (*ctx_Float_AsDouble)(HPyContext *ctx, HPy h);
    HPy (*ctx_Bool_FromLong)(HPyContext *ctx, long v);
    HPy_ssize_t (*ctx_Length)(HPyContext *ctx, HPy h);
    int (*ctx_Number_Check)(HPyContext *ctx, HPy h);
    HPy (*ctx_Add)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Subtract)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Multiply)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_MatrixMultiply)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_FloorDivide)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_TrueDivide)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Remainder)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Divmod)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Power)(HPyContext *ctx, HPy h1, HPy h2, HPy h3);
    HPy (*ctx_Negative)(HPyContext *ctx, HPy h1);
    HPy (*ctx_Positive)(HPyContext *ctx, HPy h1);
    HPy (*ctx_Absolute)(HPyContext *ctx, HPy h1);
    HPy (*ctx_Invert)(HPyContext *ctx, HPy h1);
    HPy (*ctx_Lshift)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Rshift)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_And)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Xor)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Or)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_Index)(HPyContext *ctx, HPy h1);
    HPy (*ctx_Long)(HPyContext *ctx, HPy h1);
    HPy (*ctx_Float)(HPyContext *ctx, HPy h1);
    HPy (*ctx_InPlaceAdd)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceSubtract)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceMultiply)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceMatrixMultiply)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceFloorDivide)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceTrueDivide)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceRemainder)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlacePower)(HPyContext *ctx, HPy h1, HPy h2, HPy h3);
    HPy (*ctx_InPlaceLshift)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceRshift)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceAnd)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceXor)(HPyContext *ctx, HPy h1, HPy h2);
    HPy (*ctx_InPlaceOr)(HPyContext *ctx, HPy h1, HPy h2);
    int (*ctx_Callable_Check)(HPyContext *ctx, HPy h);
    HPy (*ctx_CallTupleDict)(HPyContext *ctx, HPy callable, HPy args, HPy kw);
    void (*ctx_FatalError)(HPyContext *ctx, const char *message);
    void (*ctx_Err_SetString)(HPyContext *ctx, HPy h_type, const char *message);
    void (*ctx_Err_SetObject)(HPyContext *ctx, HPy h_type, HPy h_value);
    HPy (*ctx_Err_SetFromErrnoWithFilename)(HPyContext *ctx, HPy h_type, const char *filename_fsencoded);
    void (*ctx_Err_SetFromErrnoWithFilenameObjects)(HPyContext *ctx, HPy h_type, HPy filename1, HPy filename2);
    int (*ctx_Err_Occurred)(HPyContext *ctx);
    int (*ctx_Err_ExceptionMatches)(HPyContext *ctx, HPy exc);
    void (*ctx_Err_NoMemory)(HPyContext *ctx);
    void (*ctx_Err_Clear)(HPyContext *ctx);
    HPy (*ctx_Err_NewException)(HPyContext *ctx, const char *name, HPy base, HPy dict);
    HPy (*ctx_Err_NewExceptionWithDoc)(HPyContext *ctx, const char *name, const char *doc, HPy base, HPy dict);
    int (*ctx_Err_WarnEx)(HPyContext *ctx, HPy category, const char *message, HPy_ssize_t stack_level);
    void (*ctx_Err_WriteUnraisable)(HPyContext *ctx, HPy obj);
    int (*ctx_IsTrue)(HPyContext *ctx, HPy h);
    HPy (*ctx_Type_FromSpec)(HPyContext *ctx, HPyType_Spec *spec, HPyType_SpecParam *params);
    HPy (*ctx_Type_GenericNew)(HPyContext *ctx, HPy type, _HPyPtr args, HPy_ssize_t nargs, HPy kw);
    HPy (*ctx_GetAttr)(HPyContext *ctx, HPy obj, HPy name);
    HPy (*ctx_GetAttr_s)(HPyContext *ctx, HPy obj, const char *name);
    int (*ctx_HasAttr)(HPyContext *ctx, HPy obj, HPy name);
    int (*ctx_HasAttr_s)(HPyContext *ctx, HPy obj, const char *name);
    int (*ctx_SetAttr)(HPyContext *ctx, HPy obj, HPy name, HPy value);
    int (*ctx_SetAttr_s)(HPyContext *ctx, HPy obj, const char *name, HPy value);
    HPy (*ctx_GetItem)(HPyContext *ctx, HPy obj, HPy key);
    HPy (*ctx_GetItem_i)(HPyContext *ctx, HPy obj, HPy_ssize_t idx);
    HPy (*ctx_GetItem_s)(HPyContext *ctx, HPy obj, const char *key);
    int (*ctx_Contains)(HPyContext *ctx, HPy container, HPy key);
    int (*ctx_SetItem)(HPyContext *ctx, HPy obj, HPy key, HPy value);
    int (*ctx_SetItem_i)(HPyContext *ctx, HPy obj, HPy_ssize_t idx, HPy value);
    int (*ctx_SetItem_s)(HPyContext *ctx, HPy obj, const char *key, HPy value);
    HPy (*ctx_Type)(HPyContext *ctx, HPy obj);
    int (*ctx_TypeCheck)(HPyContext *ctx, HPy obj, HPy type);
    int (*ctx_Is)(HPyContext *ctx, HPy obj, HPy other);
    void *(*ctx_AsStruct)(HPyContext *ctx, HPy h);
    void *(*ctx_AsStructLegacy)(HPyContext *ctx, HPy h);
    HPy (*ctx_New)(HPyContext *ctx, HPy h_type, void **data);
    HPy (*ctx_Repr)(HPyContext *ctx, HPy obj);
    HPy (*ctx_Str)(HPyContext *ctx, HPy obj);
    HPy (*ctx_ASCII)(HPyContext *ctx, HPy obj);
    HPy (*ctx_Bytes)(HPyContext *ctx, HPy obj);
    HPy (*ctx_RichCompare)(HPyContext *ctx, HPy v, HPy w, int op);
    int (*ctx_RichCompareBool)(HPyContext *ctx, HPy v, HPy w, int op);
    HPy_hash_t (*ctx_Hash)(HPyContext *ctx, HPy obj);
    int (*ctx_Bytes_Check)(HPyContext *ctx, HPy h);
    HPy_ssize_t (*ctx_Bytes_Size)(HPyContext *ctx, HPy h);
    HPy_ssize_t (*ctx_Bytes_GET_SIZE)(HPyContext *ctx, HPy h);
    char *(*ctx_Bytes_AsString)(HPyContext *ctx, HPy h);
    char *(*ctx_Bytes_AS_STRING)(HPyContext *ctx, HPy h);
    HPy (*ctx_Bytes_FromString)(HPyContext *ctx, const char *v);
    HPy (*ctx_Bytes_FromStringAndSize)(HPyContext *ctx, const char *v, HPy_ssize_t len);
    HPy (*ctx_Unicode_FromString)(HPyContext *ctx, const char *utf8);
    int (*ctx_Unicode_Check)(HPyContext *ctx, HPy h);
    HPy (*ctx_Unicode_AsASCIIString)(HPyContext *ctx, HPy h);
    HPy (*ctx_Unicode_AsLatin1String)(HPyContext *ctx, HPy h);
    HPy (*ctx_Unicode_AsUTF8String)(HPyContext *ctx, HPy h);
    const char *(*ctx_Unicode_AsUTF8AndSize)(HPyContext *ctx, HPy h, HPy_ssize_t *size);
    HPy (*ctx_Unicode_FromWideChar)(HPyContext *ctx, const wchar_t *w, HPy_ssize_t size);
    HPy (*ctx_Unicode_DecodeFSDefault)(HPyContext *ctx, const char *v);
    HPy (*ctx_Unicode_DecodeFSDefaultAndSize)(HPyContext *ctx, const char *v, HPy_ssize_t size);
    HPy (*ctx_Unicode_EncodeFSDefault)(HPyContext *ctx, HPy h);
    HPy_UCS4 (*ctx_Unicode_ReadChar)(HPyContext *ctx, HPy h, HPy_ssize_t index);
    HPy (*ctx_Unicode_DecodeASCII)(HPyContext *ctx, const char *s, HPy_ssize_t size, const char *errors);
    HPy (*ctx_Unicode_DecodeLatin1)(HPyContext *ctx, const char *s, HPy_ssize_t size, const char *errors);
    int (*ctx_List_Check)(HPyContext *ctx, HPy h);
    HPy (*ctx_List_New)(HPyContext *ctx, HPy_ssize_t len);
    int (*ctx_List_Append)(HPyContext *ctx, HPy h_list, HPy h_item);
    int (*ctx_Dict_Check)(HPyContext *ctx, HPy h);
    HPy (*ctx_Dict_New)(HPyContext *ctx);
    int (*ctx_Tuple_Check)(HPyContext *ctx, HPy h);
    HPy (*ctx_Tuple_FromArray)(HPyContext *ctx, _HPyPtr items, HPy_ssize_t n);
    HPy (*ctx_Import_ImportModule)(HPyContext *ctx, const char *name);
    HPy (*ctx_FromPyObject)(HPyContext *ctx, cpy_PyObject *obj);
    cpy_PyObject *(*ctx_AsPyObject)(HPyContext *ctx, HPy h);
    void (*ctx_CallRealFunctionFromTrampoline)(HPyContext *ctx, HPyFunc_Signature sig, HPyCFunction func, void *args);
    HPyListBuilder (*ctx_ListBuilder_New)(HPyContext *ctx, HPy_ssize_t initial_size);
    void (*ctx_ListBuilder_Set)(HPyContext *ctx, HPyListBuilder builder, HPy_ssize_t index, HPy h_item);
    HPy (*ctx_ListBuilder_Build)(HPyContext *ctx, HPyListBuilder builder);
    void (*ctx_ListBuilder_Cancel)(HPyContext *ctx, HPyListBuilder builder);
    HPyTupleBuilder (*ctx_TupleBuilder_New)(HPyContext *ctx, HPy_ssize_t initial_size);
    void (*ctx_TupleBuilder_Set)(HPyContext *ctx, HPyTupleBuilder builder, HPy_ssize_t index, HPy h_item);
    HPy (*ctx_TupleBuilder_Build)(HPyContext *ctx, HPyTupleBuilder builder);
    void (*ctx_TupleBuilder_Cancel)(HPyContext *ctx, HPyTupleBuilder builder);
    HPyTracker (*ctx_Tracker_New)(HPyContext *ctx, HPy_ssize_t size);
    int (*ctx_Tracker_Add)(HPyContext *ctx, HPyTracker ht, HPy h);
    void (*ctx_Tracker_ForgetAll)(HPyContext *ctx, HPyTracker ht);
    void (*ctx_Tracker_Close)(HPyContext *ctx, HPyTracker ht);
    void (*ctx_Field_Store)(HPyContext *ctx, HPy target_object, _HPyFieldPtr target_field, HPy h);
    HPy (*ctx_Field_Load)(HPyContext *ctx, HPy source_object, HPyField source_field);
    void (*ctx_ReenterPythonExecution)(HPyContext *ctx, HPyThreadState state);
    HPyThreadState (*ctx_LeavePythonExecution)(HPyContext *ctx);
    void (*ctx_Global_Store)(HPyContext *ctx, _HPyGlobalPtr global, HPy h);
    HPy (*ctx_Global_Load)(HPyContext *ctx, HPyGlobal global);
    void (*ctx_Dump)(HPyContext *ctx, HPy h);
    _HPyConst h_ComplexType;
    _HPyConst h_BytesType;
    _HPyConst h_MemoryViewType;
    _HPyConst h_CapsuleType;
    _HPyConst h_SliceType;
    HPy (*ctx_MaybeGetAttr_s)(HPyContext *ctx, HPy obj, const char *name);
    int (*ctx_Slice_Unpack)(HPyContext *ctx, HPy slice, HPy_ssize_t *start, HPy_ssize_t *stop, HPy_ssize_t *step);
    HPy (*ctx_ContextVar_New)(HPyContext *ctx, const char *name, HPy default_value);
    int (*ctx_ContextVar_Get)(HPyContext *ctx, HPy context_var, HPy default_value, _HPyPtr result);
    HPy (*ctx_ContextVar_Set)(HPyContext *ctx, HPy context_var, HPy value);
    HPy (*ctx_Capsule_New)(HPyContext *ctx, void *pointer, const char *name, HPyCapsule_Destructor destructor);
    void *(*ctx_Capsule_Get)(HPyContext *ctx, HPy capsule, _HPyCapsule_key key, const char *name);
    int (*ctx_Capsule_IsValid)(HPyContext *ctx, HPy capsule, const char *name);
    int (*ctx_Capsule_Set)(HPyContext *ctx, HPy capsule, _HPyCapsule_key key, void *value);
    HPy (*ctx_Unicode_FromEncodedObject)(HPyContext *ctx, HPy obj, const char *encoding, const char *errors);
    HPy (*ctx_Unicode_InternFromString)(HPyContext *ctx, const char *str);
    HPy (*ctx_Unicode_Substring)(HPyContext *ctx, HPy obj, HPy_ssize_t start, HPy_ssize_t end);
    HPy (*ctx_Dict_Keys)(HPyContext *ctx, HPy h);
    HPy (*ctx_Dict_GetItem)(HPyContext *ctx, HPy op, HPy key);
    int (*ctx_Sequence_Check)(HPyContext *ctx, HPy h);
    int (*ctx_SetType)(HPyContext *ctx, HPy obj, HPy type);
    int (*ctx_Type_IsSubtype)(HPyContext *ctx, HPy sub, HPy type);
    const char *(*ctx_Type_GetName)(HPyContext *ctx, HPy type);
    HPy (*ctx_SeqIter_New)(HPyContext *ctx, HPy seq);
    int (*ctx_Type_CheckSlot)(HPyContext *ctx, HPy type, HPyDef *value);
    int (*ctx_TypeCheck_g)(HPyContext *ctx, HPy obj, HPyGlobal type);
    int (*ctx_Is_g)(HPyContext *ctx, HPy obj, HPyGlobal other);
};

#ifdef GRAALVM_PYTHON_LLVM
#undef HPy
#undef HPyListBuilder
#undef HPyTupleBuilder
#undef HPyTracker
#undef HPyField
#undef HPyThreadState
#undef HPyGlobal
#undef _HPyCapsule_key
#endif
