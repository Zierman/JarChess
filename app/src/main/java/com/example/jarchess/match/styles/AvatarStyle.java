package com.example.jarchess.match.styles;

/**
 * An avatar style is a style that allows the resource ids of avatar art in the style to be accessed.
 */
public interface AvatarStyle {

    /**
     * Gets the avatar resource id that is linked to this style.
     *
     * @return the avatar resource id linked to this style
     */
    int getAvatarResourceID();
}
