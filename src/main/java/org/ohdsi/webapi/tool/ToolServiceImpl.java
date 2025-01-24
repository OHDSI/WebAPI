package org.ohdsi.webapi.tool;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.tool.dto.ToolDTO;
import org.springframework.stereotype.Service;

@Service
public class ToolServiceImpl extends AbstractDaoService implements ToolService {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private final ToolRepository toolRepository;

    public ToolServiceImpl(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    @Override
    public List<ToolDTO> getTools() {
        List<Tool> tools = (isAdmin() || canManageTools()) ? toolRepository.findAll() : toolRepository.findAllByEnabled(true);
        return tools.stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ToolDTO saveTool(ToolDTO toolDTO) {
        Tool tool = saveToolFromDTO(toolDTO, getCurrentUser());
        return toDTO(toolRepository.saveAndFlush(tool));
    }

    private Tool saveToolFromDTO(ToolDTO toolDTO, UserEntity currentUser) {
        Tool tool = toEntity(toolDTO);
        if (toolDTO.getId() == null) {
            tool.setCreatedBy(currentUser);
        }
        tool.setModifiedBy(currentUser);
        return tool;
    }

    @Override
    public ToolDTO getById(Integer id) {
        return toDTO(toolRepository.findOne(id));
    }

    @Override
    public void delete(Integer id) {
        toolRepository.delete(id);
    }

    private boolean canManageTools() {
        return Stream.of("tool:put", "tool:post", "tool:*:delete")
                .allMatch(permission -> SecurityUtils.getSubject().isPermitted(permission));
    }
    
    Tool toEntity(ToolDTO toolDTO) {
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

    ToolDTO toDTO(Tool tool) {
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
                    toolDTO.setCreatedDate(t.getCreatedDate() != null ? new SimpleDateFormat(DATE_TIME_FORMAT).format(t.getCreatedDate()) : null);
                    toolDTO.setModifiedDate(t.getModifiedDate() != null ? new SimpleDateFormat(DATE_TIME_FORMAT).format(t.getModifiedDate()) : null);
                    toolDTO.setEnabled(t.getEnabled());
                    return toolDTO;
                })
                .orElse(null);
    }

}