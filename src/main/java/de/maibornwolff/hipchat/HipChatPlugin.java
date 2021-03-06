/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.maibornwolff.hipchat;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import de.maibornwolff.hipchat.executors.GetPluginConfigurationExecutor;
import de.maibornwolff.hipchat.executors.GetViewRequestExecutor;
import de.maibornwolff.hipchat.executors.NotificationInterestedInExecutor;
import de.maibornwolff.hipchat.requests.StageStatusRequest;
import de.maibornwolff.hipchat.requests.ValidatePluginSettings;

import java.util.HashSet;

import static de.maibornwolff.hipchat.Constants.PLUGIN_IDENTIFIER;

@Extension
public class HipChatPlugin implements GoPlugin {

    public static final Logger LOG = Logger.getLoggerFor(HipChatPlugin.class);

    private GoApplicationAccessor accessor;
    private PluginRequest pluginRequest;
    private HashSet<String> pipelinesThatFailedBefore;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
        this.pluginRequest = new PluginRequest(accessor);
        this.pipelinesThatFailedBefore = new HashSet<>();
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        try {
            switch (Request.fromString(request.requestName())) {
                case PLUGIN_SETTINGS_GET_VIEW:
                    return new GetViewRequestExecutor().execute();
                case REQUEST_NOTIFICATIONS_INTERESTED_IN:
                    return new NotificationInterestedInExecutor().execute();
                case REQUEST_STAGE_STATUS:
                    return StageStatusRequest.fromJSON(request.requestBody()).executor(pluginRequest, this.pipelinesThatFailedBefore).execute();
                case PLUGIN_SETTINGS_GET_CONFIGURATION:
                    return new GetPluginConfigurationExecutor().execute();
                case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                    return ValidatePluginSettings.fromJSON(request.requestBody()).executor().execute();
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }
}
