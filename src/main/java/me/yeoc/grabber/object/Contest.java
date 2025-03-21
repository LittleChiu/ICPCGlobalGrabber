package me.yeoc.grabber.object;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
@ToString
@Setter @Getter
public class Contest {
    int id;
    String label;
    boolean active;
    int superContestId;
    String type;
    String additionalInfo;
    List<String> descendants;
    boolean leaf;

}
