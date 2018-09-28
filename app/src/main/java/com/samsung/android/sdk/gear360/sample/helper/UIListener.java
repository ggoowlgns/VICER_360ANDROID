/*
 * Copyright (c) 2017 Samsung Electronics. All Rights Reserved
 *
 * PROPRIETARY/CONFIDENTIAL
 *
 * This software is the confidential and proprietary information of
 * SAMSUNG ELECTRONICS ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with SAMSUNG ELECTRONICS. SAMSUNG make no
 * representations or warranties about the suitability
 * of the software, either express or implied, including but not
 * limited to the implied warranties of merchantability, fitness for a
 * particular purpose, or non-infringement. SAMSUNG shall not be liable
 * for any damages suffered by licensee as a result of using, modifying
 * or distributing this software or its derivatives.
 */
package com.samsung.android.sdk.gear360.sample.helper;

import com.samsung.android.sdk.gear360.SGear360;
import com.samsung.android.sdk.gear360.device.Device;

/**
 * Interface for updating UI
 */

public interface UIListener {
    /**
     * Listener for initializing
     */
    interface InitializeListener{
        /**
         * Call in Success case
         * @param sdk initialized SDK instance
         */
        void onInitialized(SGear360 sdk);

        /**
         * Call in Fail case
         */
        void onFailed();
    }

    /**
     * Listener for connecting device
     */
    interface ConnectListener{
        /**
         * Call in Success case
         * @param connectedDevice connected device
         */
        void onSuccess(Device connectedDevice);
        /**
         * Call in Fail case
         */
        void onFailed();
    }
}
