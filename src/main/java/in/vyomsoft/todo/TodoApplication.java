package in.vyomsoft.todo;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TodoApplication {

		@Bean
		public ModelMapper modelMapper() {
			return new ModelMapper();
		}
		public static void main(String[] args) {
			SpringApplication.run(TodoApplication.class, args);
		}
	}

