package com.cardanoj.plutus.spec;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import co.nstant.in.cbor.model.Number;
import com.cardanoj.common.cbor.CborSerializationUtil;
import com.cardanoj.crypto.Blake2bUtil;
import com.cardanoj.exception.CborDeserializationException;
import com.cardanoj.exception.CborRuntimeException;
import com.cardanoj.exception.CborSerializationException;
import com.cardanoj.util.HexUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.*;
import lombok.NonNull;
import lombok.*;

public interface PlutusData {
    static PlutusData unit() {
        return ConstrPlutusData.builder().data(ListPlutusData.of(new PlutusData[0])).build();
    }

    DataItem serialize() throws CborSerializationException;

    static PlutusData deserialize(DataItem dataItem) throws CborDeserializationException {
        if (dataItem == null) {
            return null;
        } else if (dataItem instanceof Number) {
            return BigIntPlutusData.deserialize((Number)dataItem);
        } else if (dataItem instanceof ByteString) {
            return BytesPlutusData.deserialize((ByteString)dataItem);
        } else if (dataItem instanceof UnicodeString) {
            return BytesPlutusData.deserialize((UnicodeString)dataItem);
        } else if (dataItem instanceof Array) {
            return (PlutusData)(dataItem.getTag() == null ? ListPlutusData.deserialize((Array)dataItem) : ConstrPlutusData.deserialize(dataItem));
        } else if (dataItem instanceof Map) {
            return MapPlutusData.deserialize((Map)dataItem);
        } else {
            throw new CborDeserializationException("Cbor deserialization failed. Invalid type. " + dataItem);
        }
    }

    static PlutusData deserialize(@NonNull byte[] serializedBytes) throws CborDeserializationException {
        if (serializedBytes == null) {
            throw new NullPointerException("serializedBytes is marked non-null but is null");
        } else {
            try {
                DataItem dataItem = (DataItem)CborDecoder.decode(serializedBytes).get(0);
                return deserialize(dataItem);
            } catch (CborDeserializationException | CborException var2) {
                throw new CborDeserializationException("Cbor de-serialization error", var2);
            }
        }
    }

    @JsonIgnore
    default String getDatumHash() {
        return HexUtil.encodeHexString(this.getDatumHashAsBytes());
    }

    @JsonIgnore
    default byte[] getDatumHashAsBytes() {
        try {
            return Blake2bUtil.blake2bHash256(CborSerializationUtil.serialize(this.serialize()));
        } catch (CborException | CborSerializationException var2) {
            throw new CborRuntimeException("Cbor serialization error", var2);
        }
    }

    default String serializeToHex() {
        try {
            return HexUtil.encodeHexString(CborSerializationUtil.serialize(this.serialize()));
        } catch (Exception var2) {
            throw new CborRuntimeException("Cbor serialization error", var2);
        }
    }

    default byte[] serializeToBytes() {
        try {
            return CborSerializationUtil.serialize(this.serialize());
        } catch (Exception var2) {
            throw new CborRuntimeException("Cbor serialization error", var2);
        }
    }
}
