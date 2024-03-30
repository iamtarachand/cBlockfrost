package com.cardanoj.plutus.spec.serializers;

import com.cardanoj.plutus.spec.ConstrPlutusData;
import com.cardanoj.plutus.spec.PlutusData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstrDataJsonDeserializer extends StdDeserializer<ConstrPlutusData> {
    public ConstrDataJsonDeserializer() {
        this((Class)null);
    }

    public ConstrDataJsonDeserializer(Class<?> clazz) {
        super(clazz);
    }

    public ConstrPlutusData deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = (JsonNode)jp.getCodec().readTree(jp);
        if (!node.has("constructor")) {
            throw new IllegalArgumentException("Invalid json for ConstrPlutusData. " + node);
        } else {
            long alternative = node.get("constructor").asLong();
            ArrayNode fieldsNode = (ArrayNode)node.get("fields");
            List<PlutusData> plutusDataList = new ArrayList();

            for(int i = 0; i < fieldsNode.size(); ++i) {
                plutusDataList.add(PlutusDataJsonConverter.toPlutusData(fieldsNode.get(i)));
            }

            return ConstrPlutusData.of(alternative, (PlutusData[])plutusDataList.toArray(new PlutusData[0]));
        }
    }
}
