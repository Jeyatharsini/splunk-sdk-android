/*
 * Copyright 2012 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk.sdk;
/**
 * The {@code FiredAlertGroup} class represents a group of fired alerts, which 
 * are the alerts for a given saved search.
 */
public class FiredAlertGroup extends Entity {

    /**
     * Class constructor.
     *
     * @param service The connected {@code Service} instance.
     * @param path The fired alert endpoint.
     */
    FiredAlertGroup(Service service, String path) {
        super(service, path);
    }

    /**
     * Returns a group of fired alerts for a given saved search.
     *
     * @return The fired alerts in the group.
     */
    public EntityCollection<FiredAlert> getAlerts() {
        return new EntityCollection<FiredAlert>(
            service, this.path, FiredAlert.class);
    }
}
