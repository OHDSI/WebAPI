package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;

public abstract class BaseCommonGenerationToDtoConverter<E extends CommonGeneration, D extends CommonGenerationDTO> extends BaseConversionServiceAwareConverter<E, D> {

  @Override
  public D convert(final E source) {
    final D resultObject = createResultObject(source);

    resultObject.setId(source.getId());
    resultObject.setStatus(source.getStatus());
    
    // Handle null source (generation from soft-deleted source)
    if (source.getSource() == null) {
      throw new IllegalStateException("Cannot convert generation with null source. The source may have been deleted.");
    }
    
    resultObject.setSourceKey(source.getSource().getSourceKey());
    resultObject.setHashCode(source.getHashCode());
    resultObject.setStartTime(source.getStartTime());
    resultObject.setEndTime(source.getEndTime());
    resultObject.setExitMessage(source.getExitMessage());

    return resultObject;
  }

}
