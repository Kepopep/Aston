
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> create(
            @RequestBody CreateUserRequest request
    ) {
        User user = userService.save(
                request.name(),
                request.email(),
                request.age()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponseDto.from(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(
            @PathVariable Long id
    ) {
        User user = userService.findById(id);

        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        List<UserResponseDto> users = userService.findAll()
                .stream()
                .map(UserResponseDto::from)
                .toList();

        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        User user = userService.update(
                id,
                request.name(),
                request.email(),
                request.age()
        );

        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
