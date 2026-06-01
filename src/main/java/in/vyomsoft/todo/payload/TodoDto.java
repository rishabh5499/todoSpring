package in.vyomsoft.todo.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TodoDto {
    private int id;
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String description;
    private boolean completed;
    private String reminder;
    private List<TodoDto> subTasks;
}
//public class TodoDto {
//    private Long id;
//    private String name;
//    private String description;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime createdAt;
//    private String reminder;
//    private boolean completed;
//}
