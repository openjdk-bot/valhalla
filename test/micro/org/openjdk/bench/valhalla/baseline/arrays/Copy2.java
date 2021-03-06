/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.bench.valhalla.baseline.arrays;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.bench.valhalla.SizedBase;
import org.openjdk.bench.valhalla.baseline.types.Ref2;
import org.openjdk.bench.valhalla.baseline.types.Utils;
import org.openjdk.bench.valhalla.types.Vector;

public class Copy2 extends SizedBase {

    int[] srcPrimitive;
    int[] dstPrimitive;
    Ref2[] srcReference;
    Ref2[] dstReference;
    Vector[] srcCovariance;
    Vector[] dstCovariance;

    @Setup
    public void setup() {
        srcPrimitive = Utils.fill(new int[size * 2]);
        dstPrimitive = new int[size * 2];
        srcReference = Utils.fill(new Ref2[size]);
        dstReference = new Ref2[size];
        srcCovariance = Utils.fill(new Ref2[size]);
        dstCovariance = new Ref2[size];
    }

    @Benchmark
    public void primitive() {
        int[] src = srcPrimitive;
        int[] dst = dstPrimitive;
        for (int i = 0; i < size * 2; i++) {
            dst[i] = src[i];
        }
    }

    @Benchmark
    public void reference() {
        Ref2[] src = srcReference;
        Ref2[] dst = dstReference;
        for (int i = 0; i < size; i++) {
            dst[i] = src[i];
        }
    }

    @Benchmark
    public void covariance() {
        Vector[] src = srcCovariance;
        Vector[] dst = dstCovariance;
        for (int i = 0; i < size; i++) {
            dst[i] = src[i];
        }
    }
}
