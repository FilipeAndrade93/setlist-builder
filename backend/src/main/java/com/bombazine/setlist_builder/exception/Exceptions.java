package com.bombazine.setlist_builder.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

public final class Exceptions {
    private Exceptions() {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class SongNotFoundException extends RuntimeException {
        public SongNotFoundException(UUID id) {
            super("Song not found: " + id);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class SetlistNotFoundException extends RuntimeException {
        public SetlistNotFoundException(UUID id) {
            super("Setlist not found: " + id);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class SetlistAlreadyExistsException extends RuntimeException {
        public SetlistAlreadyExistsException(String venue, String date) {
            super("Setlist for " + venue + " on " + date + " already exists");
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class SongAlreadySavedException extends RuntimeException {
        public SongAlreadySavedException(String spotifyId) {
            super("Spotify track already saved: " + spotifyId);
        }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class InsufficientSongsException extends RuntimeException {
        public InsufficientSongsException (int targetSeconds) {
            super("Not enough songs to fill " + targetSeconds / 60 + " minutes");
        }
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public static class SpotifyApiException extends RuntimeException {
        public SpotifyApiException(String message) { super("Spotify error: " + message);}
        public SpotifyApiException(String message, Throwable cause) {
            super("Spotify error: " + message, cause);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(UUID id) {
            super("User not found: " + id);
        }
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid username or password");
        }
    }

}
