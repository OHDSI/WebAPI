package org.ohdsi.webapi.reusable;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.repository.ReusableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReusableService extends AbstractDaoService {
    private final ReusableRepository reusableRepository;
    private final EntityManager entityManager;
    private final ConversionService conversionService;

    @Autowired
    public ReusableService(
            ReusableRepository reusableRepository,
            EntityManager entityManager,
            ConversionService conversionService) {
        this.reusableRepository = reusableRepository;
        this.entityManager = entityManager;
        this.conversionService = conversionService;
    }

    public ReusableDTO create(ReusableDTO dto) {
        Reusable reusable = conversionService.convert(dto, Reusable.class);
        Reusable saved = create(reusable);
        return conversionService.convert(saved, ReusableDTO.class);
    }

    public Reusable create(Reusable reusable) {
        reusable.setCreatedBy(getCurrentUser());
        reusable.setCreatedDate(new Date());

        return save(reusable);
    }

    public Reusable getById(Integer id) {
        return reusableRepository.findOne(id);
    }

    public ReusableDTO getDTOById(Integer id) {
        Reusable reusable = reusableRepository.findOne(id);
        return conversionService.convert(reusable, ReusableDTO.class);
    }

    public List<ReusableDTO> listDTO() {
        return list().stream()
                .map(reusable -> conversionService.convert(reusable, ReusableDTO.class))
                .collect(Collectors.toList());
    }

    public List<Reusable> list() {
        return reusableRepository.findAll();
    }

    public ReusableDTO update(Integer id, ReusableDTO entity) {
        Reusable existing = reusableRepository.findOne(id);

        checkOwnerOrAdmin(existing.getCreatedBy());

        Reusable toUpdate = this.conversionService.convert(entity, Reusable.class);

        toUpdate.setCreatedBy(existing.getCreatedBy());
        toUpdate.setCreatedDate(existing.getCreatedDate());
        toUpdate.setModifiedBy(getCurrentUser());
        toUpdate.setModifiedDate(new Date());

        Reusable saved = save(toUpdate);
        return conversionService.convert(saved, ReusableDTO.class);
    }

    @Transactional
    public void assignTag(int id, int tagId, boolean isPermissionProtected) {
        Reusable entity = getById(id);
        assignTag(entity, tagId, isPermissionProtected);
    }

    @Transactional
    public void unassignTag(int id, int tagId, boolean isPermissionProtected) {
        Reusable entity = getById(id);
        unassignTag(entity, tagId, isPermissionProtected);
    }

    public void delete(Integer id) {
        Reusable existing = reusableRepository.findOne(id);

        checkOwnerOrAdmin(existing.getCreatedBy());

        reusableRepository.delete(id);
    }

    private Reusable save(Reusable reusable) {
        reusable = reusableRepository.saveAndFlush(reusable);
        entityManager.refresh(reusable);
        return reusableRepository.findOne(reusable.getId());
    }
}
