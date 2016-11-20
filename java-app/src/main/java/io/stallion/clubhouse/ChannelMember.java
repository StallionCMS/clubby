package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_channel_members")
public class ChannelMember extends ModelBase {
    private Long userId = 0L;
    private Long channelId = 0L;

    @Column
    public Long getUserId() {
        return userId;
    }

    public ChannelMember setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column
    public Long getChannelId() {
        return channelId;
    }

    public ChannelMember setChannelId(Long channelId) {
        this.channelId = channelId;
        return this;
    }
}
