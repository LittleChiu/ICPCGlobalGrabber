package me.yeoc.grabber.object;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Team {
    int teamId;
    String name;
    String status;
    String country;
    String institution;
}
