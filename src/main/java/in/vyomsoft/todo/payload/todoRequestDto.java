package in.vyomsoft.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class todoRequestDto {
    private String name;
    private String description;
    private boolean completed;
}
