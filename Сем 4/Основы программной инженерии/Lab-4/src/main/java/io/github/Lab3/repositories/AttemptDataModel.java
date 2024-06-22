package io.github.Lab3.repositories;

import io.github.Lab3.model.CheckAreaResultsBean;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.util.List;
import java.util.Map;

@Named("attemptsList")
@SessionScoped
public class AttemptDataModel extends LazyDataModel<CheckAreaResultsBean> {
  @Inject
  private AttemptRepository service;

  @Override
  public int count(Map<String, FilterMeta> map) {
    return service.getAttemptsCount();
  }

  @Override
  public List<CheckAreaResultsBean> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {
//    return service.getAttemptsList(first, pageSize);
    return null;
  }
}
