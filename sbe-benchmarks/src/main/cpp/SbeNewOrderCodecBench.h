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
#ifndef _SBE_NEW_ORDER_CODEC_BENCH_HPP
#define _SBE_NEW_ORDER_CODEC_BENCH_HPP

#include "CodecBench.h"
#include "uk_co_real_logic_sbe_benchmarks_fix/MessageHeader.h"
#include "uk_co_real_logic_sbe_benchmarks_fix/NewOrder.h"

using namespace uk::co::real_logic::sbe::benchmarks::fix;

class SbeNewOrderCodecBench : public CodecBench<SbeNewOrderCodecBench>
{
public:
	const char accountBytes[12] {"ACC1234"};
	const char clOrdIdBytes[20] {"CLORDID999"};
	const char symbolBytes[6]  {"1234"};
	
    virtual std::uint64_t encode(char *buffer, const std::uint64_t bufferLength)
    {
        messageHeader_.wrap(buffer, 0, 0, bufferLength);
        messageHeader_.blockLength(NewOrder_.sbeBlockLength());
        messageHeader_.templateId(NewOrder_.sbeTemplateId());
        messageHeader_.schemaId(NewOrder_.sbeSchemaId());
        messageHeader_.version(NewOrder_.sbeSchemaVersion());

        NewOrder_.wrapForEncode(buffer + messageHeader_.encodedLength(), 0, bufferLength);
        NewOrder_.transactTime(1234L);
        NewOrder_.putAccount(accountBytes);
        NewOrder_.putClOrdID(clOrdIdBytes);

        NewOrder_.handInst(HandInst::Value::AUTOMATED_EXECUTION);
        NewOrder_.orderQty().mantissa(10);
        NewOrder_.ordType(OrdType::Value::LIMIT_ORDER);
        NewOrder_.price().mantissa(10000);
        NewOrder_.side(Side::Value::BUY);
        NewOrder_.putSymbol(symbolBytes);
        NewOrder_.timeInForce(TimeInForce::Value::DAY);

        return MessageHeader::encodedLength() + NewOrder_.encodedLength();
    };

	static char buffer[256];
    virtual std::uint64_t decode(const char *buffer, const std::uint64_t bufferLength)
    {
        int64_t actingVersion;
        int64_t actingBlockLength;

        messageHeader_.wrap((char *)buffer, 0, 0, bufferLength);

        actingBlockLength = messageHeader_.blockLength();
        actingVersion = messageHeader_.version();

        NewOrder_.wrapForDecode((char *)buffer, messageHeader_.encodedLength(), actingBlockLength, actingVersion, bufferLength);
        NewOrder_.transactTime();
        
        NewOrder_.getAccount((char* const ) buffer, 12);
        NewOrder_.getClOrdID((char* const ) buffer, 20);
        NewOrder_.handInst();
        NewOrder_.orderQty().mantissa();
        NewOrder_.ordType();
        NewOrder_.price().mantissa();
        NewOrder_.side();
        NewOrder_.getSymbol((char* const ) buffer, 6);
        NewOrder_.timeInForce();

        return MessageHeader::encodedLength() + NewOrder_.encodedLength();
    };

private:
    MessageHeader messageHeader_;
    NewOrder NewOrder_;
};

#endif /* _SBE_MARKET_DATA_CODEC_BENCH_HPP */
