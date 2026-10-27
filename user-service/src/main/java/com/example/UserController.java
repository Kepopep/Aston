package com.example;

import com.example.DTO.CreateUserRequest;
import com.example.DTO.UpdateUserRequest;
import com.example.DTO.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Операции с пользователями")
public class UserController {

    private final UserService userService;
    private final UserModelAssembler assembler;

    public UserController(UserService userService, UserModelAssembler assembler) {
        this.userService = userService;
        this.assembler = assembler;
    }

    @Operation(summary = "Создать пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные",
            content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDto>> create(
            @RequestBody CreateUserRequest request
    ) {
        User user = userService.save(
                request.name(),
                request.email(),
                request.age()
        );

        EntityModel<UserResponseDto> model = assembler.toModel(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Operation(summary = "Получить пользователя по id")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(assembler.toModel(userService.findById(id)));
    }

    @Operation(summary = "Получить всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDto>>> findAll() {
        List<EntityModel<UserResponseDto>> users = userService.findAll()
                .stream()
                .map(assembler::toModel)
                .toList();

        return ResponseEntity.ok(CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).findAll()).withSelfRel()
        ));
    }

    @Operation(summary = "Частично обновить пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь обновлён")
    @ApiResponse(responseCode = "400", description = "Некорректные данные",
            content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @PatchMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> update(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        User user = userService.update(
                id,
                request.name(),
                request.email(),
                request.age()
        );

        return ResponseEntity.ok(assembler.toModel(user));
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponse(responseCode = "204", description = "Пользователь удалён")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
