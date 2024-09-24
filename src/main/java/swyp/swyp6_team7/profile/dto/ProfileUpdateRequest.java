package swyp.swyp6_team7.profile.dto;

public class ProfileUpdateRequest {
    private Integer userNumber;
    private String name;;
    private String proIntroduce;

    public Integer getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(Integer userNumber) {
        this.userNumber = userNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProIntroduce() {
        return proIntroduce;
    }

    public void setProIntroduce(String proIntroduce) {
        this.proIntroduce = proIntroduce;
    }
}
