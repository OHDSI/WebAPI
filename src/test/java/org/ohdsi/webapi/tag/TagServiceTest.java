package org.ohdsi.webapi.tag;

import org.junit.After;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.exception.BadRequestAtlasException;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagType;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TagServiceTest extends AbstractDatabaseTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @After
    public void cleanupTags() {
        tagRepository.deleteAll();
    }

    @Test
    public void createsUngroupedTagWhenGroupsAreExplicitlyEmpty() {
        TagDTO tag = tagDTO("tag-group-" + System.nanoTime());
        tag.setGroups(Collections.emptySet());
        tag.setShowGroup(true);
        tag.setMultiSelection(true);
        tag.setAllowCustom(true);

        TagDTO created = tagService.create(tag);

        assertEquals(TagType.CUSTOM, created.getType());
        assertTrue(created.getGroups().isEmpty());
        assertTrue(created.isShowGroup());
        assertTrue(created.isMultiSelection());
        assertTrue(created.isAllowCustom());
    }

    @Test
    public void createsTagInExistingGroupThatAllowsCustomTags() {
        Tag parent = saveGroup("parent-" + System.nanoTime(), true);
        TagDTO tag = tagDTO("child-" + System.nanoTime());
        tag.setGroups(Collections.singleton(groupDTO(parent.getId())));

        TagDTO created = tagService.create(tag);

        assertEquals(TagType.CUSTOM, created.getType());
        assertEquals(1, created.getGroups().size());
        assertEquals(parent.getId(), created.getGroups().iterator().next().getId());
    }

    @Test
    public void rejectsTagInExistingGroupThatDoesNotAllowCustomTags() {
        Tag parent = saveGroup("restricted-parent-" + System.nanoTime(), false);
        TagDTO tag = tagDTO("child-" + System.nanoTime());
        tag.setGroups(Collections.singleton(groupDTO(parent.getId())));

        BadRequestAtlasException exception = assertThrows(
                BadRequestAtlasException.class,
                () -> tagService.create(tag));

        assertTrue(exception.getMessage().contains(parent.getName()));
    }

    @Test
    public void rejectsTagWhenGroupsAreNull() {
        TagDTO tag = tagDTO("tag-without-groups-" + System.nanoTime());

        BadRequestAtlasException exception = assertThrows(
                BadRequestAtlasException.class,
                () -> tagService.create(tag));

        assertEquals("A tag must be assigned to at least one tag group.", exception.getMessage());
    }

    private Tag saveGroup(String name, boolean allowCustom) {
        Tag group = new Tag();
        group.setName(name);
        group.setType(TagType.CUSTOM);
        group.setAllowCustom(allowCustom);
        group.setGroups(new HashSet<>());
        group.setCreatedDate(new Date());
        return tagRepository.saveAndFlush(group);
    }

    private TagDTO tagDTO(String name) {
        TagDTO tag = new TagDTO();
        tag.setName(name);
        return tag;
    }

    private TagDTO groupDTO(Integer id) {
        TagDTO group = new TagDTO();
        group.setId(id);
        return group;
    }
}