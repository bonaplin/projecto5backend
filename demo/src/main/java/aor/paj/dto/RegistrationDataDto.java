package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class RegistrationDataDto {
    private int year;
    private int month;
    private int count;

    public RegistrationDataDto() {
    }

    public RegistrationDataDto(int year, int mount, int count) {
        this.year = year;
        this.month = mount;
        this.count = count;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "RegistrationDataDto{" +
                "year=" + year +
                ", month=" + month +
                ", count=" + count +
                '}';
    }
}
