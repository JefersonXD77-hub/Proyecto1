
package com.horizontes.util;

public class PasswordGenerator {

    public static void main(String[] args) {
        String plainPassword = "admin123";
        String storedHash = "$2a$10$SspeMZ1MsDKYwlPfefDFYOYAouye4FvqndLTJt073wXBOzn.fs1aK";

        System.out.println("Password original: " + plainPassword);
        System.out.println("Hash almacenado: " + storedHash);
        System.out.println("Verifica: " + BCryptUtil.verifyPassword(plainPassword, storedHash));
    }
}
