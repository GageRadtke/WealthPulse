package com.example.wealthpulse.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.wealthpulse.model.ProfilePicture;
import com.example.wealthpulse.repository.ProfilePictureRepository;

@RestController
@RequestMapping("/api/users/profile-picture")
public class ProfilePictureController {
    /** Limits protect the database from unbounded binary-image storage. */
    private static final int MAX_PROFILE_PICTURES = 15;

    // Profile picture size cap (2MB) to keep DB storage and upload time reasonable.
    private static final long MAX_FILE_BYTES = 2 * 1024 * 1024;
    private final ProfilePictureRepository repository;
    public ProfilePictureController(ProfilePictureRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "username", defaultValue = "default") String username) {
        // Endpoint behavior:
        // 1) Validate file is an image + under size limit
        // 2) Normalize username (trim + lower-case)
        // 3) If user has no picture yet, create one (but enforce total user limit)
        // 4) Save image bytes in the database and return a URL for GET /{username}.
        //
        // This implementation stores image data in the DB for simplicity.

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please choose an image file."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Profile picture must be an image."));
        }

        if (file.getSize() > MAX_FILE_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("error", "Profile picture must be 2 MB or smaller."));
        }

        String cleanUsername = normalizeUsername(username);
        // Look up any existing profile picture row for this username.
        ProfilePicture picture = repository.findByUsernameIgnoreCase(cleanUsername).orElse(null);
        if (picture == null && repository.count() >= MAX_PROFILE_PICTURES) {
            return ResponseEntity.badRequest().body(Map.of("error", "Profile picture storage is limited to 15 users."));
        }

        try {
            // If there wasn't an existing record, we create a new one.
            if (picture == null) {
                picture = new ProfilePicture();
                picture.setUsername(cleanUsername);
            }

            picture.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "profile-picture");
            picture.setContentType(contentType);
            picture.setImageData(file.getBytes());
            picture.setUpdatedAt(LocalDateTime.now());

            ProfilePicture saved = repository.save(picture);
            return ResponseEntity.ok(Map.of(
                    "url", "/api/users/profile-picture/" + saved.getUsername(),
                    "username", saved.getUsername()));
        } catch (Exception error) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not save profile picture."));
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String username) {
        return repository.findByUsernameIgnoreCase(normalizeUsername(username))
                .map(picture -> ResponseEntity.ok()
                        .cacheControl(CacheControl.noCache())
                        .contentType(MediaType.parseMediaType(picture.getContentType()))
                        .body(picture.getImageData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "default";
        }
        return username.trim().toLowerCase();
    }
}
