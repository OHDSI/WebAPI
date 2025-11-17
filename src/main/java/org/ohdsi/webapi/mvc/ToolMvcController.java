package org.ohdsi.webapi.mvc;

import org.ohdsi.webapi.tool.ToolServiceImpl;
import org.ohdsi.webapi.tool.dto.ToolDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tool")
public class ToolMvcController extends AbstractMvcController {
    private final ToolServiceImpl service;

    public ToolMvcController(ToolServiceImpl service) {
        this.service = service;
    }

    @GetMapping("")
    public ResponseEntity<List<ToolDTO>> getTools() {
        return ok(service.getTools());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToolDTO> getToolById(@PathVariable("id") Integer id) {
        return ok(service.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<ToolDTO> createTool(@RequestBody ToolDTO dto) {
        return ok(service.saveTool(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ok();
    }

    @PutMapping("")
    public ResponseEntity<ToolDTO> updateTool(@RequestBody ToolDTO toolDTO) {
        return ok(service.saveTool(toolDTO));
    }
}
