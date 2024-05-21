package org.ohdsi.webapi.tagging;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public abstract class BaseTaggingTest<T extends CommonEntityExtDTO, ID extends Number> extends AbstractDatabaseTest {
    protected T initialDTO;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected ConversionService conversionService;
    
    private Tag tag1, tag2, tag3, protectedTag;

    protected String getExpression(String path) throws IOException {
        File ple_spec = ResourceUtils.getFile(Objects.requireNonNull(this.getClass().getResource(path)));
        return FileUtils.readFileToString(ple_spec, StandardCharsets.UTF_8);
    }

    @Before
    public void createInitialData() throws IOException {
        UserEntity user = new UserEntity();
        user.setLogin("anonymous");
        userRepository.save(user);

        this.protectedTag = new Tag();
        this.protectedTag.setName("protected tag name");
        this.protectedTag.setPermissionProtected(true);
        this.protectedTag.setType(TagType.SYSTEM);
        this.protectedTag.setCreatedDate(new Date());
        tagRepository.save(this.protectedTag);

        this.tag1 = new Tag();
        this.tag1.setName("tag name");
        this.tag1.setPermissionProtected(false);
        this.tag1.setType(TagType.SYSTEM);
        this.tag1.setCreatedDate(new Date());
        tagRepository.save(this.tag1);

        this.tag2 = new Tag();
        this.tag2.setName("tag name 2");
        this.tag2.setPermissionProtected(false);
        this.tag2.setType(TagType.SYSTEM);
        this.tag2.setCreatedDate(new Date());
        tagRepository.save(this.tag2);

        this.tag3 = new Tag();
        this.tag3.setName("tag name 3");
        this.tag3.setPermissionProtected(false);
        this.tag3.setType(TagType.SYSTEM);
        this.tag3.setCreatedDate(new Date());
        tagRepository.save(this.tag3);

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

    @Test
    public void assignProtectedTag() {
        assignProtectedTag(getId(initialDTO), true);

        T dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 1);
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

    @Test
    public void unassignProtectedTag() {
        assignProtectedTag();
        T dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 1);

        unassignProtectedTag(getId(initialDTO), true);

        dto = getDTO(getId(initialDTO));
        assertEquals(dto.getTags().size(), 0);
    }

    @Test
    public void byTags() {
        assignTags(getId(initialDTO), this.tag1, this.tag2);

        List<T> dtos = getDTOsByTag(Arrays.asList("tag name", "tag name 2"));
        assertEquals(dtos.size(), 1);

        dtos = getDTOsByTag(Collections.singletonList("tag name"));
        assertEquals(dtos.size(), 1);

        dtos = getDTOsByTag(Collections.singletonList("tag name 2"));
        assertEquals(dtos.size(), 1);

        dtos = getDTOsByTag(Collections.singletonList("tag name 3"));
        assertEquals(dtos.size(), 0);

        T dto = doCopyData(initialDTO);
        assignTags(getId(dto), this.tag3);

        dtos = getDTOsByTag(Arrays.asList("tag name", "tag name 2"));
        assertEquals(dtos.size(), 1);

        dtos = getDTOsByTag(Arrays.asList("tag name", "tag name 2", "tag name 3"));
        assertEquals(dtos.size(), 0);

        dtos = getDTOsByTag(Collections.singletonList("tag name"));
        assertEquals(dtos.size(), 1);

        dtos = getDTOsByTag(Collections.singletonList("tag name 2"));
        assertEquals(dtos.size(), 1);

        dtos = getDTOsByTag(Collections.singletonList("tag name 3"));
        assertEquals(dtos.size(), 1);
        
        assignTags(getId(dto), this.tag1, this.tag2);

        dtos = getDTOsByTag(Arrays.asList("tag name", "tag name 2"));
        assertEquals(dtos.size(), 2);

        dtos = getDTOsByTag(Arrays.asList("tag name", "tag name 2", "tag name 3"));
        assertEquals(dtos.size(), 1);
    }

    protected Tag getTag(boolean isProtected) {
        return tagRepository.findAll().stream()
                .filter(t -> t.isPermissionProtected() == isProtected)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cannot get tag"));
    }

    protected List<Tag> getTags(boolean isProtected) {
        return tagRepository.findAll().stream()
                .filter(t -> t.isPermissionProtected() == isProtected)
                .collect(Collectors.toList());
    }

    protected abstract void doCreateInitialData() throws IOException;

    protected abstract T doCopyData(T def);

    protected abstract void doClear();

    protected abstract String getExpressionPath();

    protected abstract void assignTag(ID id, boolean isPermissionProtected);

    protected abstract void assignTags(ID id, Tag...tags);

    protected abstract void unassignTag(ID id, boolean isPermissionProtected);

    protected abstract void assignProtectedTag(ID id, boolean isPermissionProtected);

    protected abstract void unassignProtectedTag(ID id, boolean isPermissionProtected);

    protected abstract T getDTO(ID id);

    protected abstract ID getId(T dto);

    protected abstract List<T> getDTOsByTag(List<String> tagNames);
}
