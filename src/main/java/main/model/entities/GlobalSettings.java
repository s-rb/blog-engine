package main.model.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "global_settings")
public class GlobalSettings implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String code;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GlobalSettings(String code, String name, String value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }

    public GlobalSettings() {
    }

    @Override
    public String toString() {
        return "GlobalSettings{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
