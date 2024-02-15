package org.ohdsi.webapi.tool;

import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.management.Security;
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
    private final Security security;

    public ToolServiceImpl(ToolRepository toolRepository, ToolConvertor toolConvertor, PermissionManager permissionManager, Security security) {
        this.toolRepository = toolRepository;
        this.toolConvertor = toolConvertor;
        this.permissionManager = permissionManager;
        this.security = security;
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
        if (!isAdmin() || "anonymous".equals(security.getSubject())) { // "anonymous" is the default subject for disabled security
            throw new ForbiddenException();
        }
        UserEntity currentUser = permissionManager.getCurrentUser();
        Tool tool = saveToolFromDTO(toolDTO, currentUser);
        return toolConvertor.toDTO(toolRepository.saveAndFlush(tool));
    }

    private Tool saveToolFromDTO(ToolDTO toolDTO, UserEntity currentUser) {
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
        if (isAdmin() && !"anonymous".equals(security.getSubject())) { // "anonymous" is the default subject for disabled security
            toolRepository.delete(id);
        } else {
            throw new ForbiddenException();
        }
    }
}