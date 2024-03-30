package com.cardanoj.plutus.spec.serializers;


import com.cardanoj.plutus.spec.ConstrPlutusData;
import com.cardanoj.plutus.spec.ListPlutusData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Collections;

public class ConstrDataJsonSerializer extends StdSerializer<ConstrPlutusData> {
    public ConstrDataJsonSerializer() {
        this((Class)null);
    }

    public ConstrDataJsonSerializer(Class<ConstrPlutusData> clazz) {
        super(clazz);
    }

    public void serialize(ConstrPlutusData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("constructor", value.getAlternative());
        if (value.getData() != null) {
            ListPlutusData listPlutusData = value.getData();
            gen.writeObjectField("fields", listPlutusData.getPlutusDataList());
        } else {
            gen.writeObjectField("fields", Collections.emptyList());
        }

        gen.writeEndObject();
    }
}
