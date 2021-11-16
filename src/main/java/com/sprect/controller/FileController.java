package com.sprect.controller;

import com.sprect.model.response.ResponseError;
import com.sprect.service.file.FileService;
import com.sprect.service.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.sprect.utils.DefaultString.EXAMPLE_FILE;
import static com.sprect.utils.DefaultString.RESPONSE_SUCCESS_SAVE_AVATAR;

@RestController
@Slf4j
@RequestMapping("/file")
@Tag(name = "File", description = "to work with files")
public class FileController {
    private final JwtService jwtService;
    private final FileService fileService;

    public FileController(JwtService jwtService,
                          FileService fileService) {
        this.jwtService = jwtService;
        this.fileService = fileService;
    }

    @Operation(summary = "Uploading a user's avatar", tags = {"File"})
    @io.swagger.v3.oas.annotations.parameters.RequestBody
            (content = @Content(
                    mediaType = "formdata",
                    examples = @ExampleObject(value = EXAMPLE_FILE)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The avatar was successfully saved to the vault",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = RESPONSE_SUCCESS_SAVE_AVATAR))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cannot upload empty file or FIle uploaded is not an image",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/uploadAvatar")
    public ResponseEntity<?> uploadAvatar(@RequestHeader("Authorization") String token,
                                          @RequestParam("file") MultipartFile file) {
        String id = jwtService.getClaims(token.substring(7)).getBody().get("id").toString();
        fileService.saveAvatar(id, file);
        return new ResponseEntity<>(fileService.getUrlForDownloadAvatar(id), HttpStatus.OK);
    }

    @Operation(summary = "Deleting a user's avatar", tags = {"File"})
    @ApiResponse(
            responseCode = "200",
            description = "The avatar was successfully deleted to the vault")
    @DeleteMapping("/deleteAvatar")
    public ResponseEntity<?> deleteAvatar(@RequestHeader("Authorization") String token) {
        String id = jwtService.getClaims(token.substring(7)).getBody().get("id").toString();
        fileService.deleteAvatar(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


//    @GetMapping("/download")
//    public ResponseEntity<?> downloadFile(@RequestHeader("Authorization") String token) {
//        String id = jwtService.getBodyToken(token.substring(7)).get("id").toString();
//        return new ResponseEntity<>(todoService.downloadAvatar(id), HttpStatus.OK);
//    }
}