/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RuntimeTest extends junit.framework.TestCase {

    Runtime r = Runtime.getRuntime();

    InputStream is;

    String s;

    static boolean flag = false;

    static boolean ranFinalize = false;

    class HasFinalizer {
        String internalString;

        HasFinalizer(String s) {
            internalString = s;
        }

        @Override
        protected void finalize() {
            internalString = "hit";
        }
    }

    @Override
    protected void finalize() {
        if (flag)
            ranFinalize = true;
    }

    protected RuntimeTest createInstance() {
        return new RuntimeTest("FT");
    }

    /**
     * java.lang.Runtime#exit(int)
     */
    public void test_exitI() {
        // Test for method void java.lang.Runtime.exit(int)
        assertTrue("Can't really test this", true);
    }

    /**
     * java.lang.Runtime#exec(java.lang.String)
     */
    public void test_exec() {
        boolean success = false;

        /* successful exec's are tested by java.lang.Process */
        try {
            Runtime.getRuntime().exec("AnInexistentProgram");
        } catch (IOException e) {
            success = true;
        }
        assertTrue(
                "failed to throw IOException when exec'ed inexistent program",
                success);
    }

    /**
     * java.lang.Runtime#gc()
     */
    public void test_gc() {
        // Test for method void java.lang.Runtime.gc()
        try {
            r.gc(); // ensure all garbage objects have been collected
            r.gc(); // two GCs force collection phase to complete
            long firstRead = r.totalMemory() - r.freeMemory();
            Vector<StringBuffer> v = new Vector<StringBuffer>();
            for (int i = 1; i < 10; i++)
                v.addElement(new StringBuffer(10000));
            long secondRead = r.totalMemory() - r.freeMemory();
            v = null;
            r.gc();
            r.gc();
            assertTrue("object memory did not grow", secondRead > firstRead);
            assertTrue("space was not reclaimed", (r.totalMemory() - r
                    .freeMemory()) < secondRead);
        } catch (OutOfMemoryError oome) {
            System.out.println("Out of memory during freeMemory test");
            r.gc();
            r.gc();
        }
    }

    /**
     * java.lang.Runtime#getRuntime()
     */
    public void test_getRuntime() {
        // Test for method java.lang.Runtime java.lang.Runtime.getRuntime()
        assertTrue("Used to test", true);
    }

    /**
     * java.lang.Runtime#runFinalization()
     */
    public void test_runFinalization() {
        // Test for method void java.lang.Runtime.runFinalization()

        flag = true;
        createInstance();
        int count = 10;
        // the gc below likely bogosifies the test, but will have to do for
        // the moment
        while (!ranFinalize && count-- > 0) {
            r.gc();
            r.runFinalization();
        }
        assertTrue("Failed to run finalization", ranFinalize);
    }

    /**
     * java.lang.Runtime#freeMemory() / java.lang.Runtime#totalMemory() /
     * java.lang.Runtime#maxMemory()
     */
    public void test_memory() {
        assertTrue("freeMemory <= 0", r.freeMemory() > 0);
        assertTrue("totalMemory() < freeMemory()", r.totalMemory() >= r.freeMemory());
        assertTrue("maxMemory() < totalMemory()", r.maxMemory() >= r.totalMemory());
    }

    public void test_freeMemory() {
        // Heap might grow or do GC at any time, so we can't really test a lot. Hence we are just
        // doing some basic sanity checks here.
        long freeBefore = r.freeMemory();
        List<byte[]> arrays = new ArrayList<byte[]>();
        for (int i = 1; i < 10; i++) {
            arrays.add(new byte[10000]);
        }
        long freeAfter =  r.freeMemory();

        // If totalMemory() has grown/shrunk freeMemory() might have gone down or up, but the
        // freeMemory is unlikely to stay the same.
        assertTrue("free memory must change with allocations", freeAfter != freeBefore);
    }

    public RuntimeTest() {
    }

    public RuntimeTest(String name) {
        super(name);
    }
}
