package org.ohdsi.webapi.tagging;

import org.ohdsi.webapi.reusable.ReusableController;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.repository.ReusableRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class ReusableTaggingTest extends BaseTaggingTest<ReusableDTO, Integer> {
    @Autowired
    private ReusableController controller;

    @Autowired
    private ReusableRepository repository;

    @Override
    public void doCreateInitialData() throws IOException {
        ReusableDTO dto = new ReusableDTO();
        dto.setData("test data");
        dto.setName("test name");
        dto.setDescription("test description");

        initialDTO = controller.create(dto);
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
        controller.assignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignTag(Integer id, boolean isPermissionProtected) {
        controller.unassignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void assignProtectedTag(Integer id, boolean isPermissionProtected) {
        controller.assignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignProtectedTag(Integer id, boolean isPermissionProtected) {
        controller.unassignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected ReusableDTO getDTO(Integer id) {
        return controller.get(id);
    }

    @Override
    protected Integer getId(ReusableDTO dto) {
        return dto.getId();
    }
}
