package com.cardanoj.backendmodule.blockfrost.service;

import com.cardanoj.coreapi.exception.ApiRuntimeException;
import com.cardanoj.coreapi.model.EvaluationResult;
import com.cardanoj.coreapi.model.Result;
import com.cardanoj.plutus.spec.ExUnits;
import com.cardanoj.plutus.spec.RedeemerTag;
import com.cardanoj.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class OgmiosTxResponseParser {
    private final static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static Result<List<EvaluationResult>> processEvaluateResponse(Response<Object> response) throws IOException {
        if (!response.isSuccessful())
            return Result.error(response.errorBody().string()).withValue(Collections.emptyList()).code(response.code());

        String body = JsonUtil.getPrettyJson(response.body());
        try {
            RawResponse rawResponse = mapper.readValue(body, RawResponse.class);

            if (rawResponse == null)
                throw new ApiRuntimeException("Could not parse response");

            if (rawResponse.getType().equals("jsonwsp/fault")) {
                return Result.error(rawResponse.getFault().toString()).withValue(Collections.emptyList()).code(500);
            } else {
                JsonNode result = rawResponse.getResult();
                if (result.has("EvaluationFailure")) {
                    JsonNode evaluationFailure = result.get("EvaluationFailure");
                    return Result.error(evaluationFailure.toString()).withValue(Collections.emptyList()).code(500);
                }

                List<EvaluationResult> evaluationResults = new ArrayList<>();
                if (result.has("EvaluationResult")) {
                    JsonNode evaluationResultsObj = result.get("EvaluationResult");

                    Iterator<String> keys = evaluationResultsObj.fieldNames();

                    while (keys.hasNext()) {
                        String redeemerPointer = keys.next();

                        JsonNode evalRes = evaluationResultsObj.get(redeemerPointer);
                        BigInteger memory = evalRes.get("memory").bigIntegerValue();
                        BigInteger steps = evalRes.get("steps").bigIntegerValue();

                        String[] splits = redeemerPointer.split(":");
                        if (splits.length != 2)
                            throw new RuntimeException("Invalid redeemer pointer : " + evaluationResultsObj);

                        RedeemerTag redeemerTag;
                        if ("spend".equals(splits[0]))
                            redeemerTag = RedeemerTag.Spend;
                        else if ("mint".equals(splits[0]))
                            redeemerTag = RedeemerTag.Mint;
                        else if ("certificate".equals(splits[0]))
                            redeemerTag = RedeemerTag.Cert;
                        else if ("withdrawal".equals(splits[0]))
                            redeemerTag = RedeemerTag.Reward;
                        else
                            throw new RuntimeException("Invalid RedeemerTag in evaluateTx reponse: " + splits[0]);

                        EvaluationResult evaluationResult = new EvaluationResult();
                        evaluationResult.setRedeemerTag(redeemerTag);
                        evaluationResult.setIndex(Integer.parseInt(splits[1]));

                        ExUnits exUnits = ExUnits.builder()
                                .mem(memory)
                                .steps(steps)
                                .build();
                        evaluationResult.setExUnits(exUnits);

                        evaluationResults.add(evaluationResult);
                    }
                }

                return Result.success("OK").withValue(evaluationResults).code(200);

            }
        } catch (JsonProcessingException e) {
            return Result.error(e.getMessage()).withValue(Collections.emptyList()).code(response.code());
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static public class RawResponse {
        private String type;
        private String version;
        private String servicename;
        private String methodname;
        private JsonNode fault;
        private JsonNode result;
        private JsonNode reflection;
    }
}
