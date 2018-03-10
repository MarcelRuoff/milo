/*
 * Copyright (c) 2016 Kevin Herron
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.html.
 */

package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.util.Locale;
import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

public final class LocalizedText {

    public static final LocalizedText NULL_VALUE = new LocalizedText(null, null);

    private final String locale;
    private final String text;

    /**
     * Create a {@link LocalizedText} in English locale.
     *
     * @param text the text in English locale.
     */
    public LocalizedText(@Nullable String text) {
        this(Locale.ENGLISH.getLanguage(), text);
    }

    /**
     * @param locale the locale.
     * @param text   the text in the specified locale.
     */
    public LocalizedText(@Nullable String locale, @Nullable String text) {
        this.locale = locale;
        this.text = text;
    }

    @Nullable
    public String getLocale() {
        return locale;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public static LocalizedText english(@Nullable String text) {
        return new LocalizedText(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalizedText that = (LocalizedText) o;

        return !(locale != null ? !locale.equals(that.locale) : that.locale != null) &&
            !(text != null ? !text.equals(that.text) : that.text != null);
    }

    @Override
    public int hashCode() {
        int result = locale != null ? locale.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("text", text)
            .add("locale", locale)
            .toString();
    }
}
