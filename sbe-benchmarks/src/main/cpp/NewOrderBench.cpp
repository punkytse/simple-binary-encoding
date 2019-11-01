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
#include "benchlet.h"
#include "SbeNewOrderCodecBench.h"
#include "GpbNewOrderCodecBench.h"

#define MAX_MD_BUFFER (1000*1000)

class SbeNewOrderBench : public Benchmark
{
public:
    virtual void setUp(void)
    {
        buffer_ = new char[MAX_MD_BUFFER];
        bench_.runEncode(buffer_, MAX_MD_BUFFER);  // set buffer up for decoding runs
    };

    virtual void tearDown(void)
    {
        delete[] buffer_;
    };

    SbeNewOrderCodecBench bench_;
    char *buffer_;
};

class GpbNewOrderBench : public Benchmark
{
public:
    virtual void setUp(void)
    {
        buffer_ = new char[MAX_MD_BUFFER];
        int length = bench_.runEncode(buffer_, MAX_MD_BUFFER);  // set buffer up for decoding runs
        
        printf("setup encoded length = %d\n", length);

		decodeLength_ = bench_.runDecode(buffer_, length);
        printf("decode2  length = %d\n", decodeLength_);
    };

    virtual void tearDown(void)
    {
        delete[] buffer_;
    };

    GpbNewOrderCodecBench bench_;
    char *buffer_;
    int decodeLength_;
};

static struct Benchmark::Config cfg[] = {
    { Benchmark::ITERATIONS, "10000000" },
    { Benchmark::BATCHES, "20" }
};

BENCHMARK_CONFIG(SbeNewOrderBench, RunSingleEncode, cfg)
{
    bench_.runEncode(buffer_, MAX_MD_BUFFER);
}

BENCHMARK_CONFIG(SbeNewOrderBench, RunSingleDecode, cfg)
{
    bench_.runDecode(buffer_, MAX_MD_BUFFER);
}

BENCHMARK_CONFIG(SbeNewOrderBench, RunSingleEncodeAndDecode, cfg)
{
    bench_.runEncodeAndDecode(buffer_, MAX_MD_BUFFER);
}

BENCHMARK_CONFIG(GpbNewOrderBench, RunSingleEncode, cfg)
{
    bench_.runEncode(buffer_, MAX_MD_BUFFER);
}

BENCHMARK_CONFIG(GpbNewOrderBench, RunSingleDecode, cfg)
{
    bench_.runDecode(buffer_, decodeLength_);
}

BENCHMARK_CONFIG(GpbNewOrderBench, RunSingleEncodeAndDecode, cfg)
{
    bench_.runEncodeAndDecode(buffer_, MAX_MD_BUFFER);
}
