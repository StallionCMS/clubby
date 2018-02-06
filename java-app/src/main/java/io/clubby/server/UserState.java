package io.clubby.server;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_user_states")
public class UserState extends ModelBase {

    private Long userId;
    private UserStateType state;

    @UniqueKey
    @Column
    public Long getUserId() {
        return userId;
    }

    public UserState setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column
    public UserStateType getState() {
        return state;
    }

    public UserState setState(UserStateType state) {
        this.state = state;
        return this;
    }
}
