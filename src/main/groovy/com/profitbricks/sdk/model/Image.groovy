/*
   Copyright 2018 Profitbricks GmbH

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.profitbricks.sdk.model

import com.profitbricks.sdk.annotation.Updatable
import groovy.transform.*

/**
 * an image POGO
 * @see <a href="https://devops.profitbricks.com/api/cloud/v4/#images">Cloud API reference</a>
 *
 * @author fudge <frank.geusch@profitbricks.com>
 */
@ToString(includeNames = true, ignoreNulls = true, includeSuperProperties = true, includePackage = false, excludes = ['resource'])
@EqualsAndHashCode(callSuper = true)
final class Image extends ModelBase {
    @Updatable
    String name, description, location, licenceType, imageType
    @Updatable
    int size
    @Updatable
    boolean cpuHotPlug, cpuHotUnplug, ramHotPlug, ramHotUnplug, nicHotPlug, nicHotUnplug, discVirtioHotPlug, discVirtioHotUnplug, discScsiHotPlug, discScsiHotUnplug, _public

    /**
     * images cannot be created via the API, this just throws
     * @see NoSuchMethodException
     */
    @Override
    final Image create() {
        throw new NoSuchMethodException('create not implemented for Images')
    }

    final String resource = 'images'
}
