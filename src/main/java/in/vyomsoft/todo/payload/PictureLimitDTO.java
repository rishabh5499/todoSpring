package in.vyomsoft.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureLimitDTO {
    private int maxAllowedChanges;
    private int changesDone;
    private int changesRemaining;
}
