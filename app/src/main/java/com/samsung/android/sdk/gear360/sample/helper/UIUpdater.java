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

/**
 * Interface fot Main UI update helper
 */

public interface UIUpdater {
    /**
     * Toggle progress ring (Use for showing progress)
     *
     * @param isShow is progress show
     */
    void toggleProgress(final boolean isShow);

    /**
     * Update GUI state for each situation. (Disconnect/Connect/Streaming)
     *
     * @param state UI state
     */
    void updateUIState(final UIState state);

    /**
     * Update GUI state for each situation.
     *
     * @param state Broadcast state
     */
    void updateBroadcastState(final String state);

    /**
     * UI state
     */
    enum UIState {
        /**
         * Disconnected state
         */
        Disconnect,
        /**
         * Connected state
         */
        Connect,
        /**
         * Previewing state
         */
        Streaming;
    }
}
