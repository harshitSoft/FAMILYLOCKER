package com.familyvault.dto;

import java.time.Instant;
import java.time.LocalDate;

public class PublicProfileDto {
    public record Request(LocalDate dateOfBirth, String name, String bloodGroup, String education, String contact,
                          String work, String basicInfo, String basicInformation, String phone, String email, String favoriteFood, String recipes,
                          String lifeLessons, String lifeRegrets, String importantAdvice, String hobbies,
                          String publicBio) {}

    public record Response(boolean exists, Long memberId, String fullName, String relationship, String memberCode,
                           LocalDate dateOfBirth, String name, String bloodGroup, String education, String contact,
                           String work, String basicInfo, String basicInformation, String phone, String email, String favoriteFood, String recipes,
                           String lifeLessons, String lifeRegrets, String importantAdvice, String hobbies,
                           String publicBio, Instant updatedAt, String message) {}
}
