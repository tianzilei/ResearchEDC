package org.researchedc.module.fhir.dto;

public class SubmitFhirResourceRequest {
    private Long connectorId;
    private String payloadJson;

    public Long getConnectorId() { return connectorId; }
    public void setConnectorId(Long connectorId) { this.connectorId = connectorId; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}
