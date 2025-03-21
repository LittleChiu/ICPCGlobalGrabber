package me.yeoc.grabber.object;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@RequiredArgsConstructor
@Getter @Setter
public class TeamObject {
    @NonNull
    String name;
    @NonNull
    int siteId;
    @NonNull
    int institutionUnitId;
    boolean studentCoach = false;
    List<String> teamMembers = new ArrayList<>();
}
