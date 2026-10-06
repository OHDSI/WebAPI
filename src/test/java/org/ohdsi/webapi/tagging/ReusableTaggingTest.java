package org.ohdsi.webapi.tagging;

import org.ohdsi.webapi.reusable.ReusableService;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.repository.ReusableRepository;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class ReusableTaggingTest extends BaseTaggingTest<ReusableDTO, Integer> {
    @Autowired
    private ReusableService service;

    @Autowired
    private ReusableRepository repository;

    @Override
    public void doCreateInitialData() throws IOException {
        ReusableDTO dto = new ReusableDTO();
        dto.setData("test data");
        dto.setName("test name");
        dto.setDescription("test description");

        initialDTO = service.create(dto);
    }

    @Override
    protected ReusableDTO doCopyData(ReusableDTO def) {
        return service.copy(def.getId());
    }

    @Override
    protected void doClear() {
        repository.deleteAll();
    }

    @Override
    protected String getExpressionPath() {
        return null;
    }

    @Override
    protected void assignTag(Integer id, boolean isPermissionProtected) {
        service.assignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignTag(Integer id, boolean isPermissionProtected) {
        service.unassignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void assignProtectedTag(Integer id, boolean isPermissionProtected) {
        service.assignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignProtectedTag(Integer id, boolean isPermissionProtected) {
        service.unassignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected ReusableDTO getDTO(Integer id) {
        return service.getDTOById(id);
    }

    @Override
    protected Integer getId(ReusableDTO dto) {
        return dto.getId();
    }

    @Override
    protected void assignTags(Integer id, Tag...tags) {
        for (Tag tag : tags) {
            service.assignTag(id, tag.getId());
        }
    }

    @Override
    protected List<ReusableDTO> getDTOsByTag(List<String> tagNames) {
        TagNameListRequestDTO requestDTO = new TagNameListRequestDTO();
        requestDTO.setNames(tagNames);
        return service.listByTags(requestDTO);
    }
}
