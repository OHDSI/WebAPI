package org.ohdsi.webapi.versioning;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionFullDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseVersioningTest<S extends CommonEntityExtDTO,
        T extends VersionFullDTO<?>, ID extends Number> extends AbstractDatabaseTest {
    protected S initialDTO;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ConversionService conversionService;

    protected String getExpression(String path) throws IOException {
        File ple_spec = ResourceUtils.getFile(Objects.requireNonNull(this.getClass().getResource(path)));
        return FileUtils.readFileToString(ple_spec, StandardCharsets.UTF_8);
    }

    @Before
    public void createInitialData() throws IOException {
        UserEntity user = new UserEntity();
        user.setLogin("anonymous");
        userRepository.save(user);

        doCreateInitialData();
    }

    protected abstract void doCreateInitialData() throws IOException;

    @After
    public void clear() {
        doClear();
        userRepository.deleteAll();
    }

    @Test
    public void createAndGetVersion() throws Exception {
        S dto = getEntity(getId(initialDTO));
        dto.setName(dto.getName() + "__copy");
        updateEntity(dto);

        List<VersionDTO> versions = getVersions(getId(dto));
        assertEquals(1, versions.size());

        VersionDTO version = versions.get(0);
        T fullDTO = getVersion(getId(dto), version.getVersion());
        assertNotNull(fullDTO);

        checkClientDTOEquality(fullDTO);
    }

    @Test
    public void deleteVersion() throws Exception {
        S dto = getEntity(getId(initialDTO));
        dto.setName(dto.getName() + "__copy");
        updateEntity(dto);

        List<VersionDTO> versions = getVersions(getId(dto));
        assertEquals(1, versions.size());

        VersionDTO version = versions.get(0);
        deleteVersion(getId(dto), version.getVersion());

        versions = getVersions(getId(dto));
        assertTrue(versions.get(0).isArchived());
    }

    @Test
    public void createAndGetMultipleVersions() throws Exception {
        S dto = getEntity(getId(initialDTO));
        int count = 3;
        for (int i = 0; i < count; i++) {
            updateEntity(dto);
        }

        List<VersionDTO> versions = getVersions(getId(dto));
        assertEquals(count, versions.size());
    }

    @Test
    public void updateVersion() throws Exception {
        S dto = getEntity(getId(initialDTO));
        dto.setName(dto.getName() + "__copy");
        updateEntity(dto);

        List<VersionDTO> versions = getVersions(getId(dto));
        VersionDTO version = versions.get(0);

        VersionUpdateDTO updateDTO = new VersionUpdateDTO();
        updateDTO.setVersion(version.getVersion());
        updateDTO.setArchived(true);
        updateDTO.setComment("test comment");

        updateVersion(getId(dto), version.getVersion(), updateDTO);

        T fullDTO = getVersion(getId(dto), version.getVersion());
        version = fullDTO.getVersionDTO();
        assertEquals(version.getComment(), updateDTO.getComment());
        assertEquals(version.isArchived(), updateDTO.isArchived());
    }

    @Test
    public void copyAssetFromVersion() throws Exception {
        S dto = getEntity(getId(initialDTO));
        dto.setName(dto.getName() + "__copy");
        updateEntity(dto);

        List<VersionDTO> versions = getVersions(getId(dto));
        VersionDTO version = versions.get(0);

        S newDTO = copyAssetFromVersion(getId(dto), version.getVersion());
        checkServerDTOEquality(newDTO);
    }

    protected abstract void doClear();

    protected abstract void checkServerDTOEquality(S dto);

    protected abstract void checkClientDTOEquality(T dto);

    protected abstract List<VersionDTO> getVersions(ID id);

    protected abstract T getVersion(ID id, int version);

    protected abstract String getExpressionPath();

    protected abstract S getEntity(ID id);

    protected abstract S updateEntity(S dto) throws Exception;

    protected abstract S copyAssetFromVersion(ID id, int version);

    protected abstract void updateVersion(ID id, int version, VersionUpdateDTO updateDTO);

    protected abstract void deleteVersion(ID id, int version);

    protected abstract ID getId(S dto);
}
