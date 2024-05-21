package org.ohdsi.webapi.versioning;

import org.ohdsi.webapi.reusable.ReusableService;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.dto.ReusableVersionFullDTO;
import org.ohdsi.webapi.reusable.repository.ReusableRepository;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ReusableVersioningTest extends BaseVersioningTest<ReusableDTO, ReusableVersionFullDTO, Integer> {
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

    protected void checkClientDTOEquality(ReusableVersionFullDTO fullDTO) {
        ReusableDTO dto = fullDTO.getEntityDTO();
        checkEquality(dto);
    }

    protected void checkServerDTOEquality(ReusableDTO dto) {
        checkEquality(dto);
    }

    private void checkEquality(ReusableDTO dto) {
        assertNotEquals(dto.getName(), initialDTO.getName());
        assertEquals(dto.getTags().size(), 0);
    }

    @Override
    protected ReusableDTO getEntity(Integer id) {
        return service.getDTOById(id);
    }

    @Override
    protected ReusableDTO updateEntity(ReusableDTO dto) {
        return service.update(dto.getId(), dto);
    }

    @Override
    protected ReusableDTO copyAssetFromVersion(Integer id, int version) {
        return service.copyAssetFromVersion(id, version);
    }

    @Override
    protected Integer getId(ReusableDTO dto) {
        return dto.getId();
    }

    @Override
    protected String getExpressionPath() {
        return null;
    }

    @Override
    protected List<VersionDTO> getVersions(Integer id) {
        return service.getVersions(id);
    }

    @Override
    protected ReusableVersionFullDTO getVersion(Integer id, int version) {
        return service.getVersion(id, version);
    }

    @Override
    protected void updateVersion(Integer id, int version, VersionUpdateDTO updateDTO) {
        service.updateVersion(id, version, updateDTO);
    }

    @Override
    protected void deleteVersion(Integer id, int version) {
        service.deleteVersion(id, version);
    }

    @Override
    public void doClear() {
        repository.deleteAll();
    }
}
