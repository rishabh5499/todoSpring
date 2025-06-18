package in.vyomsoft.todo.service;

import in.vyomsoft.todo.payload.LoginDto;
import in.vyomsoft.todo.payload.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);
    String register(RegisterDto registerDto);
}
