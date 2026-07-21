package com.amanoteam.nz.library;

public class Maintainer {
    private final String name;
    private final String email;

    public Maintainer(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
