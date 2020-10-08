/*
   Copyright (c) 2013, The Linux Foundation. All rights reserved.
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.
   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
   ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
   BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
   OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
   IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <stdlib.h>
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>

#include <android-base/properties.h>
#include <android-base/logging.h>

#include "property_service.h"
#include "vendor_init.h"

using android::base::GetProperty;
using android::base::SetProperty;

void property_override(char const prop[], char const value[]) {
    prop_info *pi;

    pi = (prop_info*) __system_property_find(prop);
    if (pi)
        __system_property_update(pi, value, strlen(value));
    else
        __system_property_add(prop, strlen(prop), value, strlen(value));
}

void property_override_dual(char const system_prop[], char const vendor_prop[],
    char const value[])
{
    property_override(system_prop, value);
    property_override(vendor_prop, value);
}

void property_override_fingerprints(char const value[])
{
    property_override("ro.build.fingerprint", value);
    property_override("ro.vendor.build.fingerprint", value);
    property_override("ro.bootimage.build.fingerprint", value);
    property_override("ro.odm.build.fingerprint", value);
    property_override("ro.system.build.fingerprint", value);
}

void cdma_properties(char const default_cdma_sub[], char const default_network[]) {
    SetProperty("ril.subscription.types", "NV,RUIM");
    SetProperty("ro.ril.enable.sdr", "0");
    SetProperty("ro.telephony.default_cdma_sub", default_cdma_sub);
    SetProperty("ro.telephony.default_network", default_network);
    SetProperty("telephony.lteOnCdmaDevice", "1");
    SetProperty("persist.radio.sib16_support", "1");
    SetProperty("ro.cdma.data_retry_config", "max_retries=infinite,0,0,60000,120000,480000,900000");
    SetProperty("ro.config.svlte1x", "true");
    SetProperty("ro.gsm.2nd_data_retry_config", "max_retries=infinite,0,0,60000,120000,480000,900000");
    SetProperty("ro.gsm.data_retry_config", "max_retries=infinite,0,0,60000,120000,480000,900000");
    SetProperty("ro.ril.air.enabled", "0");
    SetProperty("ro.ril.att.feature", "0");
    SetProperty("ro.ril.def.agps.mode", "1");
    SetProperty("ro.ril.disable.fd.plmn.prefix", "23402,23410,23411,23420,23594,27202,27205");
    SetProperty("ro.ril.enable.a52", "0");
    SetProperty("ro.ril.enable.dtm", "0");
    SetProperty("ro.ril.enable.gea3", "1");
    SetProperty("ro.ril.enable.sdr", "0");
    SetProperty("ro.ril.gprsclass", "12");
    SetProperty("ro.ril.gsm.to.lte.blind.redir", "0");
    SetProperty("ro.ril.oem.ecclist", "911,*911,#911");
    SetProperty("ro.ril.oem.show.act", "0");
    SetProperty("ro.ril.roaming_lte.plmn", "302220,302610,45400,45402,45410,45418");
    SetProperty("ro.ril.set.mtusize", "1428");
    SetProperty("ro.ril.vzw.feature", "1");
    SetProperty("ro.ril.wp.feature", "1");
    SetProperty("ro.telephony.get_imsi_from_sim", "true");
    SetProperty("ro.cdma.home.operator.numeric", "311480");
    SetProperty("ro.cdma.home.operator.alpha", "Verizon");
    SetProperty("gsm.sim.operator.alpha", "Verizon");
    SetProperty("gsm.sim.operator.numeric", "311480");
    SetProperty("gsm.operator.alpha", "Verizon");
    SetProperty("gsm.operator.numeric", "311480");
}

void cdma_sprint_properties() {
    SetProperty("ro.cdma.home.operator.numeric", "310120");
    SetProperty("ro.cdma.home.operator.alpha", "Sprint");
    SetProperty("gsm.sim.operator.alpha", "Sprint");
    SetProperty("gsm.sim.operator.numeric", "310120");
    SetProperty("gsm.operator.alpha", "Sprint");
    SetProperty("gsm.operator.numeric", "310120");
}

void gsm_properties(char const default_network[]) {
    SetProperty("ro.telephony.default_network", default_network);
    SetProperty("telephony.lteOnGsmDevice", "1");
    SetProperty("persist.radio.process_sups_ind", "1");
    SetProperty("ro.ril.disable.cpc", "1");
    SetProperty("ro.ril.enable.amr.wideband", "0");
    SetProperty("ro.ril.enable.dtm", "1");
    SetProperty("ro.ril.enable.pre_r8fd", "1");
    SetProperty("ro.ril.enable.sdr", "1");
    SetProperty("ro.ril.fd.pre_r8_tout.scr_off", "2");
    SetProperty("ro.ril.fd.pre_r8_tout.scr_on", "3");
    SetProperty("ro.ril.fd.r8_tout.scr_off", "2");
    SetProperty("ro.ril.fd.r8_tout.scr_on", "3");
    SetProperty("ro.ril.ltefgi.rel9", "2147483648");
    SetProperty("ro.ril.ltefgi", "1561328784");
    SetProperty("ro.ril.oem.ecclist", "911");
    SetProperty("ro.ril.oem.nosim.ecclist", "911");
}

void vendor_load_properties() {
    std::string platform;
    std::string bootmid;
    std::string device;

    bootmid = GetProperty("ro.boot.mid", "");
    if (bootmid == "0PJA10000") {
        /* himauhl */
        gsm_properties("9");
        property_override("ro.build.description", "4.28.401.3 CL869604 release-keys");
        property_override_fingerprints("htc/himauhl_htc_europe/htc_himauhl:7.0/NRD90M/869604.3:user/release-keys");
        property_override("ro.build.changelist", "869604");
        property_override("ro.build.product", "htc_himauhl");
        property_override("ro.build.sense.version", "8.0");
        property_override_dual("ro.product.device", "ro.vendor.product.device", "htc_himauhl");
        property_override_dual("ro.product.model", "ro.vendor.product.model", "HTC One M9");
        property_override_dual("ro.product.name", "ro.vendor.product.name", "himauhl_htc_europe");
    } else if (bootmid == "0PJA11000") {
        /* himaul */
        gsm_properties("9");
        property_override("ro.build.description", "4.30.617.12 CL894012 release-keys");
        property_override_fingerprints("htc/himaulatt_na_gen_unlock/htc_himaulatt:7.0/NRD90M/894012.12:user/release-keys");
        property_override("ro.build.changelist", "894012");
        property_override("ro.build.product", "htc_himaulatt");
        property_override("ro.build.sense.version", "8.0");
        property_override_dual("ro.product.device", "ro.vendor.product.device", "htc_himaulatt");
        property_override_dual("ro.product.model", "ro.vendor.product.model", "HTC One M9");
        property_override_dual("ro.product.name", "ro.vendor.product.name", "himaulatt_na_gen_unlock");
    } else if (bootmid == "0PJA12000") {
        /* himaul */
        gsm_properties("9");
        property_override("ro.build.description", "4.27.531.6 CL869604 release-keys");
        property_override_fingerprints("htc/himaul_tmous/htc_himaul:7.0/NRD90M/869604.6:user/release-keys");
        property_override("ro.build.changelist", "869604");
        property_override("ro.build.product", "htc_himaul");
        property_override("ro.build.sense.version", "8.0");
        property_override_dual("ro.product.device", "ro.vendor.product.device", "htc_himaul");
        property_override_dual("ro.product.model", "ro.vendor.product.model", "HTC One M9");
        property_override_dual("ro.product.name", "ro.vendor.product.name", "himaul_tmous");
    } else if (bootmid == "0PJA20000") {
        /* himawhl */
        cdma_properties("0", "10");
        cdma_sprint_properties();
        property_override("ro.build.description", "4.27.651.4 CL870580 release-keys");
        property_override_fingerprints("htc/himawhl_sprint_wwe/htc_himawhl:7.0/NRD90M/870580.4:user/release-keys");
        property_override("ro.build.changelist", "870580");
        property_override("ro.build.product", "htc_himawhl");
        property_override("ro.build.sense.version", "8.0");
        property_override_dual("ro.product.device", "ro.vendor.product.device", "htc_himawhl");
        property_override_dual("ro.product.model", "ro.vendor.product.model", "0PJA2");
        property_override_dual("ro.product.name", "ro.vendor.product.name", "himawhl_sprint_wwe");
    } else if (bootmid == "0PJA30000") {
        /* himawl */
        cdma_properties("0", "10");
        property_override("ro.build.description", "4.49.605.11 CL947620 release-keys");
        property_override_fingerprints("htc/HTCOneM9vzw/htc_himawl:7.0/NRD90M/947620.11:user/release-keys");
        property_override("ro.build.changelist", "947620");
        property_override("ro.build.product", "htc_himawl");
        property_override("ro.build.sense.version", "8.0");
        property_override_dual("ro.product.device", "ro.vendor.product.device", "htc_himawl");
        property_override_dual("ro.product.model", "ro.vendor.product.model", "HTC6535LVW");
        property_override_dual("ro.product.name", "ro.vendor.product.name", "HTCOneM9vzw");
        property_override("ro.com.google.clientidbase", "android-verizon");
    }

    device = GetProperty("ro.product.device", "");
    LOG(INFO) << "Found bootmid '" << bootmid.c_str() <<
        "' setting build properties for '" << device.c_str() << "' device\n";
}
