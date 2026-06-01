package in.vyomsoft.todo.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

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
    private WeatherDTO weather;
}