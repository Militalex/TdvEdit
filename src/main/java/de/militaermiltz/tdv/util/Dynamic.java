package de.militaermiltz.tdv.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Alexander Ley
 * @version 1.0
 *
 * This class maps dynamic arguments like ppp, pp, mp or ff into dynamic values for playsound command.
 *
 */
public enum Dynamic {
    ppp(0.13),
    pp(0.26),
    p(0.39),
    mp(0.51),
    mf(0.63),
    f(0.76),
    ff(0.89),
    fff(1.0);

    private final double volume;

    Dynamic(double volume){
        this.volume = volume;
    }

    /**
     * @return Returns all Values  as String in a list.
     */
    public static List<String> getStringValues(){
        return Arrays.stream(Dynamic.values()).map(Objects::toString).collect(Collectors.toList());
    }

    public double getVolume() {
        return volume;
    }
}
