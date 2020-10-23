/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __unused
#define __unused  __attribute__((__unused__))
#endif

//#define LOG_NDEBUG 0

#include <log/log.h>
#include <cutils/properties.h>

signed int __htclog_read_masks(char *buf __unused, signed int len __unused)
{
    return 0;
}

int __htclog_init_mask(const char *a1 __unused, unsigned int a2 __unused, int a3 __unused)
{
#if !(LOG_NDEBUG)
    return property_get_int32("debug.htc.logmask", 0);
#else
    return 0;
#endif
}

int __htclog_print_private(int a1 __unused, const char *a2 __unused, const char *fmt __unused, ...)
{
    return 0;
}
