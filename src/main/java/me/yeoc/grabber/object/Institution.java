package me.yeoc.grabber.object;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class Institution {
    int id;
    String name;
    String abbr;
    String url;
    String country;
}
