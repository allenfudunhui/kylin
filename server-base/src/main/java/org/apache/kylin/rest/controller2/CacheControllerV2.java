/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.rest.controller2;

import java.io.IOException;

import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.metadata.cachesync.Broadcaster;
import org.apache.kylin.rest.controller.BasicController;
import org.apache.kylin.rest.msg.MsgPicker;
import org.apache.kylin.rest.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * CubeController is defined as Restful API entrance for UI.
 *
 * @author jianliu
 */
@Controller
@RequestMapping(value = "/cache")
public class CacheControllerV2 extends BasicController {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(CacheControllerV2.class);

    @Autowired
    @Qualifier("cacheService")
    private CacheService cacheService;

    /**
     * Announce wipe cache to all cluster nodes
     */

    @RequestMapping(value = "/announce/{entity}/{cacheKey}/{event}", method = { RequestMethod.PUT }, produces = {
            "application/vnd.apache.kylin-v2+json" })
    @ResponseBody
    public void announceWipeCacheV2(@RequestHeader("Accept-Language") String lang, @PathVariable String entity,
            @PathVariable String event, @PathVariable String cacheKey) throws IOException {
        MsgPicker.setMsg(lang);

        cacheService.annouceWipeCache(entity, event, cacheKey);
    }

    /**
     * Wipe cache on this node
     */

    @RequestMapping(value = "/{entity}/{cacheKey}/{event}", method = { RequestMethod.PUT }, produces = {
            "application/vnd.apache.kylin-v2+json" })
    @ResponseBody
    public void wipeCacheV2(@RequestHeader("Accept-Language") String lang, @PathVariable String entity,
            @PathVariable String event, @PathVariable String cacheKey) throws IOException {
        MsgPicker.setMsg(lang);

        cacheService.notifyMetadataChange(entity, Broadcaster.Event.getEvent(event), cacheKey);
    }

    @RequestMapping(value = "/announce/config", method = { RequestMethod.POST }, produces = {
            "application/vnd.apache.kylin-v2+json" })
    @ResponseBody
    public void hotLoadKylinConfigV2(@RequestHeader("Accept-Language") String lang) throws IOException {
        MsgPicker.setMsg(lang);

        KylinConfig.getInstanceFromEnv().hotLoadKylinProperties();
        cacheService.notifyMetadataChange(Broadcaster.SYNC_ALL, Broadcaster.Event.UPDATE, Broadcaster.SYNC_ALL);
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
}
