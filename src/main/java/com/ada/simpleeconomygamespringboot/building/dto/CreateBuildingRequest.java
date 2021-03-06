package com.ada.simpleeconomygamespringboot.building.dto;

import lombok.*;
import com.ada.simpleeconomygamespringboot.building.entity.Building;
import com.ada.simpleeconomygamespringboot.user.entity.User;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class CreateBuildingRequest {

    private String type;

    private User user;

    public static Function<CreateBuildingRequest, Building> dtoToEntityMapper(
            Supplier<User> userSupplier) {
        return request -> Building.builder()
                .type(request.getType())
                .user(userSupplier.get())
                .build();
    }
}
