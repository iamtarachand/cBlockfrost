package com.cardanoj.plutus.spec;

import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.NegativeInteger;
import co.nstant.in.cbor.model.Number;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.cardanoj.exception.CborDeserializationException;
import com.cardanoj.exception.CborSerializationException;
import com.cardanoj.plutus.spec.serializers.BigIntDataJsonDeserializer;
import com.cardanoj.plutus.spec.serializers.BigIntDataJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.math.BigInteger;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@JsonSerialize(using = BigIntDataJsonSerializer.class)
@JsonDeserialize(using = BigIntDataJsonDeserializer.class)
public class BigIntPlutusData implements PlutusData {
    private BigInteger value;

    public static BigIntPlutusData deserialize(Number numberDI) throws CborDeserializationException {
        if (numberDI == null)
            return null;

        return new BigIntPlutusData(numberDI.getValue());
    }

    public static BigIntPlutusData of(int i) {
        return new BigIntPlutusData(BigInteger.valueOf(i));
    }

    public static BigIntPlutusData of(long l) {
        return new BigIntPlutusData(BigInteger.valueOf(l));
    }

    public static BigIntPlutusData of(BigInteger b) {
        return new BigIntPlutusData(b);
    }

    @Override
    public DataItem serialize() throws CborSerializationException {
        DataItem di = null;
        if (value != null) {
            if (value.compareTo(BigInteger.ZERO) == 0 || value.compareTo(BigInteger.ZERO) == 1) {
                di = new UnsignedInteger(value);
            } else {
                di = new NegativeInteger(value);
            }
        }

        return di;
    }
}
