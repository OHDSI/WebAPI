package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KNearestNeighbors {
  @JsonProperty("k")
  private Integer k = 1000;

  @JsonProperty("indexFolder")
  private String indexFolder = null;

  public KNearestNeighbors k(Integer k) {
    this.k = k;
    return this;
  }

  /**
   * The number of neighbors to consider 
   * @return k
   **/
  @JsonProperty("k")
  public Integer getK() {
    return k;
  }

  public void setK(Integer k) {
    this.k = k;
  }

  public KNearestNeighbors indexFolder(String indexFolder) {
    this.indexFolder = indexFolder;
    return this;
  }

  /**
   * The directory where the results and intermediate steps are output 
   * @return indexFolder
   **/
  @JsonProperty("indexFolder")
  public String getIndexFolder() {
    return indexFolder;
  }

  public void setIndexFolder(String indexFolder) {
    this.indexFolder = indexFolder;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KNearestNeighbors knearestNeighbors = (KNearestNeighbors) o;
    return Objects.equals(this.k, knearestNeighbors.k) &&
        Objects.equals(this.indexFolder, knearestNeighbors.indexFolder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(k, indexFolder);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KNearestNeighbors {\n");
    
    sb.append("    k: ").append(toIndentedString(k)).append("\n");
    sb.append("    indexFolder: ").append(toIndentedString(indexFolder)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }    
}
