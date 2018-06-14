/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package runtime.valhalla.valuetypes;

import java.lang.invoke.*;

import jdk.experimental.value.MethodHandleBuilder;

import jdk.test.lib.Asserts;

/*
 * @test
 * @summary Flattenable field semantic test
 * @modules java.base/jdk.experimental.bytecode
 *          java.base/jdk.experimental.value
 * @library /test/lib
 * @compile -XDenableValueTypes Point.java JumboValue.java
 * @run main/othervm -Xint -XX:ValueFieldMaxFlatSize=64 -XX:+EnableValhalla runtime.valhalla.valuetypes.FlattenableSemanticTest
 * @run main/othervm -Xcomp -XX:+EnableValhalla -XX:ValueFieldMaxFlatSize=64 runtime.valhalla.valuetypes.FlattenableSemanticTest
 * // debug: -XX:+PrintValueLayout -XX:-ShowMessageBoxOnError
 */
public class FlattenableSemanticTest {

    __NotFlattened static Point nfsp;
    __Flattenable  static Point fsp;

    __NotFlattened Point nfip;
    __Flattenable Point fip;

    static __NotFlattened JumboValue nfsj;
    static __Flattenable JumboValue fsj;

    __NotFlattened JumboValue nfij;
    __Flattenable JumboValue fij;

    static Object getNull() {
        return null;
    }

    FlattenableSemanticTest() { }

    public static void main(String[] args) {
        FlattenableSemanticTest test = new FlattenableSemanticTest();

        // Uninitialized value fields must be null for non flattenable fields
        Asserts.assertNull(nfsp, "Invalid non null value for unitialized non flattenable field");
        Asserts.assertNull(nfsj, "Invalid non null value for unitialized non flattenable field");
        Asserts.assertNull(test.nfip, "Invalid non null value for unitialized non flattenable field");
        Asserts.assertNull(test.nfij, "Invalid non null value for unitialized non flattenable field");

        fsp.equals(null);

        // Uninitialized value fields must be non null for flattenable fields
        Asserts.assertNotNull(fsp, "Invalid null value for unitialized flattenable field");
        Asserts.assertNotNull(fsj, "Invalid null value for unitialized flattenable field");
        Asserts.assertNotNull(test.fip, "Invalid null value for unitialized flattenable field");
        Asserts.assertNotNull(test.fij, "Invalid null value for unitialized flattenable field");

        // Assigning null must be allowed for non flattenable value fields
        /**
         * Javac currently aggressively guarding new code with null checks, so code like:
         *
         * nfsp = (Point)getNull();
         *
         * Will throw NPE, with javac compiled, so test with own generated bytecode instead...
         */
        boolean exception = fieldSetWithNullThrowsNpe("nfsp", Point.class, null); // nfsp = null;
        Asserts.assertFalse(exception, "Invalid NPE when assigning null to a non flattenable field");

        exception = fieldSetWithNullThrowsNpe("nfsj", JumboValue.class, null); // nfsj = null;
        Asserts.assertFalse(exception, "Invalid NPE when assigning null to a non flattenable field");

        exception = fieldSetWithNullThrowsNpe("nfip", Point.class, test); // test.nfip = null;
        Asserts.assertFalse(exception, "Invalid NPE when assigning null to a non flattenable field");

        exception = fieldSetWithNullThrowsNpe("nfij", JumboValue.class, test); // test.nfij = null;
        Asserts.assertFalse(exception, "Invalid NPE when assigning null to a non flattenable field");

        // Assigning null to a flattenable value field must trigger a NPE
        try {
            fsp = (Point)getNull();
        } catch(NullPointerException e) {
            exception = true;
        }
        Asserts.assertTrue(exception, "NPE not thrown when assigning null to a flattenable field");
        exception = false;
        try {
            fsj = (JumboValue)getNull();
        } catch(NullPointerException e) {
            exception = true;
        }
        Asserts.assertTrue(exception, "NPE not thrown when assigning null to a flattenable field");
        exception = false;
        try {
            test.fip = (Point)getNull();
        } catch(NullPointerException e) {
            exception = true;
        }
        Asserts.assertTrue(exception, "NPE not thrown when assigning null to a flattenable field");
        exception = false;
        try {
            test.fij = (JumboValue)getNull();
        } catch(NullPointerException e) {
            exception = true;
        }
        Asserts.assertTrue(exception, "NPE not thrown when assigning null to a flattenable field");
        exception = false;
    }

    static boolean fieldSetWithNullThrowsNpe(String name, Class<?> argType, FlattenableSemanticTest test) {
        return checkSetterThrowsNpe(generateSetter(name, argType, test == null), test, null);
    }

    static MethodHandle generateSetter(String name, Class<?> argType, boolean isStaticField) {
        MethodHandles.Lookup mhLookup = MethodHandles.lookup();

        Class<?> thisClass = FlattenableSemanticTest.class;
        MethodType mt = MethodType.methodType(Void.TYPE, thisClass, argType);
        String sig = "L" + argType.getName().replace('.', '/') + ";";
        if (isStaticField) {// putstatic
            return MethodHandleBuilder
                .loadCode(mhLookup, "set_" + name, mt,
                          CODE -> {
                              CODE
                                  .aload(1)
                                  .putstatic(thisClass, name, sig)
                                  .return_();
                          }, argType);
        } else {
            return MethodHandleBuilder
                .loadCode(mhLookup, "set_" + name, mt,
                          CODE -> {
                              CODE
                                  .aload(0)
                                  .aload(1)
                                  .putfield(thisClass, name, sig)
                                  .return_();
                          }, argType);
        }
    }

    static boolean checkSetterThrowsNpe(MethodHandle mh, FlattenableSemanticTest test, Object val) {
        try {
            mh.invoke(test, val); // nfsp = null;
        } catch(Throwable t) {
            if (t instanceof NullPointerException) {
                return true;
            } else {
                throw new RuntimeException(t);
            }
        }
        return false;
    }
}
