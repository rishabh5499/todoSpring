package in.vyomsoft.todo.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ImgBBService {

    private final RestTemplate restTemplate;

    public ImgBBService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void deleteImage(String deleteUrl) {
        ResponseEntity<String> response = restTemplate.getForEntity(deleteUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Image deleted successfully.");
        } else {
            System.out.println("Failed to delete image: " + response.getStatusCode());
        }
    }
}
