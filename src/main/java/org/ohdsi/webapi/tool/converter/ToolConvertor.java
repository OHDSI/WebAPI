package org.ohdsi.webapi.tool.converter;

import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.tool.Tool;
import org.ohdsi.webapi.tool.ToolRepository;
import org.ohdsi.webapi.tool.dto.ToolDTO;
import org.ohdsi.webapi.util.DateUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class ToolConvertor extends AbstractDaoService {
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;

    public ToolConvertor(ToolRepository toolRepository, UserRepository userRepository) {
        this.toolRepository = toolRepository;
        this.userRepository = userRepository;
    }

    public Tool toEntity(ToolDTO toolDTO) {
        boolean isNewTool = toolDTO.getId() == null;
        Tool tool = isNewTool ? new Tool() : toolRepository.findOne(toolDTO.getId());
        Instant currentInstant = Instant.now();
        if (isNewTool) {
            setCreationDetails(tool, currentInstant);
        } else {
            setModificationDetails(tool, currentInstant);
        }
        updateToolFromDTO(tool, toolDTO);
        return tool;
    }

    private void setCreationDetails(Tool tool, Instant currentInstant) {
        tool.setCreatedDate(Date.from(currentInstant));
        tool.setCreatedBy(getCurrentUser());
    }

    private void setModificationDetails(Tool tool, Instant currentInstant) {
        tool.setModifiedDate(Date.from(currentInstant));
        tool.setModifiedBy(getCurrentUser());
    }

    private void updateToolFromDTO(Tool tool, ToolDTO toolDTO) {
        Optional.ofNullable(toolDTO.getName()).ifPresent(tool::setName);
        Optional.ofNullable(toolDTO.getUrl()).ifPresent(tool::setUrl);
        Optional.ofNullable(toolDTO.getDescription()).ifPresent(tool::setDescription);
        Optional.ofNullable(toolDTO.getEnabled()).ifPresent(tool::setEnabled);
    }

    public ToolDTO toDTO(Tool tool) {
        return Optional.ofNullable(tool)
                .map(t -> {
                    ToolDTO toolDTO = new ToolDTO();
                    toolDTO.setId(t.getId());
                    toolDTO.setName(t.getName());
                    toolDTO.setUrl(t.getUrl());
                    toolDTO.setDescription(t.getDescription());
                    Optional.ofNullable(tool.getCreatedBy())
                            .map(UserEntity::getId)
                            .map(userRepository::findOne)
                            .map(UserEntity::getName)
                            .ifPresent(toolDTO::setCreatedByName);
                    Optional.ofNullable(tool.getModifiedBy())
                            .map(UserEntity::getId)
                            .map(userRepository::findOne)
                            .map(UserEntity::getName)
                            .ifPresent(toolDTO::setModifiedByName);
                    toolDTO.setCreatedDate(DateUtils.dateToString(t.getCreatedDate()));
                    toolDTO.setModifiedDate(DateUtils.dateToString(t.getModifiedDate()));
                    toolDTO.setEnabled(t.getEnabled());
                    return toolDTO;
                })
                .orElse(null);
    }
}
