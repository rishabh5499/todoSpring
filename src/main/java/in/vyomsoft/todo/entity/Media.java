package in.vyomsoft.todo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId;

    private String mediaId;
    private String title;
    private String urlViewer;
    private String deleteUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "filename", column = @Column(name = "image_filename")),
            @AttributeOverride(name = "mime", column = @Column(name = "image_mime")),
            @AttributeOverride(name = "url", column = @Column(name = "image_url"))
    })
    private ImageDetail image;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "filename", column = @Column(name = "thumb_filename")),
            @AttributeOverride(name = "mime", column = @Column(name = "thumb_mime")),
            @AttributeOverride(name = "url", column = @Column(name = "thumb_url"))
    })
    private ImageDetail thumb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Notes note;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDetail {
        private String filename;
        private String mime;
        private String url;
    }
}