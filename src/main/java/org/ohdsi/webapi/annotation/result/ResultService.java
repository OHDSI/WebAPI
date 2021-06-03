package org.ohdsi.webapi.annotation.result;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.result.Result;
import org.ohdsi.webapi.annotation.result.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {

  @Autowired
  private ResultRepository resultRepository;

  public List<Result> getResultsByAnnotationID(int AnnotationID) {
    return resultRepository.findByAnnotationId(AnnotationID);
  }

}
