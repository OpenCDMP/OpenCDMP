package org.opencdmp.model.plan;


public class PlanContact {

    private String firstName;

    public static final String _firstName = "firstName";
    private String lastName;

    public static final String _lastName = "lastName";
    private String email;
    public static final String _email = "email";

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
