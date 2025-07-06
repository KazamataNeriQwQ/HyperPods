#include "hook_base.h"
#include <string>
#include <android/log.h>
#include <jni.h>

static HookFunType hook_func = nullptr;

static bool isHookSuccess = false;

uintptr_t getModuleBase(const char *module_name) {
    FILE *fp;
    unsigned long addr = 0;
    char *pch;
    char filename[64];
    char line[1024];
    snprintf(filename, sizeof(filename), "/proc/self/maps");
    fp = fopen(filename, "r");
    if (fp != nullptr) {
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, module_name)) {
                pch = strtok(line, "-");
                addr = strtoul(pch, nullptr, 16);
                if (addr == 0x8000)
                    addr = 0;
                break;
            }
        }
        fclose(fp);
    }
    return addr;
}

uintptr_t findStr(uintptr_t moduleBase, const char *str) {
    size_t len = strlen(str) + 1;
    uintptr_t max_addr = moduleBase + 0xc50000;
    uintptr_t pos = moduleBase;
    for (;;) {
        auto &buf = *reinterpret_cast<char (*)[1024]>(pos);
        int cur = 0;
        for (int i = 0; i < 1024; i++) {
            auto cur_data = buf[i];
            auto str_data = str[cur];
            if (cur_data == str_data) {
                cur++;
                if (cur == len) {
                    return pos + i - len + 1;
                }
            } else {
                cur = 0;
            }
        }
        pos += 1024;
        if (pos > max_addr) {
            return -1;
        }
    }
    return -1;
}

uint8_t (*l2c_fcr_chk_chan_modes_backup)(void* p_ccb);

uint8_t l2c_fcr_chk_chan_modes_hook(void* p_ccb) {
    if (p_ccb == nullptr) {
        abort();
    }
    return 1;
}

uintptr_t findFunction(uintptr_t moduleBase, uintptr_t str1Addr, uintptr_t str2Addr, bool isMtk) {
    uintptr_t pos = moduleBase;
    uintptr_t max_addr = moduleBase + 0xc50000;
    uintptr_t need_offset_hex = (((str1Addr - moduleBase) & 0xfff) * 4) + 1;
    uintptr_t need_offset_str2_hex = ((str2Addr - moduleBase) & 0xfff) * 4;
    uintptr_t str_offset_to_func = isMtk ? 0x84 : 0x44;
    for (;;) {
        auto &buf = *reinterpret_cast<char (*)[1024]>(pos);

        for (int i = 0; i < 4096; i += 4) {
            auto addr = buf + i;
//            auto cur_data = *(uintptr_t * )(moduleBase + 0xb48838);
            auto cur_data = *(uintptr_t * )(addr);
            uintptr_t first = (cur_data & 0xff000000) >> 24;
//            __android_log_print(ANDROID_LOG_INFO, "Art_Chen-Hook", "str1Addr %x cur_data %x first %x offset_hex %x need_offset_hex %x",str1Addr, cur_data, first, (cur_data & 0xffff00) >> 8, need_offset_hex);
            if (first != 0x91) [[likely]] {
                // not 'add, <reg>, <reg>'
                continue;
            }

            uintptr_t offset_hex = (cur_data & 0xffff00) >> 8;
            if (offset_hex != need_offset_hex && offset_hex != need_offset_hex - 1) [[likely]] {
                // add offset is not correct
                continue;
            }
            uintptr_t reg = cur_data & 0xff;
            if (reg != 0x8) {
                // reg not matched
                continue;
            }

            // Check assert log matched
            auto assert_log_data = *(uintptr_t * )(addr + 0x10);
            if ((assert_log_data & 0xff000000) >> 24 != 0x91) [[likely]] {
                // not 'add, <reg>, <reg>'
                continue;
            }

            uintptr_t str2_offset_hex = (assert_log_data & 0xffff00) >> 8;
            if (str2_offset_hex != need_offset_str2_hex && str2_offset_hex != need_offset_str2_hex + 1) [[likely]] {
                // add offset is not correct
                continue;
            }

            if ((assert_log_data & 0xff) != 0x63) {
                // reg not matched
                continue;
            }

            return pos + i - str_offset_to_func;
        }
        if (pos > max_addr) {
            return -1;
        }
        pos += 1024;
    }
}

void on_library_loaded(const char *name, void *handle) {
    if (std::string(name).ends_with("libbluetooth_jni.so")) {
        auto base_addr = getModuleBase("libbluetooth_jni.so");
        auto mtk_str_addr = findStr(base_addr, "vendor/mediatek/proprietary/packages/modules");
        auto str_addr = findStr(base_addr, "l2c_fcr_chk_chan_modes");
        // TODO: check prop may be better.
        bool isMtk = mtk_str_addr != -1;
        auto str2_addr = findStr(base_addr,
                                 isMtk ? "L2CAP - Peer does not support our desired channel types"
                                            : "assert failed: p_ccb != NULL");
        auto func_log_addr = findFunction(base_addr, str_addr, str2_addr, isMtk);
        if (func_log_addr == -1) {
            isHookSuccess = false;
            return;
        }
        void *target = reinterpret_cast<void *>(func_log_addr);
        __android_log_print(ANDROID_LOG_DEBUG, "Art_Chen-Hook", "found func base_addr %x, func_addr %x", base_addr, func_log_addr);
        auto res = hook_func(target, (void *) l2c_fcr_chk_chan_modes_hook, (void **) &l2c_fcr_chk_chan_modes_backup);
        __android_log_print(ANDROID_LOG_DEBUG, "Art_Chen-Hook", "hook res %d", res);
        isHookSuccess = res == 0;
    }
}

extern "C" [[gnu::visibility("default")]] [[gnu::used]]
NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    hook_func = entries->hook_func;
    return on_library_loaded;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_moe_chenxy_hyperpods_hook_HeadsetStateDispatcher_nativeGetHookResult(JNIEnv *env, jobject thiz) {
    return isHookSuccess;
}