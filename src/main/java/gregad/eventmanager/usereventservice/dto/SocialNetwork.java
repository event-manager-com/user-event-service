package gregad.eventmanager.usereventservice.dto;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author Greg Adler
 */
@RequiredArgsConstructor
public enum SocialNetwork {
    FACEBOOK("facebook"),
    TWITTER("twitter"),
    INSTAGRAM("instagram");
    private final String network;

    public static String getNetwork(String str){
        SocialNetwork socialNetworkConstants = Arrays
                .stream(values())
                .filter(e -> e.network.equalsIgnoreCase(str))
                .findAny()
                .orElseThrow(() -> {
                    throw new IllegalArgumentException("Network: " + str + " not supported yet");
                });
        return socialNetworkConstants.network;
    }

    
}
