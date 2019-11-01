/*
 * Copyright 2013-2019 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe;

import org.openjdk.jmh.annotations.*;
import org.agrona.concurrent.UnsafeBuffer;

import uk.co.real_logic.sbe.benchmarks.fix.HandInst;
import uk.co.real_logic.sbe.benchmarks.fix.MessageHeaderDecoder;
import uk.co.real_logic.sbe.benchmarks.fix.MessageHeaderEncoder;
import uk.co.real_logic.sbe.benchmarks.fix.NewOrderDecoder;
import uk.co.real_logic.sbe.benchmarks.fix.NewOrderEncoder;
import uk.co.real_logic.sbe.benchmarks.fix.OrdType;
import uk.co.real_logic.sbe.benchmarks.fix.Side;
import uk.co.real_logic.sbe.benchmarks.fix.TimeInForce;

import java.nio.ByteBuffer;

public class NewOrderSingleBenchmark
{
    @State(Scope.Benchmark)
    public static class MyState
    {
        final int bufferIndex = 0;

        final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();

        final NewOrderEncoder newOrderEncoder =
            new NewOrderEncoder();
        final NewOrderDecoder newOrderDecoder =
            new NewOrderDecoder();

        final UnsafeBuffer encodeBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(1024));
        final UnsafeBuffer decodeBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(1024));

        {
            NewOrderSingleBenchmark.encode(messageHeaderEncoder, newOrderEncoder, decodeBuffer, bufferIndex);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public int testEncode(final MyState state)
    {
        final NewOrderEncoder newOrder = state.newOrderEncoder;
        final MessageHeaderEncoder messageHeader = state.messageHeaderEncoder;
        final UnsafeBuffer buffer = state.encodeBuffer;
        final int bufferIndex = state.bufferIndex;

        encode(messageHeader, newOrder, buffer, bufferIndex);

        return newOrder.encodedLength();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public int testDecode(final MyState state)
    {
        final NewOrderDecoder newOrder = state.newOrderDecoder;
        final MessageHeaderDecoder messageHeader = state.messageHeaderDecoder;
        final UnsafeBuffer buffer = state.decodeBuffer;
        final int bufferIndex = state.bufferIndex;

        decode(messageHeader, newOrder, buffer, bufferIndex);

        return newOrder.encodedLength();
    }

    static byte accountBytes[] = new byte[12];
    static byte clOrdIDBytes[] = new byte[20];
    static byte symbolBytes[] = new byte[6];

    public static void encode(
        final MessageHeaderEncoder messageHeader,
        final NewOrderEncoder newOrder,
        final UnsafeBuffer buffer,
        final int bufferIndex)
    {
        newOrder
            .wrapAndApplyHeader(buffer, bufferIndex, messageHeader)
            .transactTime(1234L);
        newOrder.putAccount(accountBytes, 0);
        newOrder.putClOrdID(clOrdIDBytes, 0);
        newOrder.handInst(HandInst.AUTOMATED_EXECUTION);
        newOrder.orderQty().mantissa(10);
        newOrder.ordType(OrdType.LIMIT_ORDER);
        newOrder.price().mantissa(10000);
        newOrder.side(Side.BUY);
        newOrder.putSymbol(symbolBytes, 0);
        newOrder.timeInForce(TimeInForce.DAY);
    }

    static byte decodeArray[] = new byte[128];

    private static void decode(
        final MessageHeaderDecoder messageHeader,
        final NewOrderDecoder newOrder,
        final UnsafeBuffer buffer,
        final int bufferIndex)
    {
        messageHeader.wrap(buffer, bufferIndex);

        final int actingVersion = messageHeader.version();
        final int actingBlockLength = messageHeader.blockLength();

        newOrder.wrap(buffer, bufferIndex + messageHeader.encodedLength(), actingBlockLength, actingVersion);

        newOrder.transactTime();
        newOrder.getAccount(decodeArray, 0);
        newOrder.getClOrdID(decodeArray, 0);
        newOrder.handInst();
        newOrder.orderQty();
        newOrder.ordType();
        newOrder.price();
        newOrder.side();
        newOrder.getSymbol(decodeArray, 0);
        newOrder.timeInForce();
    }

    /*
     * Benchmarks to allow execution outside of JMH.
     */

    public static void main(final String[] args)
    {
        for (int i = 0; i < 10; i++)
        {
            perfTestEncode(i);
            perfTestDecode(i);
        }
    }

    private static void perfTestEncode(final int runNumber)
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final NewOrderSingleBenchmark benchmark = new NewOrderSingleBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testEncode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testEncode() - message encodedLength %d%n",
            runNumber,
            totalDuration / reps,
            benchmark.getClass().getName(),
            state.newOrderEncoder.encodedLength() + state.messageHeaderEncoder.encodedLength());
    }

    private static void perfTestDecode(final int runNumber)
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final NewOrderSingleBenchmark benchmark = new NewOrderSingleBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testDecode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testDecode() - message encodedLength %d%n",
            runNumber,
            totalDuration / reps,
            benchmark.getClass().getName(),
            state.newOrderDecoder.encodedLength() + state.messageHeaderDecoder.encodedLength());
    }
}
