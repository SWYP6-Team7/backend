package swyp.swyp6_team7.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "create_ts", nullable = false, updatable = false)
    private LocalDateTime createTs;

    @Column(name = "update_ts", nullable = false)
    private LocalDateTime updateTs;

    protected void setUpdateTs(LocalDateTime updateTs) {
        this.updateTs = updateTs;
    }
}
