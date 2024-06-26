package com.cardanoj.crypto;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import com.cardanoj.common.cbor.CborSerializationUtil;
import com.cardanoj.exception.CborDeserializationException;
import com.cardanoj.exception.CborSerializationException;
import com.cardanoj.util.HexUtil;
import org.bouncycastle.util.encoders.Hex;

import java.util.List;

public class KeyGenCborUtil {

    public static String bytesToCbor(byte[] bytes) throws CborSerializationException {
        CborBuilder cborBuilder = new CborBuilder();

        try {
            List<DataItem> dataItems = cborBuilder.add(bytes).build();
            byte[] encodedBytes = CborSerializationUtil.serialize(dataItems.toArray(new DataItem[0]));

            return Hex.toHexString(encodedBytes);
        } catch (CborException e) {
            throw new CborSerializationException("Cbor serialization error", e);
        }
    }

    public static byte[] cborToBytes(String cbor) throws CborDeserializationException {
        List<DataItem> dataItemList = null;
        try {
            dataItemList = CborDecoder.decode(HexUtil.decodeHexString(cbor));
        } catch (CborException e) {
            throw new CborDeserializationException("Cbor deserialization error", e);
        }
        byte[] bytes = ((ByteString)dataItemList.get(0)).getBytes();
        return bytes;
    }

}
