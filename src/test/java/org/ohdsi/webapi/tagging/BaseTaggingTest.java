package org.ohdsi.webapi.tagging;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.exception.BadRequestAtlasException;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagType;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseTaggingTest<T extends CommonEntityExtDTO, ID extends Number> extends AbstractDatabaseTest {
    protected T initialDTO;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TagRepository tagRepository;

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

        Tag protectedTag = new Tag();
        protectedTag.setName("protected tag name");
        protectedTag.setPermissionProtected(true);
        protectedTag.setType(TagType.SYSTEM);
        protectedTag.setCreatedDate(new Date());
        tagRepository.save(protectedTag);

        Tag tag = new Tag();
        tag.setName("tag name");
        tag.setPermissionProtected(false);
        tag.setType(TagType.SYSTEM);
        tag.setCreatedDate(new Date());
        tagRepository.save(tag);

        doCreateInitialData();
    }

    @After
    public void clear() {
        doClear();
        tagRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void assignTag() {
        assignTag(getId(initialDTO), false);

        T dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 1);
    }

    @Test(expected = BadRequestAtlasException.class)
    public void assignTagToWrongEndpoint() {
        assignTag(getId(initialDTO), true);
    }

    @Test
    public void assignProtectedTag() {
        assignProtectedTag(getId(initialDTO), true);

        T dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 1);
    }

    @Test(expected = BadRequestAtlasException.class)
    public void assignProtectedTagToWrongEndpoint() {
        assignProtectedTag(getId(initialDTO), false);
    }

    @Test
    public void unassignTag() {
        assignTag();

        T dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 1);
        unassignTag(getId(initialDTO), false);

        dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 0);
    }

    @Test(expected = BadRequestAtlasException.class)
    public void unassignTagToWrongEndpoint() {
        assignTag();

        unassignTag(getId(initialDTO), true);
    }

    @Test
    public void unassignProtectedTag() {
        assignProtectedTag();
        T dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 1);

        unassignProtectedTag(getId(initialDTO), true);

        dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 0);
    }

    @Test(expected = BadRequestAtlasException.class)
    public void unassignProtectedTagToWrongEndpoint() {
        assignProtectedTag();
        unassignProtectedTag(getId(initialDTO), false);
    }

    protected Tag getTag(boolean isProtected) {
        return tagRepository.findAll().stream()
                .filter(t -> t.isPermissionProtected() == isProtected)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cannot get tag"));
    }

    protected abstract void doCreateInitialData() throws IOException;

    protected abstract void doClear();

    protected abstract String getExpressionPath();

    protected abstract void assignTag(ID id, boolean isPermissionProtected);

    protected abstract void unassignTag(ID id, boolean isPermissionProtected);

    protected abstract void assignProtectedTag(ID id, boolean isPermissionProtected);

    protected abstract void unassignProtectedTag(ID id, boolean isPermissionProtected);

    protected abstract T getDTO(ID id);

    protected abstract ID getId(T dto);
}
