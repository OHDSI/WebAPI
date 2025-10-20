package org.ohdsi.webapi.tool;

import org.ohdsi.webapi.tool.dto.ToolDTO;

import java.util.List;

public interface ToolService {
    List<ToolDTO> getTools();
    ToolDTO saveTool(ToolDTO toolDTO);
    ToolDTO getById(Integer id);

    void delete(Integer id);
}
