// SPDX-License-Identifier: GPL-3.0-only
#include <jni.h>
#include <cstdint>

// Compile-only JNI type boundary. No library is loaded or packaged by the M1 app.
// Execution, graphics and presentation entry points do not exist in this milestone.
static_assert(sizeof(jlong) == sizeof(std::int64_t));
static_assert(sizeof(jint) == sizeof(std::int32_t));
namespace studiodroid {
constexpr jint kBridgeContractVersion = 1;
static_assert(kBridgeContractVersion == 1);
}
