package ws.furrify.worker.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WorkerPluginResults {
    private List<String> errors;
    private List<String> warnings;

    private String log;
}
