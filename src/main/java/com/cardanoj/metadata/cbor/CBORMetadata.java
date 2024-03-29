package com.cardanoj.metadata.cbor;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import com.cardanoj.common.cbor.CborSerializationUtil;
import com.cardanoj.crypto.Blake2bUtil;
import com.cardanoj.metadata.Metadata;
import com.cardanoj.metadata.MetadataList;
import com.cardanoj.metadata.MetadataMap;
import com.cardanoj.metadata.exception.MetadataDeSerializationException;
import com.cardanoj.metadata.exception.MetadataSerializationException;
import com.cardanoj.metadata.helper.MetadataToJsonNoSchemaConverter;
import com.cardanoj.util.JsonUtil;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static co.nstant.in.cbor.model.MajorType.*;
import static com.cardanoj.metadata.cbor.MetadataHelper.*;

public class CBORMetadata implements Metadata {
    private final Map map;

    public CBORMetadata() {
        map = new Map();
    }

    public CBORMetadata(Map map) {
        this.map = map;
    }

    @Override
    public CBORMetadata put(BigInteger key, BigInteger value) {
        map.put(new UnsignedInteger(key), new UnsignedInteger(value));
        return this;
    }

    @Override
    public CBORMetadata putNegative(BigInteger key, BigInteger value) {
        map.put(new UnsignedInteger(key), new NegativeInteger(value));
        return this;
    }

    @Override
    public CBORMetadata put(BigInteger key, byte[] value) {
        map.put(new UnsignedInteger(key), new ByteString(value));
        return this;
    }

    @Override
    public CBORMetadata put(BigInteger key, String value) {
        checkLength(value);
        map.put(new UnsignedInteger(key), new UnicodeString(value));
        return this;
    }

    @Override
    public CBORMetadata put(BigInteger key, MetadataMap mm) {
        if(map != null)
            map.put(new UnsignedInteger(key), mm.getMap());
        return this;
    }

    @Override
    public CBORMetadata put(BigInteger key, MetadataList list) {
        map.put(new UnsignedInteger(key), list.getArray());
        return this;
    }

    @Override
    public Object get(BigInteger key) {
        return extractActualValue(getData().get(objectToDataItem(key)));
    }

    @Override
    public void remove(BigInteger key) {
        this.getData().remove(objectToDataItem(key));
    }

    @Override
    public List keys() {
        return getData().getKeys().stream().map(di -> extractActualValue(di)).collect(Collectors.toList());
    }

    @Override
    public Map getData() throws MetadataSerializationException {
        return map;
    }

    public byte[] serialize() throws MetadataSerializationException {
        try {
            return CborSerializationUtil.serialize(map);
        } catch (Exception ex) {
            throw new MetadataSerializationException("CBOR serialization exception ", ex);
        }
    }

    @Override
    public Metadata merge(Metadata other) {
        CBORMetadata newMetadata = new CBORMetadata();
        if (other != null) {
            Map otherMap = other.getData();
            otherMap.getKeys().forEach(key -> newMetadata.getData().put(key, otherMap.get(key)));
        }

        this.map.getKeys().forEach(key -> newMetadata.getData().put(key, this.map.get(key)));
        return newMetadata;
    }

    public static CBORMetadata deserialize(Map metadataMap) throws MetadataDeSerializationException {
        CBORMetadata cborMetadata = new CBORMetadata();
        Collection<DataItem> keys = metadataMap.getKeys();
        for(DataItem keyDI: keys){
            DataItem valueDI = metadataMap.get(keyDI);
            BigInteger key = ((UnsignedInteger)keyDI).getValue();

            if(UNSIGNED_INTEGER.equals(valueDI.getMajorType())){
                cborMetadata.put(key, ((UnsignedInteger)valueDI).getValue());
            } else if(NEGATIVE_INTEGER.equals(valueDI.getMajorType())) {
                cborMetadata.putNegative(key, ((NegativeInteger)valueDI).getValue());
            } else if(BYTE_STRING.equals(valueDI.getMajorType())) {
                cborMetadata.put(key, ((ByteString)valueDI).getBytes());
            } else if(UNICODE_STRING.equals(valueDI.getMajorType())) {
                cborMetadata.put(key, ((UnicodeString)valueDI).getString());
            } else if(MAP.equals(valueDI.getMajorType())){
                CBORMetadataMap cborMetadataMap = new CBORMetadataMap((Map)valueDI);
                cborMetadata.put(key, cborMetadataMap);
            } else if(ARRAY.equals(valueDI.getMajorType())) {
                CBORMetadataList cborMetadataList = new CBORMetadataList((Array)valueDI);
                cborMetadata.put(key, cborMetadataList);
            } else {
                throw new MetadataDeSerializationException("Unsupported type : " + valueDI.getMajorType());
            }
        }

        return cborMetadata;
    }

    public static CBORMetadata deserialize(byte[] cborBytes) throws MetadataDeSerializationException {
        List<DataItem> dataItemList = null;
        try {
            dataItemList = CborDecoder.decode(cborBytes);
        } catch (CborException e) {
            throw new MetadataDeSerializationException("Cbor deserialization failed", e);
        }

        if (dataItemList == null || dataItemList.size() == 0)
            throw new MetadataDeSerializationException("Cbor deserialization failed. Null dataitem");

        if(dataItemList.size() > 1)
            throw new MetadataDeSerializationException("Multiple DataItems found at top level. Should be one : " + dataItemList.size());

        DataItem di = dataItemList.get(0);
        if (di instanceof Map) {
            return deserialize((Map) di);
        } else {
            throw new MetadataDeSerializationException("Unknown data type at the top level. Should be a Map. " + di.getClass());
        }
    }

    public byte[] getMetadataHash() throws MetadataSerializationException {
        byte[] encodedBytes = serialize();

        return Blake2bUtil.blake2bHash256(encodedBytes);
    }

    /**
     * Convert to json string
     * @return
     */
    public String toJson() {
        String json = MetadataToJsonNoSchemaConverter.cborBytesToJson(this.serialize());
        return JsonUtil.getPrettyJson(json);
    }

}
