package in.vyomsoft.todo.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDTO {
    private String name;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String username;
    private String dpUrl;
    private String deleteUrl;
    private int pictureChangeCount;
    private LocalDate pictureChangeWindowStart;
}