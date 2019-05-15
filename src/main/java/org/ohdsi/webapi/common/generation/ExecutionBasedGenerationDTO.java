package org.ohdsi.webapi.common.generation;

public class ExecutionBasedGenerationDTO extends CommonGenerationDTO {
  private int numResultFiles;

  public int getNumResultFiles() {
    return numResultFiles;
  }

  public void setNumResultFiles(int numResultFiles) {
    this.numResultFiles = numResultFiles;
  }
}
