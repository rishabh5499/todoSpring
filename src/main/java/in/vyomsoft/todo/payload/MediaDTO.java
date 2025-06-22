package in.vyomsoft.todo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {
    private ImageDataDTO data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDataDTO {
        private String id;
        private String title;
        private String url_viewer;
        private ImageDetailDTO image;
        private ImageDetailDTO thumb;
        private String delete_url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDetailDTO {
        private String filename;
        private String mime;
        private String url;
    }
}
