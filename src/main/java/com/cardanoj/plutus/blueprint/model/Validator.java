package com.cardanoj.plutus.blueprint.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Validator {
    private String title;
    private BlueprintDatum datum;
    private BlueprintRedeemer redeemer;
    private List<Parameter> parameters;
    private String compiledCode;
    private String hash;
}
