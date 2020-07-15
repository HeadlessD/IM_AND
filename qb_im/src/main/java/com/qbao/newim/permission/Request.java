/*
 * Copyright © Yan Zhenjie
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
package com.qbao.newim.permission;

import android.support.annotation.NonNull;

/**
 * <p>Permission request.</p>
 */
public interface Request {

    /**
     * Here to fill in all of this to apply for permission, can be a, can be more.
     *
     * @param permissions one or more permissions.
     * @return {@link Request}.
     */
    @NonNull
    Request permission(String... permissions);

    /**
     * Here to fill in all of this to apply for permission, can be a, can be more.
     *
     * @param permissionsArray one or more permissions.
     * @return {@link Request}.
     */
    @NonNull
    Request permission(String[]... permissionsArray);

    /**
     * With user privilege refused many times, the Listener will be called back, you can prompt the user
     * permissions role in this method.
     *
     * @param listener {@link RationaleListener}.
     * @return Request.
     */
    @NonNull
    Request rationale(RationaleListener listener);

    /**
     * Request code.
     *
     * @param requestCode int, the first parameter in callback {@code onRequestPermissionsResult(int, String[],
     *                    int[])}}.
     * @return {@link Request}.
     */
    @NonNull
    Request requestCode(int requestCode);

    /**
     * Set the callback object.
     *
     * @return {@link Request}.
     */
    Request callback(PermissionListener callback);

    /**
     * set the fail tip
     * @param txt
     * @return
     */
    Request failTips(String txt);

    /**
     * Request permission.
     */
    void start();

}
