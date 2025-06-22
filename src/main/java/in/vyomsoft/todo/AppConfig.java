package in.vyomsoft.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.vyomsoft.todo.entity.Media;
import in.vyomsoft.todo.payload.MediaDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.config.Configuration.AccessLevel;

@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(AccessLevel.PRIVATE);

        // MediaDTO → Media (already exists, but ensure it's here)
        modelMapper.typeMap(MediaDTO.class, Media.class).setConverter(ctx -> {
            MediaDTO source = ctx.getSource();
            if (source == null || source.getData() == null) return null;

            Media media = new Media();
            media.setMediaId(source.getData().getId());
            media.setTitle(source.getData().getTitle());
            media.setUrlViewer(source.getData().getUrl_viewer());
            media.setDeleteUrl(source.getData().getDelete_url());

            Media.ImageDetail image = new Media.ImageDetail();
            image.setFilename(source.getData().getImage().getFilename());
            image.setMime(source.getData().getImage().getMime());
            image.setUrl(source.getData().getImage().getUrl());
            media.setImage(image);

            Media.ImageDetail thumb = new Media.ImageDetail();
            thumb.setFilename(source.getData().getThumb().getFilename());
            thumb.setMime(source.getData().getThumb().getMime());
            thumb.setUrl(source.getData().getThumb().getUrl());
            media.setThumb(thumb);

            return media;
        });

        // Media → MediaDTO (needed for returning from service)
        modelMapper.typeMap(Media.class, MediaDTO.class).setConverter(ctx -> {
            Media source = ctx.getSource();
            if (source == null) return null;

            MediaDTO.ImageDetailDTO imageDTO = new MediaDTO.ImageDetailDTO(
                    source.getImage().getFilename(),
                    source.getImage().getMime(),
                    source.getImage().getUrl()
            );

            MediaDTO.ImageDetailDTO thumbDTO = new MediaDTO.ImageDetailDTO(
                    source.getThumb().getFilename(),
                    source.getThumb().getMime(),
                    source.getThumb().getUrl()
            );

            MediaDTO.ImageDataDTO dataDTO = new MediaDTO.ImageDataDTO(
                    source.getMediaId(),
                    source.getTitle(),
                    source.getUrlViewer(),
                    imageDTO,
                    thumbDTO,
                    source.getDeleteUrl()
            );

            return new MediaDTO(dataDTO);
        });

        return modelMapper;
    }

}
