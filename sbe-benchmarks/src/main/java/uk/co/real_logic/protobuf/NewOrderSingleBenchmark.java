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
package uk.co.real_logic.protobuf;

import org.openjdk.jmh.annotations.*;
import com.google.protobuf.ByteString;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class NewOrderSingleBenchmark
{
    @State(Scope.Benchmark)
    public static class MyState
    {
    	final byte[] decodeBuffer;

        {
            try
            {
                decodeBuffer = encode();
            }
            catch (final Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public byte[] testEncode(final MyState state) throws Exception
    {
    	return encode();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public Fix.NewOrderSingle testDecode(final MyState state) throws Exception
    {
    	final byte[] buffer = state.decodeBuffer;

        return decode(buffer);
    }

    static ByteString accountBytes = ByteString.copyFromUtf8("ACC123");
    static ByteString clOrdIdBytes = ByteString.copyFromUtf8("ClOrdID999");
    static ByteString symbolBytes = ByteString.copyFromUtf8("1234");

    
    public static byte[] encode()
    {
    	
    	final Fix.NewOrderSingle.Builder newOrder = Fix.NewOrderSingle.newBuilder();

        newOrder.clear()
                  .setTransactTime(1234L);
        
        newOrder.setAccount(accountBytes);
        newOrder.setClOrdID(clOrdIdBytes);
        newOrder.setHandInst(Fix.NewOrderSingle.HandInst.AUTOMATIC);
        newOrder.getOrderQtyBuilder().setMantissa(10);
        newOrder.setOrdType(Fix.NewOrderSingle.OrdType.LIMIT_ORDER);
        newOrder.getPriceBuilder().setMantissa(10000);
        newOrder.setSide(Fix.NewOrderSingle.Side.BUY);
        newOrder.setSymbol(symbolBytes);
        newOrder.setTif(Fix.NewOrderSingle.TimeInForce.DAY);
        
        return newOrder.build().toByteArray();
    }

    static byte decodeArray[] = new byte[128];

    private static Fix.NewOrderSingle decode(final byte[] buffer) throws Exception
    {
    	final Fix.NewOrderSingle newOrder = Fix.NewOrderSingle.parseFrom(buffer);
	
    	newOrder.getTransactTime();
        newOrder.getAccount();
        newOrder.getClOrdID();
        newOrder.getHandInst();
        newOrder.getOrderQty();
        newOrder.getOrdType();
        newOrder.getPrice();
        newOrder.getSide();
        newOrder.getSymbol();
        newOrder.getTif();
        
        return newOrder;
    }

    /*
     * Benchmarks to allow execution outside of JMH.
     */

    public static void main(final String[] args)
    {
    	try 
    	{
	        for (int i = 0; i < 10; i++)
	        {
	            perfTestEncode(i);
	            perfTestDecode(i);
	        }
    	}
    	catch (Exception e) 
    	{
    		System.exit(1);
    	}
    }

    private static void perfTestEncode(final int runNumber) throws Exception
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final NewOrderSingleBenchmark benchmark = new NewOrderSingleBenchmark();

        final long start = System.nanoTime();
        byte[] newOrderBytes = null;
        for (int i = 0; i < reps; i++)
        {
            newOrderBytes = benchmark.testEncode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testEncode() - message encodedLength %d%n",
            runNumber,
            totalDuration / reps,
            benchmark.getClass().getName(),
            Integer.valueOf(newOrderBytes.length));
    }

    private static void perfTestDecode(final int runNumber) throws Exception
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final NewOrderSingleBenchmark benchmark = new NewOrderSingleBenchmark();

        final long start = System.nanoTime();
        Fix.NewOrderSingle newOrder = null;
        for (int i = 0; i < reps; i++)
        {
            newOrder = benchmark.testDecode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testDecode() - message encodedLength %d%n",
            runNumber,
            totalDuration / reps,
            benchmark.getClass().getName(),
            state.decodeBuffer.length);
    }
}
