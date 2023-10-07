package net.kettlemc.klanguage.common;

import io.github.almightysatan.slams.Context;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "language")
public class LanguageEntity implements Context {

    @Id
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "language")
    private String language;

    /**
     * Required for Jo2SQL, do not use.
     */
    public LanguageEntity() {
    }

    public LanguageEntity(String uuid, String value) {
        this.uuid = uuid;
        this.language = value;
    }

    public String uuid() {
        return this.uuid;
    }

    @Override
    public String language() {
        return this.language;
    }

    public void setLanguage(@NotNull Locale language) {
        Objects.requireNonNull(language);
        this.language = language.toLanguageTag();
    }


}


