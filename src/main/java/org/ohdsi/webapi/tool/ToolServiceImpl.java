package org.ohdsi.webapi.tool;

import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.tool.converter.ToolConvertor;
import org.ohdsi.webapi.tool.dto.ToolDTO;
import org.springframework.stereotype.Service;

import javax.ws.rs.ForbiddenException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToolServiceImpl extends AbstractDaoService implements ToolService {
    private final ToolRepository toolRepository;
    private final ToolConvertor toolConvertor;
    private final PermissionManager permissionManager;

    public ToolServiceImpl(ToolRepository toolRepository, ToolConvertor toolConvertor, PermissionManager permissionManager) {
        this.toolRepository = toolRepository;
        this.toolConvertor = toolConvertor;
        this.permissionManager = permissionManager;
    }

    @Override
    public List<ToolDTO> getTools() {
        return toolRepository.findAll().stream()
                .map(tool -> {
                    ToolDTO dto = toolConvertor.toDTO(tool);
                    UserEntity userById = permissionManager.getUserById(tool.getCreatedBy().getId());
                    dto.setCreatedByName(userById.getName());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    public ToolDTO saveTool(ToolDTO toolDTO) {
        if (!isAdmin()) {
            throw new ForbiddenException();
        }
        UserEntity currentUser = permissionManager.getCurrentUser();
        Tool tool = updateToolFromDTO(toolDTO, currentUser);
        return toolConvertor.toDTO(toolRepository.saveAndFlush(tool));
    }

    private Tool updateToolFromDTO(ToolDTO toolDTO, UserEntity currentUser) {
        Tool tool = toolConvertor.toEntity(toolDTO);
        if (toolDTO.getId() == null) {
            tool.setCreatedBy(currentUser);
        }
        tool.setModifiedBy(currentUser);
        return tool;
    }

    @Override
    public ToolDTO getById(Integer id) {
        return toolConvertor.toDTO(toolRepository.findOne(id));
    }

    @Override
    public void delete(Integer id) {
        if (isAdmin()) {
            toolRepository.delete(id);
        } else {
            throw new ForbiddenException();
        }
    }
}