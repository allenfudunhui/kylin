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

package org.apache.kylin.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HiveCmdBuilderTest {

    @Before
    public void setup() {
        System.setProperty("log4j.configuration", "file:../build/conf/kylin-tools-log4j.properties");
        System.setProperty("KYLIN_CONF", LocalFileMetadataTestCase.LOCALMETA_TEST_DATA);
    }

    @After
    public void after() throws Exception {
        System.clearProperty("kylin.source.hive.client");
        System.clearProperty("kylin.source.hive.beeline-params");
    }

    @Test
    public void testHiveCLI() {
        System.setProperty("kylin.source.hive.client", "cli");

        Map<String, String> hiveProps = new HashMap<>();
        hiveProps.put("hive.execution.engine", "mr");
        Map<String, String> hivePropsOverwrite = new HashMap<>();
        hivePropsOverwrite.put("hive.execution.engine", "tez");
        HiveCmdBuilder hiveCmdBuilder = new HiveCmdBuilder();
        hiveCmdBuilder.addStatement("USE default;");
        hiveCmdBuilder.addStatement("DROP TABLE test;");
        hiveCmdBuilder.addStatement("SHOW\n TABLES;");
        hiveCmdBuilder.setHiveConfProps(hiveProps);
        hiveCmdBuilder.overwriteHiveProps(hivePropsOverwrite);
        assertEquals(
                "hive -e \"USE default;\nDROP TABLE test;\nSHOW\n TABLES;\n\" --hiveconf hive.execution.engine=tez",
                hiveCmdBuilder.build());
    }

    @Test
    public void testBeeline() throws IOException {
        String lineSeparator = java.security.AccessController
                .doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));
        System.setProperty("kylin.source.hive.client", "beeline");
        System.setProperty("kylin.source.hive.beeline-params", "-u jdbc_url");

        HiveCmdBuilder hiveCmdBuilder = new HiveCmdBuilder();
        hiveCmdBuilder.addStatement("USE default;");
        hiveCmdBuilder.addStatement("DROP TABLE test;");
        hiveCmdBuilder.addStatement("SHOW\n TABLES;");

        String cmd = hiveCmdBuilder.build();
        assertTrue(cmd.startsWith("beeline -u jdbc_url"));

        String hqlFile = cmd.substring(cmd.lastIndexOf("-f ") + 3).trim();
        hqlFile = hqlFile.substring(0, hqlFile.length() - ";exit $ret_code".length());

        String hqlStatement = FileUtils.readFileToString(new File(hqlFile), Charset.defaultCharset());
        assertEquals(
                "USE default;" + lineSeparator + "DROP TABLE test;" + lineSeparator + "SHOW\n TABLES;" + lineSeparator,
                hqlStatement);

        FileUtils.forceDelete(new File(hqlFile));
    }
}
