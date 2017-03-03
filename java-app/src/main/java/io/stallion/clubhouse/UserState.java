package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.services.Log;

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
