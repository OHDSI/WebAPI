package org.ohdsi.webapi.tool;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.tool.dto.ToolDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool")
public class ToolServiceImpl extends AbstractDaoService implements ToolService {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private final ToolRepository toolRepository;

    public ToolServiceImpl(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    @Override
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ToolDTO> getTools() {
        List<Tool> tools = toolRepository.findAll();
        return tools.stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ToolDTO saveTool(@RequestBody ToolDTO toolDTO) {
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
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ToolDTO getById(@PathVariable("id") Integer id) {
        return toDTO(toolRepository.findById(id).orElse(null));
    }

    @Override
    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable("id") Integer id) {
        toolRepository.deleteById(id);
    }

    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ToolDTO updateTool(@RequestBody ToolDTO toolDTO) {
        return saveTool(toolDTO);
    }

    Tool toEntity(ToolDTO toolDTO) {
        boolean isNewTool = toolDTO.getId() == null;
        Tool tool = isNewTool ? new Tool() : toolRepository.findById(toolDTO.getId()).orElse(null);
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
                            .flatMap(userRepository::findById)
                            .map(UserEntity::getName)
                            .ifPresent(toolDTO::setCreatedByName);
                    Optional.ofNullable(tool.getModifiedBy())
                            .map(UserEntity::getId)
                            .flatMap(userRepository::findById)
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