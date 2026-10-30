#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_nativeengine_NativeBridge_getEngineVersion(
        JNIEnv* env,
        jobject /* this */) {

    std::string version = "MindVault Native Engine 1.0 (C++17)";
    return env->NewStringUTF(version.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_nativeengine_NativeBridge_calculateTextLength(
        JNIEnv* env,
        jobject /* this */,
        jstring text) {

    if (text == nullptr) {
        return 0;
    }

    const char* chars = env->GetStringUTFChars(text, nullptr);
    if (chars == nullptr) {
        return 0;
    }

    int length = 0;
    for (const char* p = chars; *p; ++p) {
        ++length;
    }

    env->ReleaseStringUTFChars(text, chars);
    return length;
}
