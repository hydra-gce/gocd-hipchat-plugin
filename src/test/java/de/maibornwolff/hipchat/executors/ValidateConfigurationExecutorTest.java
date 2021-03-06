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

package de.maibornwolff.hipchat.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import de.maibornwolff.hipchat.requests.ValidatePluginSettings;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ValidateConfigurationExecutorTest {
    @Test
    public void shouldRejectBadJSONPipelineConfigsAndBlankServerURLs() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("pipelineConfig", "this is not json");

        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\n" +
            "  {\n" +
            "    \"message\": \"GoCD Server URL must not be blank.\",\n" +
            "    \"key\": \"gocd_server_url\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"message\": \"HipChat Server URL must not be blank.\",\n" +
            "    \"key\": \"hipchat_server_url\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"message\": \"No valid JSON received for PipelineConfigField pipelineConfig\",\n" +
            "    \"key\": \"pipelineConfig\"\n" +
            "  }\n" +
            "]", response.responseBody(), true);
    }

    @Test
    public void shouldRejectInvalidServerUrls() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("hipchat_server_url", "this is not a valid url");
        settings.put("gocd_server_url", "this is not a valid url");

        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\n" +
            "  {\n" +
            "    \"message\": \"GoCD Server URL is not a valid URL.\",\n" +
            "    \"key\": \"gocd_server_url\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"message\": \"HipChat Server URL is not a valid URL.\",\n" +
            "    \"key\": \"hipchat_server_url\"\n" +
            "  }\n" +
            "]", response.responseBody(), true);
    }

    @Test
    public void shouldValidateAGoodConfiguration() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("hipchat_server_url", "https://hipchat.example.com");
        settings.put("gocd_server_url", "https://gocd.example.com");
        settings.put("pipelineConfig", "[]");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }
}
