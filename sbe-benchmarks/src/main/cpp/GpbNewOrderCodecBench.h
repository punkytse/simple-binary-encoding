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
#ifndef _GPB_NEW_ORDER_CODEC_BENCH_HPP
#define _GPB_NEW_ORDER_CODEC_BENCH_HPP

#include <unistd.h>

#include "CodecBench.h"
#include "fix-messages.pb.h"

using namespace uk::co::real_logic::sbe::benchmarks::fix;

class GpbNewOrderCodecBench : public CodecBench<GpbNewOrderCodecBench>
{
public:
	const char accountBytes[12] {"ACC1234"};
	const char clOrdIdBytes[20] {"CLORDID999"};
	const char symbolBytes[6]  {"1234"};
	
    virtual std::uint64_t encode(char *buffer, const std::uint64_t bufferLength)
    {
        NewOrder_.set_transacttime(1234L);
        NewOrder_.set_account(accountBytes);
        NewOrder_.set_clordid(clOrdIdBytes);
        NewOrder_.set_handinst(Fix::NewOrderSingle_HandInst_AUTOMATIC);
        NewOrder_.mutable_orderqty()->set_mantissa(10LL);
        NewOrder_.set_ordtype(Fix::NewOrderSingle_OrdType_LIMIT_ORDER);
        NewOrder_.mutable_price()->set_mantissa(10000LL);
        NewOrder_.set_side(Fix::NewOrderSingle_Side_BUY);
        NewOrder_.set_symbol(symbolBytes);
        NewOrder_.set_tif(Fix::NewOrderSingle_TimeInForce_DAY);

		NewOrder_.SerializeToArray(buffer, bufferLength);
        return NewOrder_.ByteSizeLong();
    };

	//static char buffer[256];
    virtual std::uint64_t decode(const char *buffer, const std::uint64_t bufferLength)
    {
		NewOrder_.ParseFromArray(buffer, bufferLength);
    	
		NewOrder_.transacttime();
        NewOrder_.account();
        NewOrder_.clordid();
        NewOrder_.handinst();
        NewOrder_.orderqty().mantissa();
        NewOrder_.ordtype();
        NewOrder_.price().mantissa();
        NewOrder_.side();
        NewOrder_.symbol();
        NewOrder_.tif();
        
        return bufferLength;
    };

private:
    Fix::NewOrderSingle NewOrder_;
};

#endif /* _GPB_MARKET_DATA_CODEC_BENCH_HPP */
