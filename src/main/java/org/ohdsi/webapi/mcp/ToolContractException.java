package org.ohdsi.webapi.mcp;

/**
 * The call broke the tool contract (bad or missing arguments, missing rationale, a concept that
 * does not match the vocabulary) and was refused before doing anything. Audited as REJECTED.
 */
public class ToolContractException extends RuntimeException {

    public ToolContractException(String message) {
        super(message);
    }
}
