package com.cardanoj.plutus.spec;


import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Tag;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.cardanoj.exception.CborDeserializationException;
import com.cardanoj.exception.CborSerializationException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.*;
import java.util.List;

@Data
@Setter
@Getter
public class ConstrPlutusData implements PlutusData {
    // see: https://github.com/input-output-hk/plutus/blob/1f31e640e8a258185db01fa899da63f9018c0e85/plutus-core/plutus-core/src/PlutusCore/Data.hs#L61
    // We don't directly serialize the alternative in the tag, instead the scheme is:
    // - Alternatives 0-6 -> tags 121-127, followed by the arguments in a list
    // - Alternatives 7-127 -> tags 1280-1400, followed by the arguments in a list
    // - Any alternatives, including those that don't fit in the above -> tag 102 followed by a list containing
    //   an unsigned integer for the actual alternative, and then the arguments in a (nested!) list.
    private static final long GENERAL_FORM_TAG = 102;
    private long alternative;
    @Setter
    private ListPlutusData data;

    public ConstrPlutusData(ListPlutusData data) {
        this.data = data;
    }

    @Builder
    public static ConstrPlutusData of(long alternative, PlutusData... plutusDataList) {
        return ConstrPlutusData.builder()
                .alternative(alternative)
                //.plutusDataList(ListPlutusData.of(plutusDataList))
                .build();
    }

    public static ConstrPlutusData deserialize(DataItem di) throws CborDeserializationException {
        Tag tag = di.getTag();
        Long alternative = null;
        ListPlutusData data = null;

        if (GENERAL_FORM_TAG == tag.getValue()) { //general form
            Array constrArray = (Array) di;
            List<DataItem> dataItems = constrArray.getDataItems();

            if (dataItems.size() != 2)
                throw new CborDeserializationException("Cbor deserialization failed. Expected 2 DataItem, found : " + dataItems.size());

            alternative = ((UnsignedInteger) dataItems.get(0)).getValue().longValue();
            data = ListPlutusData.deserialize((Array) dataItems.get(1));

        } else { //concise form
            alternative = compactCborTagToAlternative(tag.getValue());
            data = ListPlutusData.deserialize((Array) di);
        }

        return ConstrPlutusData.builder()
                .alternative(alternative)
                //.plutusDataList(data)
                .build();
    }

    private static Long alternativeToCompactCborTag(long alt) {
        if (alt <= 6) {
            return 121 + alt;
        } else if (alt >= 7 && alt <= 127) {
            return 1280 - 7 + alt;
        } else
            return null;
    }

    private static Long compactCborTagToAlternative(long cborTag) {
        if (cborTag >= 121 && cborTag <= 127) {
            return cborTag - 121;
        } else if (cborTag >= 1280 && cborTag <= 1400) {
            return cborTag - 1280 + 7;
        } else
            return null;
    }

    @Override
    public DataItem serialize() throws CborSerializationException {
        Long cborTag = alternativeToCompactCborTag(alternative);
        DataItem dataItem = null;

        if (cborTag != null) {
            // compact form
            dataItem = data.serialize();
            dataItem.setTag(cborTag);
        } else {
            //general form
            Array constrArray = new Array();
            constrArray.add(new UnsignedInteger(alternative));
            constrArray.add(data.serialize());
            dataItem = constrArray;
            dataItem.setTag(GENERAL_FORM_TAG);
        }

        return dataItem;
    }

    public static long getGeneralFormTag() {
        return GENERAL_FORM_TAG;
    }
}
