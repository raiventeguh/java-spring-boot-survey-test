package dev.raiven.survey.model;

public class SurveyData {
    private String firstName;
    private String lastName;
    private Integer salary;
    private Integer zipCode;

    public SurveyData(String firstName, String lastName, Integer salary, Integer zipCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.zipCode = zipCode;
    }

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

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }
}
