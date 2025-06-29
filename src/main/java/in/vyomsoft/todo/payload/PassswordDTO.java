package in.vyomsoft.todo.payload;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassswordDTO {
    private String oldPassword;
    private String newPassword;
}
