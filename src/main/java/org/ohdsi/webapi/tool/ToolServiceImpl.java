package org.ohdsi.webapi.tool;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.tool.converter.ToolConvertor;
import org.ohdsi.webapi.tool.dto.ToolDTO;
import org.springframework.stereotype.Service;

@Service
public class ToolServiceImpl extends AbstractDaoService implements ToolService {
    private final ToolRepository toolRepository;
    private final ToolConvertor toolConvertor;

    public ToolServiceImpl(ToolRepository toolRepository, ToolConvertor toolConvertor) {
        this.toolRepository = toolRepository;
        this.toolConvertor = toolConvertor;
    }

    @Override
    public List<ToolDTO> getTools() {
        List<Tool> tools = (isAdmin() || canManageTools()) ? toolRepository.findAll() : toolRepository.findAllByIsEnabled(true);
        return tools.stream()
                .map(toolConvertor::toDTO).collect(Collectors.toList());
    }

    @Override
    public ToolDTO saveTool(ToolDTO toolDTO) {
        Tool tool = saveToolFromDTO(toolDTO, getCurrentUser());
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
        toolRepository.delete(id);
    }

    private boolean canManageTools() {
        return Stream.of("tool:put", "tool:post", "tool:*:delete")
                .allMatch(permission -> SecurityUtils.getSubject().isPermitted(permission));
    }
}