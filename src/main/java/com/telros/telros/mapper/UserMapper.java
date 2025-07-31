package com.telros.telros.mapper;

import com.telros.telros.model.User;
import com.telros.telros.model.UserDetails;
import com.telros.telros.dto.request.UserDetailsRequest;
import com.telros.telros.dto.response.UserDetailsResponse;
import com.telros.telros.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Маппер для преобразования между сущностями и DTO
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует User в UserResponse
     *
     * @param user сущность User
     * @return DTO UserResponse
     */
    @Mapping(target = "userDetails", expression = "java(userDetailsToUserDetailsResponse(user.getUserDetails()))")
    UserResponse userToUserResponse(User user);

    /**
     * Преобразует UserDetails в UserDetailsResponse
     *
     * @param userDetails сущность UserDetails
     * @return DTO UserDetailsResponse
     */
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "hasPhoto", expression = "java(userDetails.getUserPhoto() != null)")
    @Mapping(target = "photoUrl", expression = "java(getPhotoUrl(userDetails))")
    UserDetailsResponse userDetailsToUserDetailsResponse(UserDetails userDetails);

    /**
     * Обновляет UserDetails из UserDetailsRequest
     *
     * @param userDetailsRequest DTO с данными для обновления
     * @param userDetails сущность для обновления
     */
    void updateUserDetailsFromRequest(UserDetailsRequest userDetailsRequest, @MappingTarget UserDetails userDetails);

    /**
     * Получает URL фотографии пользователя
     *
     * @param userDetails сущность UserDetails
     * @return URL фотографии или null
     */
    @Named("getPhotoUrl")
    default String getPhotoUrl(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUserPhoto() == null) {
            return null;
        }
        return "/api/users/" + userDetails.getId() + "/photo";
    }
}